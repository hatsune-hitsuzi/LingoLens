# 用户退出登录接口

## 基本信息

| 项目 | 说明 |
| --- | --- |
| 接口名称 | 用户退出登录 |
| 请求方式 | POST |
| 请求路径 | `/api/user/logout` |
| Content-Type | `application/json` |
| 是否需要登录 | 是 |
| Service | `com.hatsune.lingolens.module.user.service.UserAccountService#logout` |

## 请求头

| 参数名 | 类型 | 必填 | 备注 |
| --- | --- | --- | --- |
| `Authorization` | String | 否 | 推荐格式：`Bearer {token}` |
| `token` | String | 否 | 兼容写法，直接传 Token |

> `Authorization` 和 `token` 至少传一个。

## 请求参数

当前接口暂无业务入参，建议传 `{}`。

## 返回参数

| 参数名 | 类型 | 备注 |
| --- | --- | --- |
| `result.rCode` | String | `0` 成功，`1` 失败 |
| `result.msg` | String | 返回说明 |

## 成功响应示例

```json
{
  "result": {
    "rCode": "0",
    "msg": "success"
  }
}
```

## 备注

- 退出登录会删除 Redis 中的 `auth:token:{token}`。
- 退出成功后，当前 Token 立即失效。
