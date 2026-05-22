# 用户偏好查询

## 基本信息

| 字段 | 内容 |
| --- | --- |
| 请求路径 | `/api/user/preference/info` |
| 请求方式 | POST |
| Content-Type | `application/json` |
| 鉴权 | 需要 Token |
| Service | `com.hatsune.lingolens.module.user.service.UserPreferenceService#info` |

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
| `sourceLanguage` | String | 源语言编码，例如 `en` |
| `targetLanguage` | String | 目标语言编码，例如 `ja`、`zh-CN` |
| `themeMode` | String | 主题模式：`system`、`light`、`dark` |

## 响应示例

```json
{
  "result": {
    "rCode": "0",
    "msg": "success"
  },
  "sourceLanguage": "en",
  "targetLanguage": "ja",
  "themeMode": "system"
}
```
