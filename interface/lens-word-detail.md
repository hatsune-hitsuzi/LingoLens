# Lens 单词卡详情

## 基本信息

| 字段 | 内容 |
| --- | --- |
| 请求路径 | `/api/lens/word/detail` |
| 请求方式 | POST |
| Content-Type | `application/json` |
| 鉴权 | 需要 Token |
| Service | `com.hatsune.lingolens.module.lens.service.LensService#wordDetail` |

## 请求参数

| 字段 | 类型 | 必填 | 备注 |
| --- | --- | --- | --- |
| `word` | String | 是 | 单词原文，如 `mug`。 |
| `sourceLanguage` | String | 否 | 源语言，默认 `en`。 |
| `targetLanguage` | String | 否 | 目标语言，默认 `zh-CN`。 |
| `sceneName` | String | 否 | 当前识别场景名称。 |

## 响应参数

| 字段 | 类型 | 备注 |
| --- | --- | --- |
| `result.rCode` | String | `0` 成功，`1` 失败 |
| `result.msg` | String | 结果提示 |
| `wordInfo.word` | String | 单词原文 |
| `wordInfo.displayWord` | String | 展示单词 |
| `wordInfo.phonetic` | String | 音标 |
| `wordInfo.pronunciationUrl` | String | 发音音频 URL，当前演示实现可为空 |
| `wordInfo.partOfSpeech` | String | 词性 |
| `wordInfo.translation` | String | 翻译 |
| `wordInfo.definition` | String | 英文释义 |
| `wordInfo.exampleSentence` | String | 英文例句 |
| `wordInfo.exampleTranslation` | String | 例句翻译 |

## 请求示例

```json
{
  "word": "mug",
  "sourceLanguage": "en",
  "targetLanguage": "zh-CN",
  "sceneName": "浅草咖啡馆"
}
```

## 响应示例

```json
{
  "result": {
    "rCode": "0",
    "msg": "success"
  },
  "wordInfo": {
    "word": "mug",
    "displayWord": "Mug",
    "phonetic": "[mʌg]",
    "pronunciationUrl": "",
    "partOfSpeech": "n.",
    "translation": "马克杯",
    "definition": "a large cup with a handle",
    "exampleSentence": "She poured steaming coffee into her favorite ceramic mug.",
    "exampleTranslation": "她把热咖啡倒进最喜欢的陶瓷马克杯里。"
  }
}
```
