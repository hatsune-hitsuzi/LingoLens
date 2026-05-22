# 当前移动端 UI 所需后端接口清单

## 页面交互主链路

```text
登录 -> 首页 -> 打开 Lens -> AI 识别物体 -> AI 生成单词卡 -> 加入记忆宫殿 -> 留在 AR 页面继续扫描
```

个人中心链路：

```text
我的 -> 读取用户资料/学习进度/后台配置菜单 -> 修改个人信息 -> 上传头像 -> 保存资料 -> 退出登录
```

## 已实现且可直接联调

| 能力 | 接口 | 文档 |
| --- | --- | --- |
| 注册 | `POST /lingolens/user/register` | [user-register.md](user-register.md) |
| 登录 | `POST /lingolens/user/login` | [user-login.md](user-login.md) |
| 退出登录 | `POST /api/user/logout` | [user-logout.md](user-logout.md) |
| 查询个人信息 | `POST /api/user/profile/info` | [user-profile-info.md](user-profile-info.md) |
| 修改个人信息 | `POST /api/user/profile/update` | [user-profile-update.md](user-profile-update.md) |
| 修改密码 | `POST /api/user/password/update` | [user-password-update.md](user-password-update.md) |
| 用户偏好查询 | `POST /api/user/preference/info` | [user-preference-info.md](user-preference-info.md) |
| 用户偏好修改 | `POST /api/user/preference/update` | [user-preference-update.md](user-preference-update.md) |
| 通用上传 | `POST /lingolens/oss/upload` | [oss-upload.md](oss-upload.md) |
| 首页信息 | `POST /api/home/index` | [home-index.md](home-index.md) |
| 可见菜单 | `POST /api/app/menu/visible-list` | [app-menu-visible-list.md](app-menu-visible-list.md) |
| Lens 场景识别 | `POST /api/lens/recognize` | [lens-recognize.md](lens-recognize.md) |
| Lens 单词卡详情 | `POST /api/lens/word/detail` | [lens-word-detail.md](lens-word-detail.md) |
| 词库列表 | `POST /api/word-book/list` | [wordbook-list.md](wordbook-list.md) |
| 词库详情 | `POST /api/word-book/detail` | [wordbook-detail.md](wordbook-detail.md) |
| 词库单词分页 | `POST /api/word-book/word/page` | [wordbook-word-page.md](wordbook-word-page.md) |
| 加入记忆宫殿 | `POST /api/memory-palace/word/add` | [memory-palace-word-add.md](memory-palace-word-add.md) |
| 记忆宫殿列表 | `POST /api/memory-palace/list` | [memory-palace-list.md](memory-palace-list.md) |
| 记忆宫殿单词分页 | `POST /api/memory-palace/word/page` | [memory-palace-word-page.md](memory-palace-word-page.md) |

## Lens 当前实现说明

`/api/lens/recognize` 和 `/api/lens/word/detail` 已提供演示级实现，返回 `sample.html` 中使用的 Mug 与 Keyboard 单词气泡及单词卡。后续接入真实 AI/词典服务时，替换 `LensManager` 内部识别与释义逻辑即可，接口合同不需要变更。
