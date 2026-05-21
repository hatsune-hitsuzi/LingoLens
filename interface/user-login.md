# 用户登录接口

## 基本信息

| 项目 | 说明 |
| --- | --- |
| 接口名称 | 用户登录 |
| 请求方式 | POST |
| 请求路径 | `/lingolens/user/login` |
| Content-Type | `application/json` |
| 是否需要登录 | 否 |
| Service | `com.hatsune.lingolens.module.user.service.UserAuthService#login` |

## 请求参数

请求体为 JSON 字符串，Service 层统一按 `String requestJson` 接收后使用 fastjson2 反序列化。

| 参数名 | 类型 | 必填 | 备注 |
| --- | --- | --- | --- |
| `account` | String | 是 | 登录账号，支持 `userId` 或昵称，最大 32 字符 |
| `password` | String | 是 | 登录密码，长度 8-72 |

## 返回参数

| 参数名 | 类型 | 备注 |
| --- | --- | --- |
| `result.rCode` | String | `0` 成功，`1` 失败 |
| `result.msg` | String | 返回说明 |
| `token` | String | JWT Token，成功时返回 |
| `tokenExpireAt` | Long | Token 过期时间，Unix 秒级时间戳，成功时返回 |

## 请求示例

```json
{
  "account": "10000001",
  "password": "password123"
}
```

## 成功响应示例

```json
{
  "result": {
    "rCode": "0",
    "msg": "success"
  },
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenExpireAt": 1779954498
}
```

## 失败响应示例

```json
{
  "result": {
    "rCode": "1",
    "msg": "account or password is invalid"
  }
}
```
