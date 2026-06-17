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
| `sourceType` | String | 否 | 来源类型，默认 `demo`。端侧检测建议传 `local-yolo`。非 `demo` 且未传 `detectedTags` 时 `imageUrl` 必填。 |
| `clientTraceId` | String | 否 | 客户端链路 ID，用于幂等排查和问题追踪。 |
| `detectedTags` | Array | 否 | 移动端本地 YOLO/TFLite 已识别出的物体 tag。传入后后端不再使用 demo 词，而是直接按 tag 生成气泡，并异步预热中英日词资产。 |
| `detectedTags[].tag` | String | 是 | 稳定物体名，建议使用英文小写 label，例如 `monitor`、`mug`。 |
| `detectedTags[].displayWord` | String | 否 | 客户端原始展示名；后端优先按用户语言偏好投影展示。 |
| `detectedTags[].confidence` | Double | 否 | 识别置信度，范围 0-1。 |
| `detectedTags[].x/y/width/height` | Integer | 否 | 物体框坐标，参考移动端设计坐标；不传时后端生成默认位置。 |

## 响应参数

| 字段 | 类型 | 备注 |
| --- | --- | --- |
| `result.rCode` | String | `0` 成功，`1` 失败 |
| `result.msg` | String | 结果提示 |
| `sceneId` | Long | 后端生成的场景 ID |
| `sceneName` | String | 场景名称 |
| `words` | Array | 识别出的单词气泡 |
| `words[].recognitionId` | String | 本次识别单词 ID |
| `words[].wordAssetId` | Long | 全局单词资产 ID；后续加入宫殿、复习记录都应优先使用它 |
| `words[].word` | String | 单词原文 |
| `words[].displayWord` | String | 单词展示文案 |
| `words[].confidence` | Double | 识别置信度 |
| `words[].box` | Object | 前端定位框，参考 393x852 的移动端设计坐标 |

## 请求示例

```json
{
  "imageUrl": "http://159.75.140.18:9000/lingolens/lingolens_user_10000001/lens/desk.jpg",
  "sceneName": "现场扫描",
  "sourceType": "local-yolo",
  "clientTraceId": "mobile-10001",
  "detectedTags": [
    {
      "tag": "monitor",
      "displayWord": "monitor",
      "confidence": 0.91,
      "x": 210,
      "y": 260,
      "width": 124,
      "height": 72
    }
  ]
}
```

## 端侧识别与异步资产补全

移动端先用本地 detector 识别照片，拿到 `detectedTags` 后调用本接口。接口立即返回气泡列表；同时后端后台预热以下语言方向，逐步写入 `word_assets` 并刷新 Redis：

```text
zh-CN -> en
zh-CN -> ja
ja -> zh-CN
ja -> en
en -> ja
en -> zh-CN
```

用户点气泡打开单词卡时，再调用 `/api/lens/word/detail` 获取当前语言方向的完整词卡。如果后台还没预热完成，该接口会同步走 Redis -> PostgreSQL -> LLM/生成器 -> PostgreSQL -> Redis，并返回当前词卡。

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
      "wordAssetId": 940000000000001,
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
      "wordAssetId": 940000000000002,
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
