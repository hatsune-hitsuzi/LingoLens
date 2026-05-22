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
| `words[].word` | String | 单词原文 |
| `words[].displayWord` | String | 展示单词 |
| `words[].phonetic` | String | 音标 |
| `words[].pronunciationUrl` | String | 发音音频 URL |
| `words[].partOfSpeech` | String | 词性 |
| `words[].translation` | String | 翻译 |
| `words[].definition` | String | 英文释义 |
| `words[].exampleSentence` | String | 例句 |
| `words[].exampleTranslation` | String | 例句翻译 |
| `words[].sourceImageUrl` | String | 来源图片 URL |
| `words[].createTime` | String | 创建时间 |

## 请求示例

```json
{
  "palaceId": 20001,
  "pageNumber": 1,
  "pageSize": 20
}
```
