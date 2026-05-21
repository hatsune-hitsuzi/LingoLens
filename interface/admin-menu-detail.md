# 后台菜单详情接口

## 基本信息

| 字段 | 值 |
| --- | --- |
| 接口功能 | 后台查询单个 App 页面菜单或 Banner 配置详情 |
| 请求方式 | POST |
| 请求路径 | `/api/admin/menu/detail` |
| Content-Type | `application/json` |
| 鉴权 | 需要 Token |
| Service | `com.hatsune.lingolens.module.admin.service.AdminMenuService#detail` |

## 请求参数

| 参数 | 类型 | 必填 | 备注 |
| --- | --- | --- | --- |
| `menuId` | Long | 是 | 菜单配置主键 ID |

## 返回参数

| 参数 | 类型 | 备注 |
| --- | --- | --- |
| `result.rCode` | String | `0` 成功，`1` 失败 |
| `result.msg` | String | 返回信息 |
| `menu.menuId` | Long | 菜单配置主键 ID |
| `menu.pageCode` | String | 页面编码 |
| `menu.menuCode` | String | 菜单编码 |
| `menu.menuName` | String | 菜单展示名称 |
| `menu.menuType` | Integer | 类型：1 菜单，2 Banner |
| `menu.visibleStatus` | Integer | 是否可见 |
| `menu.sortNo` | Integer | 排序号 |

## 请求示例

```json
{
  "menuId": 9100000000003
}
```
