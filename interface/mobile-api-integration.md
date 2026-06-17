# 移动端接口联调记录

## 移动端配置

Flutter 默认使用：

```dart
const String.fromEnvironment(
  'LINGOLENS_API_BASE_URL',
  defaultValue: 'http://10.0.2.2:8080',
)
```

Android 模拟器访问本机后端可直接使用默认值；真机或桌面联调时通过：

```bash
flutter run --dart-define=LINGOLENS_API_BASE_URL=http://你的后端地址:8080
```

## Flutter 封装与后端接口

| 功能 | Flutter 封装 | 后端接口 |
| --- | --- | --- |
| 注册 | `AuthApi.register` | `POST /lingolens/user/register` |
| 登录 | `AuthApi.login` | `POST /lingolens/user/login` |
| 退出登录 | `AuthApi.logout` | `POST /api/user/logout` |
| 首页信息 | `HomeApi.index` | `POST /api/home/index` |
| Lens 场景识别 | `LensApi.recognize` | `POST /api/lens/recognize` |
| Lens 单词详情 | `LensApi.wordDetail` | `POST /api/lens/word/detail` |
| 加入记忆宫殿 | `MemoryPalaceApi.addWord` | `POST /api/memory-palace/word/add` |
| 记忆宫殿列表 | `MemoryPalaceApi.list` | `POST /api/memory-palace/list` |
| 记忆宫殿排序 | `MemoryPalaceApi.reorder` | `POST /api/memory-palace/reorder` |
| 记忆宫殿重命名 | `MemoryPalaceApi.update` | `POST /api/memory-palace/update` |
| 记忆宫殿删除 | `MemoryPalaceApi.delete` | `POST /api/memory-palace/delete` |
| 记忆宫殿单词分页 | `MemoryPalaceApi.pageWords` | `POST /api/memory-palace/word/page` |
| AR 扫描词本分页 | `ArScanApi.pageWords` | `POST /api/ar-scan/word/page` |
| 查询个人信息 | `ProfileApi.info` | `POST /api/user/profile/info` |
| 修改个人信息 | `ProfileApi.update` | `POST /api/user/profile/update` |
| 查询偏好 | `ProfileApi.preferenceInfo` | `POST /api/user/preference/info` |
| 修改偏好 | `ProfileApi.updatePreference` | `POST /api/user/preference/update` |
| 通用上传 | `OssApi.upload` | `POST /lingolens/oss/upload` |
| 今日复习数量 | `ReviewApi.count` | `POST /api/v1/review/count` |
| 今日复习卡片 | `ReviewApi.cards` | `POST /api/v1/review/cards` |
| 批量提交复习结果 | `ReviewApi.submitBatch` | `POST /api/v1/review/answer/batch` |

所有 `/api/**` 请求都由 `ApiClient` 的 token interceptor 自动添加：

```http
Authorization: Bearer <token>
```

## 主流程

```text
登录/注册
  -> 保存 token
  -> 首页 Shell
  -> CameraPreview 常开实时取景
  -> 周期抓取当前帧
  -> LocalYoloObjectDetector 本地识别 detectedTags
  -> OssApi.upload 上传当前帧
  -> LensApi.recognize(sourceType=local-yolo, imageUrl, detectedTags)
  -> 后端按 tags 查 Redis/DB；未命中时按 YAML 配置调用 LLM 生成三语词资产
  -> 如果本地没有 tags，后端可直接用 imageUrl 调 LLM 识别核心物体
  -> LensApi.wordDetail 获取当前语言方向单词卡
  -> MemoryPalaceApi.addWord 加入记忆宫殿
  -> MemoryPalaceApi.list / pageWords 查看结果
```

## AR 扫描词本计数

`POST /api/home/index` 返回的 `scannedWordCount` 来自后端 `ar_scan_words` 表。移动端调用 `MemoryPalaceApi.addWord` 成功后，后端在同一事务中同步写入：

```text
memory_palace_words
ar_scan_words
```

点击“AR 扫描词本”进入完整分页页，调用 `POST /api/ar-scan/word/page` 查询 `ar_scan_words`。

## 今日学习进度

`dailyGoalWordCount` 不再是硬编码 50：

```text
todayFinishedWordCount = review_records 今日已答去重词数
dailyGoalWordCount = todayFinishedWordCount + 今日未答复习词数
```

个人页展示的 `已学 / 今日目标` 完全来自后端数据库。

## 多语言词资产

当前支持：

```text
zh-CN -> en
zh-CN -> ja
ja -> zh-CN
ja -> en
en -> ja
en -> zh-CN
```

移动端启动后由 `LanguagePreferenceScope` 维护全局语言偏好。Lens、复习大厅、AR 扫描词本、记忆宫殿都从同一个上下文读取 `sourceLanguage/targetLanguage`。

后端词资产以 `word_assets.id` 为主轴：

```text
word_assets.id
  -> word_assets.language_payloads JSON
  -> memory_palace_words.word_asset_id
  -> ar_scan_words.word_asset_id
  -> review_records.word_asset_id
```

`targetLanguage` 决定卡片主词、`phonetic`、词性、例句和发音；`sourceLanguage` 决定翻译和例句翻译。英文卡片返回英文 + IPA；日文卡片返回日文 + 罗马音；中文卡片返回中文 + 拼音。

## 发音与 URL 拼接

`word_assets` 只保存公共 `pronunciation_base_url`，每个语言 payload 保存 `pronunciationPath`。后端返回时拼接：

```text
pronunciationUrl = pronunciation_base_url + pronunciationPath
```

示例：

```text
http://dict.youdao.com/dictvoice?audio=monitor&type=2
http://dict.youdao.com/dictvoice?le=jap&audio=%E3%83%A2%E3%83%8B%E3%82%BF%E3%83%BC&type=3
```

## 扫描卡片复习

个人页“今日复习任务”入口调用 `POST /api/v1/review/count` 获取今日剩余数量，并读取 `previewImageUrls` 渲染最新 3 张收藏图片组成的模糊扇形小 logo。进入卡片大厅后调用 `POST /api/v1/review/cards` 拉取来自 `memory_palace_words` 的今日待复习词卡。

移动端左右滑先写入 `ReviewSubmissionQueue` 内存队列，再 debounce 批量提交：

```text
右滑认识 / 左滑不认识
  -> ReviewSubmissionQueue.enqueue
  -> POST /api/v1/review/answer/batch
  -> backend writes review_records
  -> /api/v1/review/count 自动扣减今天已答过的词
```

记忆宫殿里的左右滑只作为回忆日记本的上一张/下一张，不写 `review_records`。

## Mock 与验证

远程开发库已使用以下脚本生成 hatsune 数据：

```bash
python LingoLens/scripts/generate_hatsune_mock_data.py --apply-schema
python LingoLens/scripts/verify_hatsune_integration.py
```

验证内容包括 schema、三语 JSON、语言切换、音频 URL 拼接、5 个宫殿、30 个 slot、30 条 AR 扫描词本记录。
