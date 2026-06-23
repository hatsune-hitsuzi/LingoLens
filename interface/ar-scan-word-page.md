# AR 扫描词本分页

- 方法：`POST`
- 路径：`/api/ar-scan/word/page`

请求：

```json
{
  "pageNumber": 1,
  "pageSize": 20,
  "keyword": "mug"
}
```

响应：

```json
{
  "result": { "rCode": "0", "msg": "success" },
  "pageNumber": 1,
  "pageSize": 20,
  "total": 32,
  "words": [
    {
      "scanWordId": 300000000000001,
      "wordAssetId": 940000000000001,
      "firstPalaceId": 1,
      "sceneName": "浅草咖啡馆",
      "word": "mug",
      "displayWord": "Mug",
      "phonetic": "[mʌg]",
      "pronunciationUrl": "https://dict.youdao.com/dictvoice?audio=mug&type=2",
      "partOfSpeech": "n.",
      "translation": "马克杯",
      "definition": "a large cup with a handle",
      "exampleSentence": "She poured steaming coffee into her favorite ceramic mug.",
      "exampleTranslation": "她把热咖啡倒进最喜欢的陶瓷马克杯里。",
      "sourceImageUrl": "http://...",
      "createTime": "2026-05-26T12:00:00+09:00",
      "updateTime": "2026-05-26T12:00:00+09:00"
    }
  ]
}
```

`keyword` 可为空；不为空时匹配稳定词 key、当前语言展示词、翻译和场景名。

同一用户同一个 `wordAssetId` 在 `ar_scan_words` 中只保留一条；如果在新场景再次加入，后端会用最新 `sceneName`、`sourceImageUrl`、`firstPalaceId` 覆盖旧记录。展示字段按用户当前语言偏好实时投影。
