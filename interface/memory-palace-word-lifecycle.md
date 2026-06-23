# Memory Palace Word Lifecycle

## Delete

`POST /api/memory-palace/word/delete`

```json
{"palaceWordId": 123456}
```

删除用户自己的宫殿单词关联。若该词不再属于用户的任何宫殿，同时从该用户的 AR 扫描词本中移除；全局共享 `word_assets` 不删除。

## Move

`POST /api/memory-palace/word/move`

```json
{
  "palaceWordId": 123456,
  "targetPalaceId": 987654
}
```

移动到用户自己的目标宫殿，并同步更新源/目标宫殿 `word_count`。如果目标宫殿已经包含相同 `word_asset_id`，会合并为目标宫殿已有记录，避免重复卡片。
