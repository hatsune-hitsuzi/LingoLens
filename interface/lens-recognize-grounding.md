# Lens Grounding

## Endpoint

`POST /api/lens/recognize/grounding`

拍摄按钮专用的云端 VLM 视觉定位接口。实时相机预览仍由本地 YOLO 处理；拍摄后客户端停止本地推理，将 1024px、JPEG 75% 的定格图片交给本接口。

## Request

```json
{
  "imageBase64": "...",
  "imageContentType": "image/jpeg",
  "sceneName": "现场扫描",
  "sourceType": "camera-grounding",
  "clientTraceId": "grounding-123",
  "sourceLanguage": "zh-CN",
  "targetLanguage": "ja"
}
```

`sourceLanguage` 是母语，`targetLanguage` 是目标学习语言。也可只传可被模型服务访问的 `imageUrl`。

## Response

```json
{
  "result": {"rCode": "0", "msg": "success"},
  "nodes": [
    {
      "id": "laptop",
      "polygon": [[10, 20], [35, 18], [38, 46], [12, 48]],
      "learningWord": "ノートパソコン",
      "learningPhonetic": "noto pasokon",
      "nativeTranslation": "笔记本电脑"
    }
  ]
}
```

后端会限制坐标到 `[0,100]`、过滤少于 3 个点的 polygon，并按英文 `id` 去重。
