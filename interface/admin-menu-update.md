# 后台菜单修改接口

## 基本信息

| 字段 | 值 |
| --- | --- |
| 接口功能 | 后台修改 App 页面菜单或 Banner 配置 |
| 请求方式 | POST |
| 请求路径 | `/api/admin/menu/update` |
| Content-Type | `application/json` |
| 鉴权 | 需要 Token |
| Service | `com.hatsune.lingolens.module.admin.service.AdminMenuService#update` |

## 请求参数

| 参数 | 类型 | 必填 | 备注 |
| --- | --- | --- | --- |
| `menuId` | Long | 是 | 菜单配置主键 ID |
| `pageCode` | String | 否 | 页面编码 |
| `menuCode` | String | 否 | 菜单编码 |
| `menuName` | String | 否 | 菜单展示名称 |
| `menuType` | Integer | 否 | 类型：1 菜单，2 Banner |
| `iconUrl` | String | 否 | 图标 URL，可传空字符串清空 |
| `imageUrl` | String | 否 | 图片 URL，可传空字符串清空 |
| `jumpType` | Integer | 否 | 跳转类型 |
| `jumpUrl` | String | 否 | 跳转地址，可传空字符串清空 |
| `visibleStatus` | Integer | 否 | 是否可见：1 可见，0 隐藏 |
| `sortNo` | Integer | 否 | 排序号 |
| `remark` | String | 否 | 备注，可传空字符串清空 |

## 返回参数

| 参数 | 类型 | 备注 |
| --- | --- | --- |
| `result.rCode` | String | `0` 成功，`1` 失败 |
| `result.msg` | String | 返回信息 |
| `menu` | Object | 修改后的菜单配置 |

## 请求示例

```json
{
  "menuId": 9100000000003,
  "menuName": "开始背词",
  "visibleStatus": 1,
  "sortNo": 10
}
```
