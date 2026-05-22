# 用户偏好修改

## 基本信息

| 字段 | 内容 |
| --- | --- |
| 请求路径 | `/api/user/preference/update` |
| 请求方式 | POST |
| Content-Type | `application/json` |
| 鉴权 | 需要 Token |
| Service | `com.hatsune.lingolens.module.user.service.UserPreferenceService#update` |

## 请求参数

至少传一个字段。

| 字段 | 类型 | 必填 | 备注 |
| --- | --- | --- | --- |
| `sourceLanguage` | String | 否 | 源语言编码，例如 `en` |
| `targetLanguage` | String | 否 | 目标语言编码，例如 `ja`、`zh-CN` |
| `themeMode` | String | 否 | 主题模式：`system`、`light`、`dark` |

## 响应参数

| 字段 | 类型 | 备注 |
| --- | --- | --- |
| `result.rCode` | String | `0` 成功，`1` 失败 |
| `result.msg` | String | 结果提示 |
| `sourceLanguage` | String | 源语言编码 |
| `targetLanguage` | String | 目标语言编码 |
| `themeMode` | String | 主题模式 |

## 请求示例

```json
{
  "sourceLanguage": "en",
  "targetLanguage": "ja",
  "themeMode": "dark"
}
```
