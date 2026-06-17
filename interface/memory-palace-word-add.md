# 加入记忆宫殿

## 基本信息

| 字段 | 内容 |
| --- | --- |
| 请求路径 | `/api/memory-palace/word/add` |
| 请求方式 | POST |
| Content-Type | `application/json` |
| 鉴权 | 需要 Token |
| Service | `com.hatsune.lingolens.module.memorypalace.service.MemoryPalaceService#addWord` |

## 请求参数

所有 Service 入参以 `String requestJson` 接收，再通过 fastjson 反序列化。

| 字段 | 类型 | 必填 | 备注 |
| --- | --- | --- | --- |
| `sceneName` | String | 否 | 场景名称，不传默认 `Default Scene` |
| `sourceImageUrl` | String | 否 | 来源图片 URL，可由 OSS 上传接口返回 |
| `clientTraceId` | String | 否 | 客户端链路 ID |
| `wordAssetId` | Long | 推荐 | 全局单词资产 ID；有它时后端只保存 ID，并按当前语言偏好投影展示 |
| `word` | String | 条件必填 | 没有 `wordAssetId` 时必填，后端会先创建/补齐词资产 |
| `displayWord` / `phonetic` / `pronunciationUrl` / `partOfSpeech` | String | 否 | 仅作为无 `wordAssetId` 的本地兜底信息 |
| `translation` / `definition` / `exampleSentence` / `exampleTranslation` | String | 否 | 仅作为无 `wordAssetId` 的本地兜底信息 |

## 响应参数

| 字段 | 类型 | 备注 |
| --- | --- | --- |
| `result.rCode` | String | `0` 成功，`1` 失败 |
| `result.msg` | String | 结果提示 |
| `palaceId` | Long | 记忆宫殿主键 ID |
| `palaceWordId` | Long | 记忆宫殿单词主键 ID |
| `palaceWordCount` | Integer | 当前宫殿单词数量 |
| `added` | Boolean | 是否本次新加入，重复添加时为 `false` |

## 请求示例

```json
{
  "sceneName": "浅草咖啡馆",
  "sourceImageUrl": "http://159.75.140.18:9000/lingolens/demo.jpg",
  "clientTraceId": "mobile-add-10001",
  "wordAssetId": 940000000000001,
  "word": "mug"
}
```

## 响应示例

```json
{
  "result": {
    "rCode": "0",
    "msg": "added"
  },
  "palaceId": 20001,
  "palaceWordId": 30001,
  "palaceWordCount": 15,
  "added": true
}
```
