# 记忆宫殿列表

## 基本信息

| 字段 | 内容 |
| --- | --- |
| 请求路径 | `/api/memory-palace/list` |
| 请求方式 | POST |
| Content-Type | `application/json` |
| 鉴权 | 需要 Token |
| Service | `com.hatsune.lingolens.module.memorypalace.service.MemoryPalaceService#list` |

## 请求参数

| 字段 | 类型 | 必填 | 备注 |
| --- | --- | --- | --- |
| `pageNumber` | Integer | 否 | 页码，默认 1 |
| `pageSize` | Integer | 否 | 每页条数，默认 20，最大 100 |

## 响应参数

| 字段 | 类型 | 备注 |
| --- | --- | --- |
| `result.rCode` | String | `0` 成功，`1` 失败 |
| `result.msg` | String | 结果提示 |
| `pageNumber` | Integer | 当前页码 |
| `pageSize` | Integer | 每页条数 |
| `total` | Integer | 总条数 |
| `palaces` | Array | 记忆宫殿列表 |
| `palaces[].palaceId` | Long | 记忆宫殿主键 ID |
| `palaces[].sceneName` | String | 场景名称 |
| `palaces[].coverUrl` | String | 封面图 URL |
| `palaces[].wordCount` | Integer | 单词数量 |
| `palaces[].updateTime` | String | 最近更新时间 |

## 请求示例

```json
{
  "pageNumber": 1,
  "pageSize": 20
}
```

## 响应示例

```json
{
  "result": {
    "rCode": "0",
    "msg": "success"
  },
  "pageNumber": 1,
  "pageSize": 20,
  "total": 1,
  "palaces": [
    {
      "palaceId": 20001,
      "sceneName": "浅草咖啡馆",
      "coverUrl": "http://159.75.140.18:9000/lingolens/demo.jpg",
      "wordCount": 15,
      "updateTime": "2026-05-23T09:30:00+09:00"
    }
  ]
}
```
