from __future__ import annotations

from pathlib import Path
import json
import cv2
from .schemas import OCRBox


def _normalize_rapidocr_result(result) -> list[OCRBox]:
    boxes: list[OCRBox] = []

    if not result:
        return boxes

    # RapidOCR typical result:
    # [
    #   [box, text, score],
    #   ...
    # ]
    for item in result:
        try:
            box = item[0]
            text = item[1]
            score = float(item[2])

            if hasattr(box, "tolist"):
                box = box.tolist()

            boxes.append(
                OCRBox(
                    text=str(text).strip(),
                    score=score,
                    box=box,
                )
            )
        except Exception:
            continue

    return [b for b in boxes if b.text]


class PaddleOCREngine:
    """
    保留类名不变，避免修改 extract.py。
    实际底层已经从 PaddleOCR 切换为 RapidOCR。
    """

    def __init__(self):
        try:
            from rapidocr_onnxruntime import RapidOCR
        except Exception as exc:
            raise RuntimeError(
                "RapidOCR import failed. Install with: pip install rapidocr-onnxruntime"
            ) from exc

        self.ocr = RapidOCR()

    def run(self, image_path: str | Path) -> list[OCRBox]:
        result, _ = self.ocr(str(image_path))
        boxes = _normalize_rapidocr_result(result)
        return boxes


def draw_ocr_boxes(image_path: str | Path, boxes: list[OCRBox], out_path: str | Path) -> None:
    img = cv2.imread(str(image_path))

    if img is None:
        raise RuntimeError(f"Failed to read image: {image_path}")

    for b in boxes:
        pts = [(int(x), int(y)) for x, y in b.box]

        for i in range(len(pts)):
            cv2.line(
                img,
                pts[i],
                pts[(i + 1) % len(pts)],
                (0, 255, 0),
                2,
            )

        cv2.putText(
            img,
            b.text[:20],
            pts[0],
            cv2.FONT_HERSHEY_SIMPLEX,
            0.55,
            (0, 0, 255),
            1,
            cv2.LINE_AA,
        )

    cv2.imwrite(str(out_path), img)


def save_raw_ocr(boxes: list[OCRBox], out_path: str | Path) -> None:
    with open(out_path, "w", encoding="utf-8") as f:
        json.dump(
            [b.model_dump() for b in boxes],
            f,
            ensure_ascii=False,
            indent=2,
        )