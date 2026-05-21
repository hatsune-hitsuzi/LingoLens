# 后台用户主键查询接口

## 基本信息

| 字段 | 值 |
| --- | --- |
| 接口功能 | 后台通过用户可见 userId 查询后端用户主键 id |
| 请求方式 | POST |
| 请求路径 | `/api/admin/user/query-by-user-id` |
| Content-Type | `application/json` |
| 鉴权 | 需要 Token |
| Service | `com.hatsune.lingolens.module.admin.service.AdminUserService#queryByUserId` |

## 请求参数

| 参数 | 类型 | 必填 | 备注 |
| --- | --- | --- | --- |
| `userId` | String | 是 | 用户可见 ID，例如 `10000001` |

## 返回参数

| 参数 | 类型 | 备注 |
| --- | --- | --- |
| `result.rCode` | String | `0` 成功，`1` 失败 |
| `result.msg` | String | 返回信息 |
| `user.id` | Long | 用户主键 ID，后端内部操作使用 |
| `user.userId` | String | 用户可见 ID |
| `user.nickname` | String | 用户昵称 |
| `user.avatarUrl` | String | 用户头像 URL |
| `user.createTime` | String | 创建时间 |
| `user.updateTime` | String | 更新时间 |

## 请求示例

```json
{
  "userId": "10000001"
}
```

## 成功响应示例

```json
{
  "result": {
    "rCode": "0",
    "msg": "success"
  },
  "user": {
    "id": 123456789,
    "userId": "10000001",
    "nickname": "hatsune",
    "avatarUrl": "http://159.75.140.18:9000/lingolens/avatar/a.png"
  }
}
```
