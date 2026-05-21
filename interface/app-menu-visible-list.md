# 移动端可见菜单查询接口

## 基本信息

| 字段 | 值 |
| --- | --- |
| 接口功能 | 移动端查询指定页面可见的菜单和 Banner 配置 |
| 请求方式 | POST |
| 请求路径 | `/api/app/menu/visible-list` |
| Content-Type | `application/json` |
| 鉴权 | 需要 Token |
| Service | `com.hatsune.lingolens.module.admin.service.AppMenuService#visibleList` |

## 请求参数

| 参数 | 类型 | 必填 | 备注 |
| --- | --- | --- | --- |
| `pageCode` | String | 否 | 页面编码，默认 `profile` |
| `menuType` | Integer | 否 | 类型：1 菜单，2 Banner |

## 返回参数

| 参数 | 类型 | 备注 |
| --- | --- | --- |
| `result.rCode` | String | `0` 成功，`1` 失败 |
| `result.msg` | String | 返回信息 |
| `menus` | Array | 可见菜单列表，按 `sortNo ASC, id DESC` 排序 |

## 请求示例

```json
{
  "pageCode": "profile"
}
```
