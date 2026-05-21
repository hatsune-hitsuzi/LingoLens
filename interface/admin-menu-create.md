# 后台菜单新增接口

## 基本信息

| 字段 | 值 |
| --- | --- |
| 接口功能 | 后台新增 App 页面菜单或 Banner 配置 |
| 请求方式 | POST |
| 请求路径 | `/api/admin/menu/create` |
| Content-Type | `application/json` |
| 鉴权 | 需要 Token |
| Service | `com.hatsune.lingolens.module.admin.service.AdminMenuService#create` |

## 请求参数

| 参数 | 类型 | 必填 | 备注 |
| --- | --- | --- | --- |
| `pageCode` | String | 是 | 页面编码，例如 `profile` |
| `menuCode` | String | 是 | 菜单编码，同一页面内唯一 |
| `menuName` | String | 是 | 菜单展示名称 |
| `menuType` | Integer | 否 | 类型：1 菜单，2 Banner，默认 1 |
| `iconUrl` | String | 否 | 图标 URL |
| `imageUrl` | String | 否 | Banner 或图片 URL |
| `jumpType` | Integer | 否 | 跳转类型：0 不跳转，1 App 路由，2 H5 链接，默认 0 |
| `jumpUrl` | String | 否 | 跳转地址 |
| `visibleStatus` | Integer | 否 | 是否可见：1 可见，0 隐藏，默认 1 |
| `sortNo` | Integer | 否 | 排序号，越小越靠前 |
| `remark` | String | 否 | 备注 |

## 返回参数

| 参数 | 类型 | 备注 |
| --- | --- | --- |
| `result.rCode` | String | `0` 成功，`1` 失败 |
| `result.msg` | String | 返回信息 |
| `menu` | Object | 菜单配置 |

## 请求示例

```json
{
  "pageCode": "profile",
  "menuCode": "word_entry",
  "menuName": "背词入口",
  "menuType": 1,
  "jumpType": 1,
  "jumpUrl": "/word/study",
  "visibleStatus": 1,
  "sortNo": 30
}
```
