# 个人信息修改

## 基本信息

| 字段 | 内容 |
| --- | --- |
| 请求路径 | `/api/user/profile/update` |
| 请求方式 | POST |
| Content-Type | `application/json` |
| 鉴权 | 需要 Token |
| Service | `com.hatsune.lingolens.module.user.service.UserAccountService#updateProfile` |

## 请求参数

至少传一个可修改字段。所有 Service 入参以 `String requestJson` 接收，再通过 fastjson 反序列化。

| 字段 | 类型 | 必填 | 备注 |
| --- | --- | --- | --- |
| `nickname` | String | 否 | 昵称，2-32 位，不能是 8 位纯数字 |
| `avatarUrl` | String | 否 | 头像 URL |
| `email` | String | 否 | 用户邮箱，最长 128 |
| `bio` | String | 否 | 用户个人简介，最长 256，可传空字符串清空 |

## 响应参数

| 字段 | 类型 | 备注 |
| --- | --- | --- |
| `result.rCode` | String | `0` 成功，`1` 失败 |
| `result.msg` | String | 结果提示 |
| `id` | Long | 用户表主键 ID，后端内部使用 |
| `userId` | String | 用户可见 ID |
| `nickname` | String | 修改后的昵称 |
| `avatarUrl` | String | 修改后的头像 URL |
| `email` | String | 修改后的邮箱 |
| `bio` | String | 修改后的个人简介 |

## 请求示例

```json
{
  "nickname": "hatsune",
  "avatarUrl": "http://159.75.140.18:9000/lingolens/avatar.png",
  "email": "hatsune@example.com",
  "bio": "用现实场景记住新单词。"
}
```
