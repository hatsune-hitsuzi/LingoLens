# OSS 通用上传接口

## 基本信息

| 项目 | 说明 |
| --- | --- |
| 接口名称 | OSS 通用上传 |
| 请求方式 | POST |
| 请求路径 | `/lingolens/oss/upload` |
| Content-Type | `application/json` |
| 是否需要登录 | 否；带 Token 时归属到当前用户，不带 Token 时走匿名前缀 |
| Service | `com.hatsune.lingolens.module.oss.service.OssUploadService#upload` |

## 请求头

| 参数名 | 类型 | 必填 | 备注 |
| --- | --- | --- | --- |
| `Authorization` | String | 否 | 推荐格式：`Bearer {token}` |
| `token` | String | 否 | 兼容写法，直接传 Token |

## 请求参数

请求体为 JSON 字符串，Service 层统一按 `String requestJson` 接收后使用 fastjson2 反序列化。

| 参数名 | 类型 | 必填 | 备注 |
| --- | --- | --- | --- |
| `fileBase64` | String | 是 | 文件 Base64 内容，支持纯 Base64，也支持 `data:image/png;base64,...` 格式 |
| `fileName` | String | 否 | 原始文件名，最大 128 字符；用于推断文件后缀 |
| `contentType` | String | 否 | 文件 MIME 类型，最大 64 字符 |
| `bizType` | String | 否 | 推荐字段，业务分类目录；头像建议传 `avatar` |
| `objectPrefix` | String | 否 | 兼容旧字段，未传 `bizType` 时使用 |

## 返回参数

| 参数名 | 类型 | 备注 |
| --- | --- | --- |
| `result.rCode` | String | `0` 成功，`1` 失败 |
| `result.msg` | String | 返回说明 |
| `url` | String | 上传成功后的公网访问地址 |

## 对象命名规则

| 场景 | 对象名前缀 |
| --- | --- |
| 带有效 Token | `lingolens_user_{userId}/{bizType}/` |
| 未带 Token 或 Token 无效 | `lingolens_user_anonymous_{fileHash}/{bizType}/` |

## 成功响应示例

```json
{
  "result": {
    "rCode": "0",
    "msg": "success"
  },
  "url": "http://159.75.140.18:9000/lingolens/lingolens_user_10000001/avatar/xxx.png"
}
```

## 失败响应示例

```json
{
  "result": {
    "rCode": "1",
    "msg": "file must not be blank"
  }
}
```
