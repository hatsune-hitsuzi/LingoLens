# 首页信息

## 基本信息

| 字段 | 内容 |
| --- | --- |
| 请求路径 | `/api/home/index` |
| 请求方式 | POST |
| Content-Type | `application/json` |
| 鉴权 | 需要 Token |
| Service | `com.hatsune.lingolens.module.home.service.HomeService#index` |

## 请求参数

| 字段 | 类型 | 必填 | 备注 |
| --- | --- | --- | --- |
| `pageCode` | String | 否 | 需要读取的菜单页编码，默认 `profile` |
| `menuType` | Integer | 否 | 菜单类型：1 菜单，2 Banner |

## 响应参数

| 字段 | 类型 | 备注 |
| --- | --- | --- |
| `result.rCode` | String | `0` 成功，`1` 失败 |
| `result.msg` | String | 结果提示 |
| `id` | Long | 用户表主键 ID，后端内部使用 |
| `userId` | String | 用户可见 ID |
| `nickname` | String | 昵称 |
| `avatarUrl` | String | 头像 URL |
| `email` | String | 用户邮箱 |
| `bio` | String | 用户个人简介 |
| `todayNewWordCount` | Integer | 今日新词数 |
| `todayReviewWordCount` | Integer | 今日复习词数 |
| `learnedWordCount` | Integer | 已学习单词数 |
| `streakDays` | Integer | 连续学习天数 |
| `currentBookName` | String | 当前词库名称 |
| `dailyGoalWordCount` | Integer | 今日目标单词数 |
| `todayFinishedWordCount` | Integer | 今日已完成单词数 |
| `scannedWordCount` | Integer | AR 扫描词本总词数 |
| `todayFinished` | Boolean | 今日是否完成 |
| `menuItems` | Array | 当前页可见菜单和 Banner |

## 请求示例

```json
{
  "pageCode": "profile"
}
```

## 响应示例

```json
{
  "result": {
    "rCode": "0",
    "msg": "success"
  },
  "id": 100000000000001,
  "userId": "10000001",
  "nickname": "hatsune",
  "avatarUrl": "http://159.75.140.18:9000/lingolens/avatar.png",
  "email": "hatsune@example.com",
  "bio": "用现实场景记住新单词。",
  "todayNewWordCount": 0,
  "todayReviewWordCount": 0,
  "learnedWordCount": 128,
  "streakDays": 0,
  "currentBookName": "",
  "dailyGoalWordCount": 50,
  "todayFinishedWordCount": 0,
  "scannedWordCount": 128,
  "todayFinished": false,
  "menuItems": []
}
```
