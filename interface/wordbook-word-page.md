# 词库单词分页接口

## 基本信息

| 项目 | 说明 |
| --- | --- |
| 接口名称 | 词库单词分页 |
| 请求方式 | POST |
| 请求路径 | `/api/word-book/word/page` |
| Content-Type | `application/json` |
| 是否需要登录 | 是 |
| Service | `com.hatsune.lingolens.module.wordbook.service.WordBookService#pageWords` |

## 请求参数

| 参数名 | 类型 | 必填 | 备注 |
| --- | --- | --- | --- |
| `bookId` | Long | 是 | 词库 ID |
| `pageNumber` | Integer | 否 | 页码，默认 1 |
| `pageSize` | Integer | 否 | 每页数量，默认 50，最大 200 |
| `keyword` | String | 否 | 按单词或中文释义模糊搜索 |

## 返回参数

| 参数名 | 类型 | 备注 |
| --- | --- | --- |
| `result.rCode` | String | `0` 成功，`1` 失败 |
| `result.msg` | String | 返回说明 |
| `bookId` | Long | 词库 ID |
| `pageNumber` | Integer | 当前页码 |
| `pageSize` | Integer | 每页数量 |
| `total` | Integer | 总数量 |
| `words` | Array | 单词列表 |
| `words[].wordId` | Long | 单词 ID |
| `words[].word` | String | 单词 |
| `words[].phonetic` | String | 音标 |
| `words[].pronunciationUrl` | String | 发音 URL |
| `words[].translation` | String | 中文释义 |
| `words[].definition` | String | 英文释义 |
| `words[].exampleSentence` | String | 例句 |
| `words[].exampleTranslation` | String | 例句翻译 |
| `words[].sortNo` | Integer | 词库内排序 |

## 请求示例

```json
{
  "bookId": 100001,
  "pageNumber": 1,
  "pageSize": 50
}
```

## 成功响应示例

```json
{
  "result": {
    "rCode": "0",
    "msg": "success"
  },
  "bookId": 100001,
  "pageNumber": 1,
  "pageSize": 50,
  "total": 1,
  "words": [
    {
      "wordId": 200001,
      "bookId": 100001,
      "word": "abandon",
      "phonetic": "/əˈbændən/",
      "pronunciationUrl": "",
      "translation": "放弃",
      "definition": "to leave someone or something",
      "exampleSentence": "Never abandon your curiosity.",
      "exampleTranslation": "永远不要放弃你的好奇心。",
      "sortNo": 1
    }
  ]
}
```
