# 用户资料修改接口

## 基本信息

| 项目 | 说明 |
| --- | --- |
| 接口名称 | 用户资料修改 |
| 请求方式 | POST |
| 请求路径 | `/api/user/profile/update` |
| Content-Type | `application/json` |
| 是否需要登录 | 是 |
| Service | `com.hatsune.lingolens.module.user.service.UserAccountService#updateProfile` |

## 请求头

| 参数名 | 类型 | 必填 | 备注 |
| --- | --- | --- | --- |
| `Authorization` | String | 否 | 推荐格式：`Bearer {token}` |
| `token` | String | 否 | 兼容写法，直接传 Token |

## 请求参数

| 参数名 | 类型 | 必填 | 备注 |
| --- | --- | --- | --- |
| `nickname` | String | 否 | 新昵称，长度 2-32；不能是 8 位纯数字 |
| `avatarUrl` | String | 否 | 新头像 URL，最大 512 字符 |

> `nickname` 和 `avatarUrl` 至少传一个。

## 返回参数

| 参数名 | 类型 | 备注 |
| --- | --- | --- |
| `result.rCode` | String | `0` 成功，`1` 失败 |
| `result.msg` | String | 返回说明 |
| `id` | Long | 用户数据库主键 ID |
| `userId` | String | 用户可见 ID |
| `nickname` | String | 修改后的昵称 |
| `avatarUrl` | String | 修改后的头像 URL |

## 请求示例

```json
{
  "nickname": "lingo",
  "avatarUrl": "http://159.75.140.18:9000/lingolens/lingolens_user_10000001/avatar/b.png"
}
```

## 成功响应示例

```json
{
  "result": {
    "rCode": "0",
    "msg": "success"
  },
  "id": 100000000000001,
  "userId": "10000001",
  "nickname": "lingo",
  "avatarUrl": "http://159.75.140.18:9000/lingolens/lingolens_user_10000001/avatar/b.png"
}
```

## 备注

- 修改昵称时会使用 Redis 分布式锁：`lock:user:update:nickname:{nickname}`。
- 昵称重复时返回失败，不抛给前端系统异常。
