from __future__ import annotations

import argparse
import json
from pathlib import Path
import cv2

from .image_preprocess import load_and_preprocess
from .ocr_engine import PaddleOCREngine, draw_ocr_boxes, save_raw_ocr
from .parse_route import parse_route_from_ocr


def main() -> None:
    parser = argparse.ArgumentParser(description="Extract route stations and times from 客货邮 service poster images.")
    parser.add_argument("--image", required=True, help="Input image path")
    parser.add_argument("--out", default="result.json", help="Output JSON path")
    parser.add_argument("--debug-dir", default=None, help="Directory for intermediate images and raw OCR")
    parser.add_argument("--no-auto-crop", action="store_true", help="Disable poster auto-crop")
    args = parser.parse_args()

    debug_dir = Path(args.debug_dir) if args.debug_dir else None
    image, prep_debug = load_and_preprocess(args.image, auto_crop=not args.no_auto_crop, debug_dir=debug_dir)

    temp_path = (debug_dir / "03_enhanced.jpg") if debug_dir else Path(args.out).with_suffix(".enhanced.jpg")
    if not debug_dir:
        cv2.imwrite(str(temp_path), image)

    engine = PaddleOCREngine()
    boxes = engine.run(temp_path)

    if debug_dir:
        save_raw_ocr(boxes, debug_dir / "raw_ocr.json")
        draw_ocr_boxes(temp_path, boxes, debug_dir / "04_ocr_boxes.jpg")

    result = parse_route_from_ocr(boxes)
    result.debug.update(prep_debug)
    if debug_dir:
        result.debug["debug_dir"] = str(debug_dir)

    out_path = Path(args.out)
    out_path.parent.mkdir(parents=True, exist_ok=True)
    with open(out_path, "w", encoding="utf-8") as f:
        json.dump(result.model_dump(), f, ensure_ascii=False, indent=2)
    print(json.dumps(result.model_dump(), ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
