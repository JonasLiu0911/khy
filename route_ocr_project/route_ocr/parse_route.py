from __future__ import annotations

import re
from statistics import median
from rapidfuzz import fuzz
from .schemas import OCRBox, RouteExtraction, DirectionTable

TIME_RE = re.compile(r"\b(?:[01]?\d|2[0-3])[:：][0-5]\d\b")
PHONE_RE = re.compile(r"(?:\d{3,4}[-—])?\d{5,8}(?:[,，、]\d{5,8})*")


def norm_text(s: str) -> str:
    return s.strip().replace(" ", "").replace("：", ":").replace("⇔", "↔").replace("一", "-")


def extract_times(text: str) -> list[str]:
    text = norm_text(text)
    return [t.replace("：", ":") for t in TIME_RE.findall(text)]


def line_group(boxes: list[OCRBox], y_tol: float | None = None) -> list[list[OCRBox]]:
    boxes = sorted(boxes, key=lambda b: (b.cy, b.cx))
    if not boxes:
        return []
    if y_tol is None:
        heights = [max(8, b.height) for b in boxes]
        y_tol = max(10, median(heights) * 0.8)
    lines: list[list[OCRBox]] = []
    for b in boxes:
        placed = False
        for line in lines:
            if abs(b.cy - median([x.cy for x in line])) <= y_tol:
                line.append(b)
                placed = True
                break
        if not placed:
            lines.append([b])
    for line in lines:
        line.sort(key=lambda b: b.cx)
    lines.sort(key=lambda line: median([b.cy for b in line]))
    return lines


def line_text(line: list[OCRBox]) -> str:
    return "".join(norm_text(b.text) for b in sorted(line, key=lambda b: b.cx))


def split_stations_from_text(text: str) -> list[str]:
    text = norm_text(text)
    # Keep only the likely route string after heading noise.
    if "客运站" in text:
        text = text[text.index("客运站"):]
    text = re.sub(r"[=—\-]+", "→", text)
    parts = re.split(r"[→↔⇄⇒]+", text)
    stations = []
    for p in parts:
        p = re.sub(r"[^\u4e00-\u9fffA-Za-z0-9]", "", p)
        if 1 < len(p) <= 8 and ("站" in p or "村" in p or p == "中心村"):
            stations.append(p)
    # de-duplicate preserving order
    out = []
    for s in stations:
        if s not in out:
            out.append(s)
    return out


def infer_route_name(lines: list[str]) -> str | None:
    for s in lines[:8]:
        if "客货邮服务点" in s:
            return s
    for s in lines[:8]:
        if "服务点" in s:
            return s
    return None


def find_route_line(lines: list[list[OCRBox]]) -> tuple[int | None, list[str]]:
    best_idx, best_score, best_stations = None, -1, []
    for i, line in enumerate(lines):
        txt = line_text(line)
        score = fuzz.partial_ratio(txt, "客运站村创新中心")
        stations = split_stations_from_text(txt)
        if len(stations) >= 3:
            score += 30 + 10 * len(stations)
        if score > best_score:
            best_idx, best_score, best_stations = i, score, stations
    return best_idx, best_stations


def times_by_columns(lines: list[list[OCRBox]], stations: list[str]) -> list[list[str]]:
    if not stations:
        return []
    time_boxes = []
    for line in lines:
        for b in line:
            ts = extract_times(b.text)
            for t in ts:
                time_boxes.append((b.cx, b.cy, t))
    if not time_boxes:
        return []
    time_boxes.sort(key=lambda x: (x[1], x[0]))

    # If OCR returned one box per row containing many times, use row-wise extraction directly.
    row_lists = []
    for line in lines:
        ts = []
        for b in sorted(line, key=lambda b: b.cx):
            ts.extend(extract_times(b.text))
        if len(ts) >= max(2, len(stations) - 1):
            row_lists.append(ts)
    if row_lists:
        return [r[:len(stations)] for r in row_lists]

    # Otherwise group by vertical rows.
    groups: list[list[tuple[float, float, str]]] = []
    y_tol = 18
    for tb in time_boxes:
        placed = False
        for g in groups:
            if abs(tb[1] - median([x[1] for x in g])) < y_tol:
                g.append(tb)
                placed = True
                break
        if not placed:
            groups.append([tb])
    rows = []
    for g in groups:
        g.sort(key=lambda x: x[0])
        row = [x[2] for x in g]
        if len(row) >= 2:
            rows.append(row[:len(stations)])
    return rows

def station_time_json(
    stations: list[str],
    upper_rows: list[list[str]],
    lower_rows: list[list[str]],
) -> list[dict]:
    """
    将按行解析出来的时刻表，转换成：
    [
        {
            "站点": "...",
            "上方的时刻": [...],
            "下方的时刻": [...]
        }
    ]

    upper_rows / lower_rows 的结构类似：
    [
        ["07:00", "07:10", "07:20"],
        ["09:00", "09:10", "09:20"]
    ]
    """
    out = []

    for idx, station in enumerate(stations):
        upper_times = [
            row[idx]
            for row in upper_rows
            if idx < len(row) and row[idx]
        ]

        lower_times = [
            row[idx]
            for row in lower_rows
            if idx < len(row) and row[idx]
        ]

        out.append({
            "站点": station,
            "上方的时刻": upper_times,
            "下方的时刻": lower_times,
        })

    return out

def parse_misc(lines_text: list[str]) -> dict:
    d = {"fares": {}}
    joined = "\n".join(lines_text)
    m = re.search(r"运营时间[:：]?\s*([0-9:：\-—~至]+)", joined)
    if m:
        d["operation_time"] = m.group(1).replace("：", ":").replace("—", "-")
    m = re.search(r"监督电话[:：]?\s*([0-9,，、\-—]+)", joined)
    if m:
        d["phone"] = m.group(1).replace("，", ",").replace("、", ",").replace("—", "-")
    m = re.search(r"运输频次[:：]?([^\n]+)", joined)
    if m:
        d["frequency"] = m.group(1)[:20]
    for s in lines_text:
        for route, price in re.findall(r"(客运站至[^:：\s]{2,8})[:：]?(\d+元/人)", s):
            d["fares"][route] = price
    return d


def parse_route_from_ocr(boxes: list[OCRBox]) -> RouteExtraction:
    boxes = [b for b in boxes if b.score >= 0.35 and b.text.strip()]
    lines = line_group(boxes)
    lines_text = [line_text(line) for line in lines]
    result = RouteExtraction(raw_text_lines=lines_text)
    result.route_name = infer_route_name(lines_text)

    route_idx, stations = find_route_line(lines)
    result.stations = stations
    if not stations:
        result.confidence_notes.append("没有稳定识别到站点行，请检查 debug/raw_ocr.json 或使用手动裁剪。")
        return result
    if route_idx is None:
        result.confidence_notes.append("未能定位线路站点行，时间表可能不可靠。")
        return result

    upper_lines = lines[max(0, route_idx - 8):route_idx]
    lower_lines = lines[route_idx + 1:route_idx + 8]
    forward = times_by_columns(upper_lines, stations)
    reverse_raw = times_by_columns(lower_lines, stations)

    result.forward_direction = DirectionTable(
        direction="→".join(stations),
        trips=forward,
    )
    result.reverse_direction = DirectionTable(
        direction="→".join(list(reversed(stations))),
        trips=reverse_raw,
    )

    # 新增：按“站点、上方的时刻、下方的时刻”结构保存
    result.station_times = station_time_json(
        stations=stations,
        upper_rows=forward,
        lower_rows=reverse_raw,
    )

    misc = parse_misc(lines_text)
    result.frequency = misc.get("frequency")
    result.operation_time = misc.get("operation_time")
    result.phone = misc.get("phone")
    result.fares = misc.get("fares", {})

    if not forward:
        result.confidence_notes.append("正向时间未能按行稳定解析，可查看 raw_text_lines。")
    if not reverse_raw:
        result.confidence_notes.append("反向时间未能按行稳定解析，可查看 raw_text_lines。")
    return result
