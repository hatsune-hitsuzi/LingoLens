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
| `sourceLanguage` | String | 否 | 源语言，默认 `zh-CN`。 |
| `targetLanguage` | String | 否 | 目标语言，默认 `en`。 |
| `sceneName` | String | 否 | 当前识别场景名称。 |

## 响应参数

| 字段 | 类型 | 备注 |
| --- | --- | --- |
| `result.rCode` | String | `0` 成功，`1` 失败 |
| `result.msg` | String | 结果提示 |
| `wordInfo.wordAssetId` | Long | 全局单词资产 ID |
| `wordInfo.word` | String | 单词原文 |
| `wordInfo.displayWord` | String | 目标语言展示词；英文为英文词，日文为日文词 |
| `wordInfo.phonetic` | String | 目标语言读音；英文为音标，日文为罗马音 |
| `wordInfo.pronunciationUrl` | String | 有道 dictvoice 发音 URL |
| `wordInfo.partOfSpeech` | String | 目标语言词性 |
| `wordInfo.translation` | String | 源语言翻译 |
| `wordInfo.definition` | String | 源语言释义 |
| `wordInfo.exampleSentence` | String | 目标语言例句 |
| `wordInfo.exampleTranslation` | String | 源语言例句翻译 |

## 请求示例

```json
{
  "word": "mug",
  "sourceLanguage": "zh-CN",
  "targetLanguage": "ja",
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
    "wordAssetId": 940000000000001,
    "word": "mug",
    "displayWord": "マグカップ",
    "phonetic": "[magukappu]",
    "pronunciationUrl": "http://dict.youdao.com/dictvoice?le=jap&audio=%E3%83%9E%E3%82%B0%E3%82%AB%E3%83%83%E3%83%97&type=3",
    "partOfSpeech": "名詞",
    "translation": "马克杯",
    "definition": "一种带把手、常用来喝热饮的杯子。",
    "exampleSentence": "彼女は熱いコーヒーを陶器のマグカップに注いだ。",
    "exampleTranslation": "她把热咖啡倒进陶瓷马克杯里。"
  }
}
```
