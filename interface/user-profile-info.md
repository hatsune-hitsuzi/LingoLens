# 用户资料查询接口

## 基本信息

| 项目 | 说明 |
| --- | --- |
| 接口名称 | 用户资料查询 |
| 请求方式 | POST |
| 请求路径 | `/api/user/profile/info` |
| Content-Type | `application/json` |
| 是否需要登录 | 是 |
| Service | `com.hatsune.lingolens.module.user.service.UserAccountService#profileInfo` |

## 请求头

| 参数名 | 类型 | 必填 | 备注 |
| --- | --- | --- | --- |
| `Authorization` | String | 否 | 推荐格式：`Bearer {token}` |
| `token` | String | 否 | 兼容写法，直接传 Token |

## 请求参数

当前接口暂无业务入参，建议传 `{}`。

## 返回参数

| 参数名 | 类型 | 备注 |
| --- | --- | --- |
| `result.rCode` | String | `0` 成功，`1` 失败 |
| `result.msg` | String | 返回说明 |
| `id` | Long | 用户数据库主键 ID |
| `userId` | String | 用户可见 ID |
| `nickname` | String | 用户昵称 |
| `avatarUrl` | String | 用户头像 URL |

## 成功响应示例

```json
{
  "result": {
    "rCode": "0",
    "msg": "success"
  },
  "id": 100000000000001,
  "userId": "10000001",
  "nickname": "hatsune",
  "avatarUrl": "http://159.75.140.18:9000/lingolens/lingolens_user_10000001/avatar/a.png"
}
```
