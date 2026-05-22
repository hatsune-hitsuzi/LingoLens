# Lens 场景识别

## 基本信息

| 字段 | 内容 |
| --- | --- |
| 请求路径 | `/api/lens/recognize` |
| 请求方式 | POST |
| Content-Type | `application/json` |
| 鉴权 | 需要 Token |
| Service | `com.hatsune.lingolens.module.lens.service.LensService#recognize` |

## 请求参数

所有 Service 入参以 `String requestJson` 接收，再通过 fastjson 反序列化。

| 字段 | 类型 | 必填 | 备注 |
| --- | --- | --- | --- |
| `imageUrl` | String | 条件必填 | 来源图片 URL。`sourceType=demo` 时可不传；真实识别场景建议先走 OSS 上传。 |
| `sceneName` | String | 否 | 场景名称，不传默认 `浅草咖啡馆`。 |
| `sourceType` | String | 否 | 来源类型，默认 `demo`。非 `demo` 时 `imageUrl` 必填。 |
| `clientTraceId` | String | 否 | 客户端链路 ID，用于幂等排查和问题追踪。 |

## 响应参数

| 字段 | 类型 | 备注 |
| --- | --- | --- |
| `result.rCode` | String | `0` 成功，`1` 失败 |
| `result.msg` | String | 结果提示 |
| `sceneId` | Long | 后端生成的场景 ID |
| `sceneName` | String | 场景名称 |
| `words` | Array | 识别出的单词气泡 |
| `words[].recognitionId` | String | 本次识别单词 ID |
| `words[].word` | String | 单词原文 |
| `words[].displayWord` | String | 单词展示文案 |
| `words[].confidence` | Double | 识别置信度 |
| `words[].box` | Object | 前端定位框，参考 393x852 的移动端设计坐标 |

## 请求示例

```json
{
  "sceneName": "浅草咖啡馆",
  "sourceType": "demo",
  "clientTraceId": "mobile-10001"
}
```

## 响应示例

```json
{
  "result": {
    "rCode": "0",
    "msg": "success"
  },
  "sceneId": 10001,
  "sceneName": "浅草咖啡馆",
  "words": [
    {
      "recognitionId": "lens_10001_1",
      "word": "mug",
      "displayWord": "Mug",
      "confidence": 0.96,
      "box": {
        "x": 95,
        "y": 320,
        "width": 80,
        "height": 40
      }
    },
    {
      "recognitionId": "lens_10001_2",
      "word": "keyboard",
      "displayWord": "Keyboard",
      "confidence": 0.94,
      "box": {
        "x": 230,
        "y": 460,
        "width": 112,
        "height": 46
      }
    }
  ]
}
```
