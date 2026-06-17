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
| `sourceLanguage` | String | 学习源语言，例如 `zh-CN`、`ja`、`en` |
| `targetLanguage` | String | 学习目标语言，例如 `en`、`ja`、`zh-CN` |
| `languageDirection` | String | 学习方向，例如 `ja-en` |
| `languageDirectionName` | String | 兼容旧客户端的展示文案，新客户端应使用本地 i18n 渲染 |
| `uiLanguage` | String | App 界面语言，例如 `zh-CN`、`ja`、`en` |
| `themeMode` | String | 主题模式：`system`、`light`、`dark` |
| `languagePairs` | Array | 当前支持的学习语言方向列表 |

## 响应示例

```json
{
  "result": {
    "rCode": "0",
    "msg": "success"
  },
  "sourceLanguage": "ja",
  "targetLanguage": "en",
  "languageDirection": "ja-en",
  "languageDirectionName": "日 -> 英",
  "uiLanguage": "ja",
  "themeMode": "system",
  "languagePairs": [
    {
      "direction": "ja-en",
      "sourceLanguage": "ja",
      "targetLanguage": "en",
      "label": "日 -> 英"
    }
  ]
}
```
