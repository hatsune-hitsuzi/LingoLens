# 用户密码修改接口

## 基本信息

| 项目 | 说明 |
| --- | --- |
| 接口名称 | 用户密码修改 |
| 请求方式 | POST |
| 请求路径 | `/api/user/password/update` |
| Content-Type | `application/json` |
| 是否需要登录 | 是 |
| Service | `com.hatsune.lingolens.module.user.service.UserAccountService#updatePassword` |

## 请求头

| 参数名 | 类型 | 必填 | 备注 |
| --- | --- | --- | --- |
| `Authorization` | String | 否 | 推荐格式：`Bearer {token}` |
| `token` | String | 否 | 兼容写法，直接传 Token |

## 请求参数

| 参数名 | 类型 | 必填 | 备注 |
| --- | --- | --- | --- |
| `oldPassword` | String | 是 | 当前密码，长度 8-72 |
| `newPassword` | String | 是 | 新密码，长度 8-72；不能和旧密码相同 |
| `confirmPassword` | String | 是 | 确认新密码，需要和 `newPassword` 一致 |

## 返回参数

| 参数名 | 类型 | 备注 |
| --- | --- | --- |
| `result.rCode` | String | `0` 成功，`1` 失败 |
| `result.msg` | String | 返回说明 |

## 请求示例

```json
{
  "oldPassword": "password123",
  "newPassword": "password456",
  "confirmPassword": "password456"
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

## 备注

- 修改密码前会校验旧密码。
- 修改成功后会删除当前 Token，前端需要重新登录。
