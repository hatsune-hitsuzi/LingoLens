# 用户注册接口

## 基本信息

| 项目 | 说明 |
| --- | --- |
| 接口名称 | 用户注册 |
| 请求方式 | POST |
| 请求路径 | `/lingolens/user/register` |
| Content-Type | `application/json` |
| 是否需要登录 | 否 |
| Service | `com.hatsune.lingolens.module.user.service.UserAuthService#register` |

## 请求参数

请求体为 JSON 字符串，Service 层统一按 `String requestJson` 接收后使用 fastjson2 反序列化。

| 参数名 | 类型 | 必填 | 备注 |
| --- | --- | --- | --- |
| `nickname` | String | 是 | 用户昵称，长度 2-32；不能是 8 位纯数字 |
| `password` | String | 是 | 登录密码，长度 8-72；后端使用 BCrypt 存储密文 |
| `avatarUrl` | String | 是 | 头像 URL，最大 512 字符；前端应先调用 OSS 通用上传接口获取 |

## 返回参数

| 参数名 | 类型 | 备注 |
| --- | --- | --- |
| `result.rCode` | String | `0` 成功，`1` 失败 |
| `result.msg` | String | 返回说明 |

## 请求示例

```json
{
  "nickname": "hatsune",
  "password": "password123",
  "avatarUrl": "http://159.75.140.18:9000/lingolens/lingolens_user_anonymous_xxx/avatar/a.png"
}
```

## 成功响应示例

```json
{
  "result": {
    "rCode": "0",
    "msg": "success"
  }
}
```

## 失败响应示例

```json
{
  "result": {
    "rCode": "1",
    "msg": "nickname already exists"
  }
}
```
