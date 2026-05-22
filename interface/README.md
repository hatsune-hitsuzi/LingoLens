# LingoLens 接口文档索引

## 通用

| 模块 | 接口 | 方法 | 路径 | 文档 |
| --- | --- | --- | --- | --- |
| 通用 | 统一响应格式 | - | - | [response-format.md](response-format.md) |
| 移动端 | Flutter 联调记录 | - | - | [mobile-api-integration.md](mobile-api-integration.md) |

## 用户

| 模块 | 接口 | 方法 | 路径 | 文档 |
| --- | --- | --- | --- | --- |
| 用户 | 注册 | POST | `/lingolens/user/register` | [user-register.md](user-register.md) |
| 用户 | 登录 | POST | `/lingolens/user/login` | [user-login.md](user-login.md) |
| 用户 | 退出登录 | POST | `/api/user/logout` | [user-logout.md](user-logout.md) |
| 用户 | 个人信息查询 | POST | `/api/user/profile/info` | [user-profile-info.md](user-profile-info.md) |
| 用户 | 个人信息修改 | POST | `/api/user/profile/update` | [user-profile-update.md](user-profile-update.md) |
| 用户 | 密码修改 | POST | `/api/user/password/update` | [user-password-update.md](user-password-update.md) |
| 用户 | 偏好查询 | POST | `/api/user/preference/info` | [user-preference-info.md](user-preference-info.md) |
| 用户 | 偏好修改 | POST | `/api/user/preference/update` | [user-preference-update.md](user-preference-update.md) |

## OSS

| 模块 | 接口 | 方法 | 路径 | 文档 |
| --- | --- | --- | --- | --- |
| OSS | 通用上传 | POST | `/lingolens/oss/upload` | [oss-upload.md](oss-upload.md) |

## 首页和菜单

| 模块 | 接口 | 方法 | 路径 | 文档 |
| --- | --- | --- | --- | --- |
| 首页 | 首页信息 | POST | `/api/home/index` | [home-index.md](home-index.md) |
| App | 可见菜单查询 | POST | `/api/app/menu/visible-list` | [app-menu-visible-list.md](app-menu-visible-list.md) |
| 后台 | 菜单新增 | POST | `/api/admin/menu/create` | [admin-menu-create.md](admin-menu-create.md) |
| 后台 | 菜单修改 | POST | `/api/admin/menu/update` | [admin-menu-update.md](admin-menu-update.md) |
| 后台 | 菜单删除 | POST | `/api/admin/menu/delete` | [admin-menu-delete.md](admin-menu-delete.md) |
| 后台 | 菜单详情 | POST | `/api/admin/menu/detail` | [admin-menu-detail.md](admin-menu-detail.md) |
| 后台 | 菜单分页 | POST | `/api/admin/menu/list` | [admin-menu-list.md](admin-menu-list.md) |
| 后台 | 用户 ID 查询主键 | POST | `/api/admin/user/query-by-user-id` | [admin-user-query-by-user-id.md](admin-user-query-by-user-id.md) |

## Lens

| 模块 | 接口 | 方法 | 路径 | 文档 |
| --- | --- | --- | --- | --- |
| Lens | 场景识别 | POST | `/api/lens/recognize` | [lens-recognize.md](lens-recognize.md) |
| Lens | 单词卡详情 | POST | `/api/lens/word/detail` | [lens-word-detail.md](lens-word-detail.md) |

## 词库

| 模块 | 接口 | 方法 | 路径 | 文档 |
| --- | --- | --- | --- | --- |
| 词库 | 词库列表 | POST | `/api/word-book/list` | [wordbook-list.md](wordbook-list.md) |
| 词库 | 词库详情 | POST | `/api/word-book/detail` | [wordbook-detail.md](wordbook-detail.md) |
| 词库 | 词库单词分页 | POST | `/api/word-book/word/page` | [wordbook-word-page.md](wordbook-word-page.md) |

## 记忆宫殿

| 模块 | 接口 | 方法 | 路径 | 文档 |
| --- | --- | --- | --- | --- |
| 记忆宫殿 | 加入单词 | POST | `/api/memory-palace/word/add` | [memory-palace-word-add.md](memory-palace-word-add.md) |
| 记忆宫殿 | 宫殿列表 | POST | `/api/memory-palace/list` | [memory-palace-list.md](memory-palace-list.md) |
| 记忆宫殿 | 宫殿单词分页 | POST | `/api/memory-palace/word/page` | [memory-palace-word-page.md](memory-palace-word-page.md) |

## UI 规划

| 模块 | 名称 | 文档 |
| --- | --- | --- |
| UI | 当前移动端 UI 所需后端接口 | [current-ui-required-apis.md](current-ui-required-apis.md) |
