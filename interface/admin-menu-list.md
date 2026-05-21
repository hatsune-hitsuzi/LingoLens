# 后台菜单分页接口

## 基本信息

| 字段 | 值 |
| --- | --- |
| 接口功能 | 后台分页查询 App 页面菜单或 Banner 配置 |
| 请求方式 | POST |
| 请求路径 | `/api/admin/menu/list` |
| Content-Type | `application/json` |
| 鉴权 | 需要 Token |
| Service | `com.hatsune.lingolens.module.admin.service.AdminMenuService#list` |

## 请求参数

| 参数 | 类型 | 必填 | 备注 |
| --- | --- | --- | --- |
| `pageNumber` | Integer | 否 | 页码，默认 1 |
| `pageSize` | Integer | 否 | 每页数量，默认 20，最大 100 |
| `keyword` | String | 否 | 按菜单编码、名称、备注模糊搜索 |
| `pageCode` | String | 否 | 页面编码 |
| `menuType` | Integer | 否 | 类型：1 菜单，2 Banner |
| `visibleStatus` | Integer | 否 | 是否可见：1 可见，0 隐藏 |

## 返回参数

| 参数 | 类型 | 备注 |
| --- | --- | --- |
| `result.rCode` | String | `0` 成功，`1` 失败 |
| `result.msg` | String | 返回信息 |
| `pageNumber` | Integer | 当前页码 |
| `pageSize` | Integer | 每页数量 |
| `total` | Integer | 总数量 |
| `menus` | Array | 菜单配置列表 |

## 请求示例

```json
{
  "pageNumber": 1,
  "pageSize": 20,
  "pageCode": "profile",
  "visibleStatus": 1
}
```
