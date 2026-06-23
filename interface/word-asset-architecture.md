# 单词资产与发音链路

## 表结构主轴

`word_assets` 是全局总词表，不属于单个用户。一个概念词只保存一行：

```text
word_assets
  id
  word_key
  canonical_language
  canonical_word
  language_payloads
  pronunciation_base_url
  asset_image_url
  asset_source
```

用户侧表只保存 `word_asset_id`：

```text
memory_palace_words.word_asset_id
ar_scan_words.word_asset_id
review_records.word_asset_id
```

因此用户后续从 `zh-CN -> en` 切到 `zh-CN -> ja`，只需要按同一个 `word_asset_id` 重新投影语言 payload，不需要复制或迁移用户数据。

## 发音 URL

数据库不再在 `word_assets` 中保存绝对 `pronunciation_url`。公共 host 存在 `word_assets.pronunciation_base_url`，每个语言 payload 保存 `pronunciationPath`：

```text
base: http://dict.youdao.com/dictvoice
en path: ?audio=monitor&type=2
ja path: ?le=jap&audio=%E3%83%A2%E3%83%8B%E3%82%BF%E3%83%BC&type=3
zh path: ?le=zh&audio=%E6%98%BE%E7%A4%BA%E5%99%A8&type=2
```

后端响应时动态返回：

```text
pronunciationUrl = pronunciation_base_url + pronunciationPath
```

## 解析流程

```text
POST /api/lens/word/detail
  -> normalize wordKey
  -> Redis: word:asset:v4:{sourceLanguage}:{targetLanguage}:{wordKey}
  -> PostgreSQL: SELECT * FROM word_assets WHERE word_key = ?
  -> parse language_payloads
  -> targetLanguage payload: displayWord / phonetic / pos / explanation / example / pronunciationPath
  -> sourceLanguage payload: translation / exampleTranslation
  -> response: LensWordInfoDTO
```

如果数据库没有该 `word_key`，后端通过 `WordAssetGenerator` 生成最小可用 payload 并写入 `word_assets`。如果已有概念词但缺少目标语言 payload，后端会 merge 新 payload 到同一行 JSON。

## 手机端识别链路

```text
上传/拍摄照片
  -> 移动端本地 YOLO/TFLite 识别物体框和 objectName
  -> /api/lens/recognize(sourceType=local-yolo, detectedTags)
  -> 后端立即返回识别气泡
  -> 后台异步预热 en / ja / zh payload
  -> 用户点击气泡时 /api/lens/word/detail 优先走 Redis/DB
```

## Mock 与验证脚本

- Seed：`LingoLens/scripts/generate_hatsune_mock_data.py`
- Verify：`LingoLens/scripts/verify_hatsune_integration.py`

验证覆盖：

- `word_assets` 只有单行 JSON 多语言 payload。
- `phonetic` 统一承载 IPA、拼音、罗马音。
- `pronunciationUrl` 由 base + path 动态拼接。
- `hatsune` 拥有 5 个宫殿、30 个 slot、30 条 AR 扫描词本记录。
