# 后台菜单删除接口

## 基本信息

| 字段 | 值 |
| --- | --- |
| 接口功能 | 后台删除 App 页面菜单或 Banner 配置 |
| 请求方式 | POST |
| 请求路径 | `/api/admin/menu/delete` |
| Content-Type | `application/json` |
| 鉴权 | 需要 Token |
| Service | `com.hatsune.lingolens.module.admin.service.AdminMenuService#delete` |

## 请求参数

| 参数 | 类型 | 必填 | 备注 |
| --- | --- | --- | --- |
| `menuId` | Long | 是 | 菜单配置主键 ID |

## 返回参数

| 参数 | 类型 | 备注 |
| --- | --- | --- |
| `result.rCode` | String | `0` 成功，`1` 失败 |
| `result.msg` | String | 返回信息 |

## 请求示例

```json
{
  "menuId": 9100000000003
}
```
