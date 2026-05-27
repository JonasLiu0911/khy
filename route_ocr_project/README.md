# 客货邮线路牌 OCR 信息提取项目

本项目用于从手机拍摄的“客货邮融合线路及走向图”图片中提取：

- 服务点/路线名称
- 线路绑定站点
- 各站点上方或下方的班次时间
- 正向、反向班次表
- 可选的票价、运输频次、运营时间、监督电话

核心思路：

1. 用 OpenCV 自动裁剪蓝白色线路牌区域，并做透视校正、放大、锐化、对比度增强。
2. 用 PaddleOCR/PP-OCRv5 识别中文与数字文本。
3. 按 OCR 文本框的几何位置，将“客货邮融合线路及走向图”区域分成：上方正向时间、线路站点行、下方反向时间。
4. 输出 JSON，便于后续入库或人工校验。

## 1. 安装

建议 Python 3.10 或 3.11。

```bash
cd route_ocr_project
python -m venv .venv
# Windows: .venv\Scripts\activate
source .venv/bin/activate
pip install -r requirements.txt
```

如果你的机器有 NVIDIA GPU，请按 PaddlePaddle 官方说明安装匹配 CUDA 的 GPU 版本；否则默认 CPU 也能跑，只是慢一些。

## 2. 快速运行

```bash
python -m route_ocr.extract --image ./examples/test.jpg --out result.json --debug-dir debug
```

输出示例：

```json
{
  "route_name": "牛项村客货邮服务点",
  "stations": ["客运站", "牛项村", "官溪村", "鹅鼻村", "创新村", "中心村"],
  "forward_direction": {
    "direction": "客运站→牛项村→官溪村→鹅鼻村→创新村→中心村",
    "trips": [
      ["06:40", "06:40", "07:10", "07:40", "08:00", "08:30"]
    ]
  },
  "reverse_direction": {
    "direction": "中心村→创新村→鹅鼻村→官溪村→牛项村→客运站",
    "trips": [
      ["07:15", "07:30", "07:45", "08:00", "08:15", "08:30"]
    ]
  }
}
```

## 3. 运行参数

```bash
python -m route_ocr.extract \
  --image input.jpg \
  --out output.json \
  --debug-dir debug \
  --no-auto-crop
```

- `--image`：输入图片路径。
- `--out`：输出 JSON 路径。
- `--debug-dir`：保存中间裁剪图、OCR 可视化图、原始 OCR 结果，便于排错。
- `--no-auto-crop`：跳过自动找牌面，直接对整张图识别。

## 4. 建议拍摄方式

为提高自动提取准确率：

- 尽量正对牌面拍摄，减少斜拍。
- 让“客货邮融合线路及走向图”占据图片较大比例。
- 避免强反光、遮挡、夜间噪声。
- 若远距离拍摄，建议先手动裁剪出牌面再输入。

## 5. 项目结构

```text
route_ocr_project/
  requirements.txt
  README.md
  route_ocr/
    __init__.py
    extract.py
    image_preprocess.py
    ocr_engine.py
    parse_route.py
    schemas.py
```

