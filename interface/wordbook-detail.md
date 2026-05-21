# 词库详情接口

## 基本信息

| 项目 | 说明 |
| --- | --- |
| 接口名称 | 词库详情 |
| 请求方式 | POST |
| 请求路径 | `/api/word-book/detail` |
| Content-Type | `application/json` |
| 是否需要登录 | 是 |
| Service | `com.hatsune.lingolens.module.wordbook.service.WordBookService#detail` |

## 请求参数

| 参数名 | 类型 | 必填 | 备注 |
| --- | --- | --- | --- |
| `bookId` | Long | 是 | 词库 ID |

## 返回参数

| 参数名 | 类型 | 备注 |
| --- | --- | --- |
| `result.rCode` | String | `0` 成功，`1` 失败 |
| `result.msg` | String | 返回说明 |
| `wordBook.bookId` | Long | 词库 ID |
| `wordBook.bookName` | String | 词库名称 |
| `wordBook.description` | String | 词库描述 |
| `wordBook.coverUrl` | String | 封面 URL |
| `wordBook.wordCount` | Integer | 单词数量 |
| `wordBook.difficultyLevel` | Integer | 难度等级 |
| `wordBook.status` | Integer | 状态 |

## 请求示例

```json
{
  "bookId": 100001
}
```

## 成功响应示例

```json
{
  "result": {
    "rCode": "0",
    "msg": "success"
  },
  "wordBook": {
    "bookId": 100001,
    "bookName": "LingoLens Core Vocabulary",
    "description": "核心词库",
    "coverUrl": "",
    "wordCount": 3000,
    "difficultyLevel": 1,
    "status": 1
  }
}
```

## 失败响应示例

```json
{
  "result": {
    "rCode": "1",
    "msg": "word book not found"
  }
}
```
