# LingoLens 接口文档索引

## 约定

- 所有新增 HTTP 接口都需要在 `interface/` 目录维护独立 `.md` 协议文档。
- 所有接口统一返回 `result.rCode` 和 `result.msg`，详见 [response-format.md](response-format.md)。
- Service 入参统一使用 `String requestJson`，由 ServiceImpl 使用 JSON 反序列化。

## 接口列表

| 模块 | 接口 | 方法 | 路径 | 文档 |
| --- | --- | --- | --- | --- |
| 用户 | 注册 | POST | `/lingolens/user/register` | [user-register.md](user-register.md) |
| 用户 | 登录 | POST | `/lingolens/user/login` | [user-login.md](user-login.md) |
| 用户 | 退出登录 | POST | `/api/user/logout` | [user-logout.md](user-logout.md) |
| 用户 | 个人信息查询 | POST | `/api/user/profile/info` | [user-profile-info.md](user-profile-info.md) |
| 用户 | 个人信息修改 | POST | `/api/user/profile/update` | [user-profile-update.md](user-profile-update.md) |
| 用户 | 密码修改 | POST | `/api/user/password/update` | [user-password-update.md](user-password-update.md) |
| OSS | 通用上传 | POST | `/lingolens/oss/upload` | [oss-upload.md](oss-upload.md) |
| 首页 | 首页信息 | POST | `/api/home/index` | [home-index.md](home-index.md) |
| 词库 | 词库列表 | POST | `/api/word-book/list` | [wordbook-list.md](wordbook-list.md) |
| 词库 | 词库详情 | POST | `/api/word-book/detail` | [wordbook-detail.md](wordbook-detail.md) |
| 词库 | 词库单词分页 | POST | `/api/word-book/word/page` | [wordbook-word-page.md](wordbook-word-page.md) |
| 后台 | 菜单新增 | POST | `/api/admin/menu/create` | [admin-menu-create.md](admin-menu-create.md) |
| 后台 | 菜单修改 | POST | `/api/admin/menu/update` | [admin-menu-update.md](admin-menu-update.md) |
| 后台 | 菜单删除 | POST | `/api/admin/menu/delete` | [admin-menu-delete.md](admin-menu-delete.md) |
| 后台 | 菜单详情 | POST | `/api/admin/menu/detail` | [admin-menu-detail.md](admin-menu-detail.md) |
| 后台 | 菜单分页 | POST | `/api/admin/menu/list` | [admin-menu-list.md](admin-menu-list.md) |
| 后台 | 用户主键查询 | POST | `/api/admin/user/query-by-user-id` | [admin-user-query-by-user-id.md](admin-user-query-by-user-id.md) |
| App | 可见菜单查询 | POST | `/api/app/menu/visible-list` | [app-menu-visible-list.md](app-menu-visible-list.md) |
