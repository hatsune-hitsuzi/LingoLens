# 词库列表接口

## 基本信息

| 项目 | 说明 |
| --- | --- |
| 接口名称 | 词库列表 |
| 请求方式 | POST |
| 请求路径 | `/api/word-book/list` |
| Content-Type | `application/json` |
| 是否需要登录 | 是 |
| Service | `com.hatsune.lingolens.module.wordbook.service.WordBookService#list` |

## 请求参数

| 参数名 | 类型 | 必填 | 备注 |
| --- | --- | --- | --- |
| `pageNumber` | Integer | 否 | 页码，默认 1 |
| `pageSize` | Integer | 否 | 每页数量，默认 20，最大 100 |
| `keyword` | String | 否 | 按词库名称或描述模糊搜索 |
| `difficultyLevel` | Integer | 否 | 难度等级 |
| `status` | Integer | 否 | 状态，建议 1 表示启用 |

## 返回参数

| 参数名 | 类型 | 备注 |
| --- | --- | --- |
| `result.rCode` | String | `0` 成功，`1` 失败 |
| `result.msg` | String | 返回说明 |
| `pageNumber` | Integer | 当前页码 |
| `pageSize` | Integer | 每页数量 |
| `total` | Integer | 总数量 |
| `wordBooks` | Array | 词库列表 |
| `wordBooks[].bookId` | Long | 词库 ID |
| `wordBooks[].bookName` | String | 词库名称 |
| `wordBooks[].description` | String | 词库描述 |
| `wordBooks[].coverUrl` | String | 封面 URL |
| `wordBooks[].wordCount` | Integer | 单词数量 |
| `wordBooks[].difficultyLevel` | Integer | 难度等级 |
| `wordBooks[].status` | Integer | 状态 |

## 请求示例

```json
{
  "pageNumber": 1,
  "pageSize": 20,
  "keyword": "core",
  "status": 1
}
```

## 成功响应示例

```json
{
  "result": {
    "rCode": "0",
    "msg": "success"
  },
  "pageNumber": 1,
  "pageSize": 20,
  "total": 1,
  "wordBooks": [
    {
      "bookId": 100001,
      "bookName": "LingoLens Core Vocabulary",
      "description": "核心词库",
      "coverUrl": "",
      "wordCount": 3000,
      "difficultyLevel": 1,
      "status": 1
    }
  ]
}
```
