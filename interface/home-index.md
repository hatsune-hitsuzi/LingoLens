# 首页信息接口

## 基本信息

| 字段 | 值 |
| --- | --- |
| 接口功能 | 获取当前登录用户首页信息，并返回当前页面可见 MenuItem 供移动端渲染 |
| 请求方式 | POST |
| 请求路径 | `/api/home/index` |
| Content-Type | `application/json` |
| 鉴权 | 需要 Token |
| Service | `com.hatsune.lingolens.module.home.service.HomeService#index` |

## Header 参数

| 参数 | 类型 | 必填 | 备注 |
| --- | --- | --- | --- |
| `Authorization` | String | 否 | 标准格式：`Bearer {token}` |
| `token` | String | 否 | 兼容旧调用方式，直接传 Token |

## 请求参数

| 参数 | 类型 | 必填 | 备注 |
| --- | --- | --- | --- |
| `pageCode` | String | 否 | 需要获取可见菜单的页面编码，默认 `profile` |
| `menuType` | Integer | 否 | 菜单类型过滤条件：1 菜单入口，2 Banner |

## 返回参数

| 参数 | 类型 | 备注 |
| --- | --- | --- |
| `result.rCode` | String | `0` 成功，`1` 失败 |
| `result.msg` | String | 返回信息 |
| `id` | Long | 用户主键 ID，后端内部使用 |
| `userId` | String | 用户可见 ID |
| `nickname` | String | 用户昵称 |
| `avatarUrl` | String | 用户头像 URL |
| `todayNewWordCount` | Integer | 今日计划新学单词数 |
| `todayReviewWordCount` | Integer | 今日计划复习单词数 |
| `learnedWordCount` | Integer | 累计已学习单词数 |
| `streakDays` | Integer | 连续学习天数 |
| `currentBookName` | String | 当前学习词库名称 |
| `todayFinished` | Boolean | 今日学习任务是否已完成 |
| `menuItems` | Array | 当前页面对移动端可见的菜单和 Banner 配置 |
| `menuItems[].menuId` | Long | 菜单配置主键 ID |
| `menuItems[].pageCode` | String | 页面编码 |
| `menuItems[].menuCode` | String | 菜单编码 |
| `menuItems[].menuName` | String | 展示名称 |
| `menuItems[].menuType` | Integer | 类型：1 菜单入口，2 Banner |
| `menuItems[].iconUrl` | String | 菜单图标 URL |
| `menuItems[].imageUrl` | String | Banner 或运营图片 URL |
| `menuItems[].jumpType` | Integer | 跳转类型：0 不跳转，1 App 路由，2 H5 链接 |
| `menuItems[].jumpUrl` | String | 跳转地址 |
| `menuItems[].visibleStatus` | Integer | 可见状态，固定返回 1 |
| `menuItems[].sortNo` | Integer | 排序号，越小越靠前 |
| `menuItems[].remark` | String | 后台备注 |

## 请求示例

```json
{
  "pageCode": "profile"
}
```

## 成功响应示例

```json
{
  "result": {
    "rCode": "0",
    "msg": "success"
  },
  "id": 1779349698057,
  "userId": "10000001",
  "nickname": "hatsune",
  "avatarUrl": "http://159.75.140.18:9000/lingolens/avatar/a.png",
  "todayNewWordCount": 20,
  "todayReviewWordCount": 40,
  "learnedWordCount": 0,
  "streakDays": 0,
  "currentBookName": "LingoLens Core Vocabulary",
  "todayFinished": false,
  "menuItems": [
    {
      "menuId": 9100000000001,
      "pageCode": "profile",
      "menuCode": "profile_info",
      "menuName": "个人栏",
      "menuType": 1,
      "iconUrl": "",
      "imageUrl": "",
      "jumpType": 1,
      "jumpUrl": "/profile/info",
      "visibleStatus": 1,
      "sortNo": 10,
      "remark": "个人信息入口"
    }
  ]
}
```

## 失败响应示例

```json
{
  "result": {
    "rCode": "1",
    "msg": "login expired, please login again"
  }
}
```
