# 记忆宫殿单词分页

## 基本信息

| 字段 | 内容 |
| --- | --- |
| 请求路径 | `/api/memory-palace/word/page` |
| 请求方式 | POST |
| Content-Type | `application/json` |
| 鉴权 | 需要 Token |
| Service | `com.hatsune.lingolens.module.memorypalace.service.MemoryPalaceService#pageWords` |

## 请求参数

| 字段 | 类型 | 必填 | 备注 |
| --- | --- | --- | --- |
| `palaceId` | Long | 是 | 记忆宫殿主键 ID |
| `pageNumber` | Integer | 否 | 页码，默认 1 |
| `pageSize` | Integer | 否 | 每页条数，默认 20，最大 100 |

## 响应参数

| 字段 | 类型 | 备注 |
| --- | --- | --- |
| `result.rCode` | String | `0` 成功，`1` 失败 |
| `result.msg` | String | 结果提示 |
| `pageNumber` | Integer | 当前页码 |
| `pageSize` | Integer | 每页条数 |
| `total` | Integer | 总条数 |
| `words` | Array | 单词列表 |
| `words[].palaceWordId` | Long | 记忆宫殿单词主键 ID |
| `words[].palaceId` | Long | 记忆宫殿主键 ID |
| `words[].wordAssetId` | Long | 全局单词资产 ID |
| `words[].slotKey` | String | 宫殿锚点 key |
| `words[].slotName` | String | 宫殿锚点展示名 |
| `words[].anchorDescription` | String | 锚点描述 |
| `words[].imageOptimized` | Boolean | 图片是否已压缩优化 |
| `words[].imageUploadProvider` | String | 图片来源，当前为 `ai-generated` |
| `words[].word` | String | 稳定英文 `word_key` |
| `words[].displayWord` | String | 当前目标语言展示词 |
| `words[].phonetic` | String | 当前目标语言读音文本：IPA / 罗马音 / 拼音 |
| `words[].pronunciationUrl` | String | 动态拼接后的发音 URL |
| `words[].partOfSpeech` | String | 词性 |
| `words[].translation` | String | 当前源语言词面 |
| `words[].definition` | String | 当前目标语言解释 |
| `words[].exampleSentence` | String | 当前目标语言例句 |
| `words[].exampleTranslation` | String | 当前源语言例句 |
| `words[].sourceImageUrl` | String | SSO 图床或本地 asset URL |
| `words[].createTime` | String | 创建时间，格式 `YYYY/MM/DD` |

`displayWord`、`phonetic`、`translation`、`definition`、`exampleSentence`、`exampleTranslation` 都从 `word_assets.language_payloads` 按当前用户语言偏好投影，不在宫殿词表中保存语言快照。

## 请求示例

```json
{
  "palaceId": 951000000000001,
  "pageNumber": 1,
  "pageSize": 20
}
```
