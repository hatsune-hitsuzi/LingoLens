# LLM YAML 配置与词资产映射

## 配置文件

后端读取 `src/main/resources/application.yml`：

```yaml
lingolens:
  llm:
    enabled: true
    invoke-url: https://integrate.api.nvidia.com/v1/chat/completions
    authorization: ${LINGOLENS_LLM_AUTHORIZATION:}
    model: stepfun-ai/step-3.7-flash
    response-format:
      type: json_object
    languages:
      - code: zh
        response-key: item_zh
      - code: en
        response-key: item_en
      - code: ja
        response-key: item_ja
```

`authorization` 支持完整 header 值，例如：

```powershell
$env:LINGOLENS_LLM_AUTHORIZATION="Bearer <your-key>"
```

不要把真实 key 提交进仓库。

## 请求约定

后端调用 OpenAI-compatible chat/completions：

```json
{
  "model": "stepfun-ai/step-3.7-flash",
  "messages": [
    {"role": "system", "content": "..."},
    {
      "role": "user",
      "content": [
        {"type": "text", "text": "..."},
        {"type": "image_url", "image_url": {"url": "<uploaded image url>"}}
      ]
    }
  ],
  "response_format": {"type": "json_object"},
  "stream": false
}
```

## 返回 JSON 映射

LLM 返回：

```json
{
  "item_zh": {"word": "苹果", "phonetic": "ping guo", "pos": "名词", "explanation": "一种常见水果。", "example": "桌子上有一个红色的苹果。"},
  "item_en": {"word": "apple", "phonetic": "/ˈæp.əl/", "pos": "noun", "explanation": "a common fruit", "example": "There is a red apple on the table."},
  "item_ja": {"word": "リンゴ", "phonetic": "ringo", "pos": "名詞", "explanation": "一般的な果物。", "example": "机の上に赤いリンゴがあります。"}
}
```

后端会压缩为 `word_assets.language_payloads`：

```json
[
  {"lang": "zh", "word": "苹果", "phonetic": "ping guo", "pos": "名词", "explanation": "一种常见水果。", "example": "桌子上有一个红色的苹果。", "pronunciationPath": "?le=zh&audio=..."},
  {"lang": "en", "word": "apple", "phonetic": "/ˈæp.əl/", "pos": "noun", "explanation": "a common fruit", "example": "There is a red apple on the table.", "pronunciationPath": "?audio=..."},
  {"lang": "ja", "word": "リンゴ", "phonetic": "ringo", "pos": "名詞", "explanation": "一般的な果物。", "example": "机の上に赤いリンゴがあります。", "pronunciationPath": "?le=jap&audio=..."}
]
```

旧别名也兼容：`pronunciation` 会映射到 `phonetic`，`example_sentence` 会映射到 `example`。
