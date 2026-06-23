# LingoLens 多语言词资产设计

## 语言方向

移动端通过用户偏好维护 `sourceLanguage -> targetLanguage`。

- `sourceLanguage`：用户熟悉、用于解释和翻译的语言，例如 `zh-CN`。
- `targetLanguage`：用户正在学习、卡片主视觉展示的语言，例如 `en` 或 `ja`。

示例：选择 `zh-CN -> ja` 时，卡片主词展示日文 `モニター`，`phonetic` 展示罗马音 `monitā`，翻译/解释使用中文。

## 核心原则

后端内部始终以稳定的 `word_assets.id` 和 `word_key` 为主轴。用户侧表只保存 `word_asset_id`，这样用户切换语言方向时，宫殿、AR 扫描词本、复习记录都不需要迁移。

## JSON Schema

`word_assets.language_payloads` 存储一个 JSON 数组，每个概念词只占一行：

```json
[
  {
    "lang": "en",
    "word": "monitor",
    "phonetic": "/ˈmɑːnɪtər/",
    "pos": "noun",
    "explanation": "A screen used to show information from a computer.",
    "example": "The monitor displayed the design draft clearly.",
    "pronunciationPath": "?audio=monitor&type=2"
  },
  {
    "lang": "ja",
    "word": "モニター",
    "phonetic": "monitā",
    "pos": "名詞",
    "explanation": "コンピューターや映像の情報を表示する画面。",
    "example": "モニターにデザイン案がはっきり表示された。",
    "pronunciationPath": "?le=jap&audio=%E3%83%A2%E3%83%8B%E3%82%BF%E3%83%BC&type=3"
  },
  {
    "lang": "zh",
    "word": "显示器",
    "phonetic": "xiǎn shì qì",
    "pos": "名词",
    "explanation": "用于显示电脑或影像信息的屏幕设备。",
    "example": "显示器清楚地展示了设计草图。",
    "pronunciationPath": "?le=zh&audio=%E6%98%BE%E7%A4%BA%E5%99%A8&type=2"
  }
]
```

统一约束：英文音标、中文拼音、日文罗马音都使用同一个字段名 `phonetic`，不再拆分 `pinyin` 或 `romaji`。

## 返回字段投影

| API 字段 | 来源 |
| --- | --- |
| `wordAssetId` | `word_assets.id` |
| `word` | 稳定英文 `word_key` |
| `displayWord` | `targetLanguage` 对应 payload 的 `word` |
| `phonetic` | `targetLanguage` 对应 payload 的 `phonetic` |
| `partOfSpeech` | `targetLanguage` 对应 payload 的 `pos` |
| `translation` | `sourceLanguage` 对应 payload 的 `word` |
| `definition` | `targetLanguage` 对应 payload 的 `explanation` |
| `exampleSentence` | `targetLanguage` 对应 payload 的 `example` |
| `exampleTranslation` | `sourceLanguage` 对应 payload 的 `example` |
| `pronunciationUrl` | `pronunciation_base_url + pronunciationPath` |

## 读写链路

```text
移动端识别 object tag
  -> wordKey = monitor
  -> Redis: word:asset:v4:{sourceLanguage}:{targetLanguage}:{wordKey}
  -> PostgreSQL: word_assets by word_key
  -> parse language_payloads JSON
  -> targetLanguage/sourceLanguage 投影为 DTO
  -> 如果缺语言 payload，LLM/本地生成器补齐并写回 word_assets
```

## 扩展新语言

新增语言时只需要：

1. 在 `LanguagePairCatalog` 和移动端 `LanguagePair.defaults()` 增加语言方向。
2. 让 `WordAssetManager` 的语言归一化识别新语言短码。
3. 在 `language_payloads` 数组中追加新语言 payload。
4. Redis key 已包含 source/target，不同语言方向天然隔离。
