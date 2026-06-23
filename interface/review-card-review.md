# 扫描卡片复习接口

## 1. 今日复习数量

- 方法：`POST`
- 路径：`/api/v1/review/count`
- 请求：空 JSON `{}` 即可

响应：

```json
{
  "result": { "rCode": "0", "msg": "success" },
  "remainingCount": 12,
  "previewImageUrls": [
    "https://img.sso.internal/hatsune/palace1_slot1.jpg",
    "https://img.sso.internal/hatsune/palace1_slot2.jpg",
    "https://img.sso.internal/hatsune/palace1_slot3.jpg"
  ]
}
```

`remainingCount` 来自当前用户 `memory_palace_words` 中今天尚未写入 `review_records` 的去重 `word_asset_id` 数量。`previewImageUrls` 返回最多 3 张最新加入宫殿的非空 `sourceImageUrl`，用于移动端个人页“今日复习任务”入口的模糊扇形预览 logo；即使今日任务已完成，也可保持入口视觉稳定。

## 2. 今日复习卡片

- 方法：`POST`
- 路径：`/api/v1/review/cards`

请求：

```json
{
  "pageSize": 30
}
```

响应：

```json
{
  "result": { "rCode": "0", "msg": "success" },
  "total": 12,
  "cards": [
    {
      "cardId": 10001,
      "palaceId": 1,
      "wordAssetId": 940000000000003,
      "sceneName": "浅草咖啡馆",
      "word": "monitor",
      "displayWord": "Monitor",
      "phonetic": "[ˈmɑːnɪtər]",
      "pronunciationUrl": "https://dict.youdao.com/dictvoice?audio=monitor&type=2",
      "partOfSpeech": "n.",
      "translation": "显示器；监控器",
      "definition": "a screen used to display information from a computer",
      "exampleSentence": "The monitor glowed softly beside the coffee cup on the desk.",
      "exampleTranslation": "显示器在桌上的咖啡杯旁柔和地亮着。",
      "sourceImageUrl": "http://..."
    }
  ]
}
```

## 3. 批量提交复习结果

- 方法：`POST`
- 路径：`/api/v1/review/answer/batch`

请求：

```json
{
  "answers": [
    {
      "cardId": 10001,
      "wordId": 10001,
      "wordAssetId": 940000000000003,
      "word": "monitor",
      "isRecognized": true,
      "answerTime": "2026-05-26T21:20:00.000+09:00"
    }
  ]
}
```

响应：

```json
{
  "result": { "rCode": "0", "msg": "success" },
  "acceptedCount": 1,
  "remainingCount": 11
}
```

移动端通过 `ReviewSubmissionQueue` 做内存批量缓存，右滑和左滑都写入 `review_records`。后端会落 `word_id` 和 `word_asset_id`，今日是否已复习按 `word_asset_id` 去重判断。记忆宫殿浏览卡片不调用本接口。
