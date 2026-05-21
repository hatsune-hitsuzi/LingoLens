# 统一响应结构

## 结构

所有 HTTP 接口统一返回 `result + 业务字段`。

```json
{
  "result": {
    "rCode": "0",
    "msg": "success"
  }
}
```

## result 字段

| 参数名 | 类型 | 必填 | 备注 |
| --- | --- | --- | --- |
| `result` | Object | 是 | 统一接口结果 |
| `result.rCode` | String | 是 | `0` 表示成功，`1` 表示失败 |
| `result.msg` | String | 是 | 提示信息，失败时可直接用于前端提醒 |

## 业务字段

除 `result` 外，各接口可按业务需要增加字段，例如登录接口增加 `token`，词库列表接口增加 `wordBooks`。
