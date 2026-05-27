from __future__ import annotations

from pathlib import Path
import cv2
import numpy as np


def order_points(pts: np.ndarray) -> np.ndarray:
    rect = np.zeros((4, 2), dtype="float32")
    s = pts.sum(axis=1)
    rect[0] = pts[np.argmin(s)]
    rect[2] = pts[np.argmax(s)]
    diff = np.diff(pts, axis=1)
    rect[1] = pts[np.argmin(diff)]
    rect[3] = pts[np.argmax(diff)]
    return rect


def four_point_transform(image: np.ndarray, pts: np.ndarray) -> np.ndarray:
    rect = order_points(pts)
    (tl, tr, br, bl) = rect
    width_a = np.linalg.norm(br - bl)
    width_b = np.linalg.norm(tr - tl)
    max_width = max(int(width_a), int(width_b))
    height_a = np.linalg.norm(tr - br)
    height_b = np.linalg.norm(tl - bl)
    max_height = max(int(height_a), int(height_b))
    dst = np.array([
        [0, 0],
        [max_width - 1, 0],
        [max_width - 1, max_height - 1],
        [0, max_height - 1],
    ], dtype="float32")
    m = cv2.getPerspectiveTransform(rect, dst)
    return cv2.warpPerspective(image, m, (max_width, max_height))


def _largest_blue_white_poster_contour(image: np.ndarray):
    hsv = cv2.cvtColor(image, cv2.COLOR_BGR2HSV)
    # Blue header and border area. Tuned for China Post service-point posters.
    blue = cv2.inRange(hsv, np.array([85, 35, 50]), np.array([135, 255, 255]))
    # White body area, helpful when the blue header is small.
    white = cv2.inRange(hsv, np.array([0, 0, 150]), np.array([180, 80, 255]))
    mask = cv2.bitwise_or(blue, white)
    kernel = cv2.getStructuringElement(cv2.MORPH_RECT, (15, 15))
    mask = cv2.morphologyEx(mask, cv2.MORPH_CLOSE, kernel, iterations=2)
    mask = cv2.morphologyEx(mask, cv2.MORPH_OPEN, kernel, iterations=1)
    contours, _ = cv2.findContours(mask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
    h, w = image.shape[:2]
    candidates = []
    for c in contours:
        area = cv2.contourArea(c)
        if area < 0.02 * w * h:
            continue
        x, y, cw, ch = cv2.boundingRect(c)
        ratio = ch / max(cw, 1)
        if 1.0 < ratio < 4.5:
            candidates.append((area, c))
    if not candidates:
        return None
    candidates.sort(key=lambda item: item[0], reverse=True)
    return candidates[0][1]


def crop_poster(image: np.ndarray, auto_crop: bool = True) -> tuple[np.ndarray, dict]:
    debug = {"auto_crop_used": False, "crop_method": "none"}
    if not auto_crop:
        return image, debug
    contour = _largest_blue_white_poster_contour(image)
    if contour is None:
        debug["warning"] = "No suitable poster contour found; using original image."
        return image, debug

    peri = cv2.arcLength(contour, True)
    approx = cv2.approxPolyDP(contour, 0.02 * peri, True)
    if len(approx) == 4:
        warped = four_point_transform(image, approx.reshape(4, 2).astype("float32"))
        debug.update({"auto_crop_used": True, "crop_method": "perspective"})
        return warped, debug

    x, y, w, h = cv2.boundingRect(contour)
    pad = int(0.03 * max(w, h))
    x1, y1 = max(0, x - pad), max(0, y - pad)
    x2, y2 = min(image.shape[1], x + w + pad), min(image.shape[0], y + h + pad)
    debug.update({"auto_crop_used": True, "crop_method": "bounding_rect", "rect": [x1, y1, x2, y2]})
    return image[y1:y2, x1:x2], debug


def enhance_for_ocr(image: np.ndarray, scale: float = 2.0) -> np.ndarray:
    if scale != 1:
        image = cv2.resize(image, None, fx=scale, fy=scale, interpolation=cv2.INTER_CUBIC)
    lab = cv2.cvtColor(image, cv2.COLOR_BGR2LAB)
    l, a, b = cv2.split(lab)
    clahe = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8, 8))
    l2 = clahe.apply(l)
    enhanced = cv2.merge((l2, a, b))
    enhanced = cv2.cvtColor(enhanced, cv2.COLOR_LAB2BGR)
    blur = cv2.GaussianBlur(enhanced, (0, 0), sigmaX=1.0)
    sharp = cv2.addWeighted(enhanced, 1.5, blur, -0.5, 0)
    return sharp


def load_and_preprocess(path: str | Path, auto_crop: bool = True, debug_dir: str | Path | None = None) -> tuple[np.ndarray, dict]:
    image = cv2.imread(str(path))
    if image is None:
        raise FileNotFoundError(f"Cannot read image: {path}")
    poster, debug = crop_poster(image, auto_crop=auto_crop)
    enhanced = enhance_for_ocr(poster)
    if debug_dir:
        d = Path(debug_dir)
        d.mkdir(parents=True, exist_ok=True)
        cv2.imwrite(str(d / "01_original.jpg"), image)
        cv2.imwrite(str(d / "02_poster_crop.jpg"), poster)
        cv2.imwrite(str(d / "03_enhanced.jpg"), enhanced)
    return enhanced, debug
