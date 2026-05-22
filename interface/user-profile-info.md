# 个人信息查询

## 基本信息

| 字段 | 内容 |
| --- | --- |
| 请求路径 | `/api/user/profile/info` |
| 请求方式 | POST |
| Content-Type | `application/json` |
| 鉴权 | 需要 Token |
| Service | `com.hatsune.lingolens.module.user.service.UserAccountService#profileInfo` |

## 请求参数

可传空 JSON。

```json
{}
```

## 响应参数

| 字段 | 类型 | 备注 |
| --- | --- | --- |
| `result.rCode` | String | `0` 成功，`1` 失败 |
| `result.msg` | String | 结果提示 |
| `id` | Long | 用户表主键 ID，后端内部使用 |
| `userId` | String | 用户可见 ID |
| `nickname` | String | 昵称 |
| `avatarUrl` | String | 头像 URL |
| `email` | String | 用户邮箱 |
| `bio` | String | 用户个人简介 |

## 响应示例

```json
{
  "result": {
    "rCode": "0",
    "msg": "success"
  },
  "id": 100000000000001,
  "userId": "10000001",
  "nickname": "hatsune",
  "avatarUrl": "http://159.75.140.18:9000/lingolens/avatar.png",
  "email": "hatsune@example.com",
  "bio": "用现实场景记住新单词。"
}
```
