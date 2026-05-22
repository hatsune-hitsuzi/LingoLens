# 移动端接口联调记录

## 移动端配置

Flutter 默认使用：

```dart
const String.fromEnvironment(
  'LINGOLENS_API_BASE_URL',
  defaultValue: 'http://10.0.2.2:8080',
)
```

Android 模拟器访问本机后端可直接使用默认值；iOS、桌面或真机联调时，通过 `--dart-define=LINGOLENS_API_BASE_URL=http://你的后端地址` 覆盖。

## Flutter 调用封装

| 功能 | Flutter 封装 | 后端接口 |
| --- | --- | --- |
| 注册 | `AuthApi.register` | `POST /lingolens/user/register` |
| 登录 | `AuthApi.login` | `POST /lingolens/user/login` |
| 退出登录 | `AuthApi.logout` | `POST /api/user/logout` |
| 首页信息 | `HomeApi.index` | `POST /api/home/index` |
| Lens 场景识别 | `LensApi.recognize` | `POST /api/lens/recognize` |
| Lens 单词卡详情 | `LensApi.wordDetail` | `POST /api/lens/word/detail` |
| 加入记忆宫殿 | `MemoryPalaceApi.addWord` | `POST /api/memory-palace/word/add` |
| 记忆宫殿列表 | `MemoryPalaceApi.list` | `POST /api/memory-palace/list` |
| 记忆宫殿单词分页 | `MemoryPalaceApi.pageWords` | `POST /api/memory-palace/word/page` |
| 查询个人信息 | `ProfileApi.info` | `POST /api/user/profile/info` |
| 修改个人信息 | `ProfileApi.update` | `POST /api/user/profile/update` |
| 修改密码 | `ProfileApi.updatePassword` | `POST /api/user/password/update` |
| 查询偏好 | `ProfileApi.preferenceInfo` | `POST /api/user/preference/info` |
| 修改偏好 | `ProfileApi.updatePreference` | `POST /api/user/preference/update` |
| 词库列表 | `WordBookApi.list` | `POST /api/word-book/list` |
| 词库详情 | `WordBookApi.detail` | `POST /api/word-book/detail` |
| 词库单词分页 | `WordBookApi.pageWords` | `POST /api/word-book/word/page` |
| 通用上传 | `OssApi.upload` | `POST /lingolens/oss/upload` |

## 主流程

```text
登录/注册
  -> 保存 token
  -> 首页 Shell
  -> LensApi.recognize 获取气泡
  -> LensApi.wordDetail 获取单词卡
  -> MemoryPalaceApi.addWord 加入记忆宫殿
  -> MemoryPalaceApi.list / pageWords 查看结果
```

所有 `/api/**` 请求由 `ApiClient` 的 token interceptor 自动添加：

```http
Authorization: Bearer <token>
```
