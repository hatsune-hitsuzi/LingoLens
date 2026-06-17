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
| `languageDirection` | String | 否 | 学习方向，例如 `ja-en`。传它时优先于单独的 source/target |
| `sourceLanguage` | String | 否 | 学习源语言，例如 `zh-CN`、`ja`、`en` |
| `targetLanguage` | String | 否 | 学习目标语言，例如 `en`、`ja`、`zh-CN` |
| `uiLanguage` | String | 否 | App 界面语言，例如 `zh-CN`、`ja`、`en`。默认可跟随源语言 |
| `themeMode` | String | 否 | 主题模式：`system`、`light`、`dark` |

## 响应参数

同 [用户偏好查询](user-preference-info.md)。

## 请求示例

```json
{
  "languageDirection": "ja-en",
  "sourceLanguage": "ja",
  "targetLanguage": "en",
  "uiLanguage": "ja",
  "themeMode": "dark"
}
```

当前内置方向：`zh-CN-en`、`zh-CN-ja`、`ja-zh-CN`、`ja-en`、`en-ja`、`en-zh-CN`。
