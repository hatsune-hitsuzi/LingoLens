# OSS 上传接口

## 1. 公开图床单点上传

| 项目 | 说明 |
| --- | --- |
| 接口名称 | 公开图床单点上传 |
| 请求方式 | POST |
| 请求路径 | `/lingolens/oss/image/upload` |
| Content-Type | `application/json` |
| 是否需要登录 | 否 |
| Service | `com.hatsune.lingolens.module.oss.service.OssUploadService#uploadPublicImage` |

### 业务说明

这个接口用于图床场景。客户端只需要把图片上传到这个单点接口，后端会写入 MinIO 的公共图床目录，并返回可被所有人直接访问的 URL。

公共图床对象固定写入：

```text
public/image-hosting/{uuid}.{ext}
```

存储层会确保 `public/*` 前缀具有 `s3:GetObject` 公共读策略。公共前缀可通过 `lingolens.oss.minio.public-read-object-prefix` 配置，默认是 `public`。

### 请求参数

请求体为 JSON 字符串，Service 层统一按 `String requestJson` 接收后使用 fastjson2 反序列化。

| 参数名 | 类型 | 必填 | 备注 |
| --- | --- | --- | --- |
| `fileBase64` | String | 是 | 图片 Base64 内容，支持纯 Base64，也支持 `data:image/png;base64,...` 格式 |
| `fileName` | String | 否 | 原始文件名，最大 128 字符；未传 `contentType` 时可用于推断图片类型 |
| `contentType` | String | 否 | 图片 MIME 类型，最大 64 字符；支持 `image/png`、`image/jpeg`、`image/webp`、`image/gif` |

### 返回参数

| 参数名 | 类型 | 备注 |
| --- | --- | --- |
| `result.rCode` | String | `0` 成功，`1` 失败 |
| `result.msg` | String | 返回说明 |
| `url` | String | 上传成功后的公开访问地址 |

### 请求示例

```json
{
  "fileBase64": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...",
  "fileName": "cover.png",
  "contentType": "image/png"
}
```

### 成功响应示例

```json
{
  "result": {
    "rCode": "0",
    "msg": "success"
  },
  "url": "http://159.75.140.18:9000/lingolens/public/image-hosting/xxx.png"
}
```

### 失败响应示例

```json
{
  "result": {
    "rCode": "1",
    "msg": "only png, jpg, jpeg, webp, gif images are supported"
  }
}
```

## 2. OSS 通用上传

| 项目 | 说明 |
| --- | --- |
| 接口名称 | OSS 通用上传 |
| 请求方式 | POST |
| 请求路径 | `/lingolens/oss/upload` |
| Content-Type | `application/json` |
| 是否需要登录 | 否；带 Token 时归属到当前用户，不带 Token 时走匿名前缀 |
| Service | `com.hatsune.lingolens.module.oss.service.OssUploadService#upload` |

### 请求头

| 参数名 | 类型 | 必填 | 备注 |
| --- | --- | --- | --- |
| `Authorization` | String | 否 | 推荐格式：`Bearer {token}` |
| `token` | String | 否 | 兼容写法，直接传 Token |

### 请求参数

请求体为 JSON 字符串，Service 层统一按 `String requestJson` 接收后使用 fastjson2 反序列化。

| 参数名 | 类型 | 必填 | 备注 |
| --- | --- | --- | --- |
| `fileBase64` | String | 是 | 文件 Base64 内容，支持纯 Base64，也支持 `data:image/png;base64,...` 格式 |
| `fileName` | String | 否 | 原始文件名，最大 128 字符；用于推断文件后缀 |
| `contentType` | String | 否 | 文件 MIME 类型，最大 64 字符 |
| `bizType` | String | 否 | 推荐字段，业务分类目录；头像建议传 `avatar` |
| `objectPrefix` | String | 否 | 兼容旧字段，未传 `bizType` 时使用 |

### 返回参数

| 参数名 | 类型 | 备注 |
| --- | --- | --- |
| `result.rCode` | String | `0` 成功，`1` 失败 |
| `result.msg` | String | 返回说明 |
| `url` | String | 上传成功后的公网访问地址 |

### 对象命名规则

| 场景 | 对象名前缀 |
| --- | --- |
| 带有效 Token | `lingolens_user_{userId}/{bizType}/` |
| 未带 Token 或 Token 无效 | `lingolens_user_anonymous_{fileHash}/{bizType}/` |

### 成功响应示例

```json
{
  "result": {
    "rCode": "0",
    "msg": "success"
  },
  "url": "http://159.75.140.18:9000/lingolens/lingolens_user_10000001/avatar/xxx.png"
}
```

### 失败响应示例

```json
{
  "result": {
    "rCode": "1",
    "msg": "file must not be blank"
  }
}
```
