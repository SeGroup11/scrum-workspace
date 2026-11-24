# WebStorm 磁盘安装与私仓连接实测

2026-11-24，Windows build 26300.9974，WebStorm 2026.2.2（WS-262.10315.144）。候选提交 `29740976e138e3e3566c1d222e5963f4c4bd5c54`，文件 `github-issue-review-0.2.0.zip`，SHA-256 `e0b10cede8dc310990fc1dad747cf46a9c6319a4cb1c89db9e40156cc394a0d3`。

| 观察项 | 实际操作及结果 | 证据 |
| --- | --- | --- |
| N3 的安装与启动子项，通过 | 我在目标 WebStorm 打开本组项目，从设置的「插件 → 从磁盘安装插件」选取上述 ZIP，应用变更。WebStorm 在 15:16 动态加载 0.2.0。我用动作搜索打开 Issue Review，看到仓库、账号、搜索框、分析与评论控件。 | [IDE 动态加载日志节选](webstorm-dynamic-load.log) · [原生窗口截图](webstorm-window-authentication.png) |
| G1 的私仓读取，受阻 | 插件显示项目的 `origin / SeGroup11/scrum-workspace`，选择了已配置的 IDE GitHub 账号。预期读取夹具 A #16、B #17。界面显示「GitHub 身份验证失败，请在 IDE GitHub 设置中重新登录」，列表为空。原始日志节选只证明插件已加载，认证结果以界面取证。 | [现场截图，已遮盖本地路径与账号](webstorm-window-authentication.png) |
| 后续实机用例，待测 | 私仓详情、慢分析、任务切换、重启恢复和发布均未在本次操作中完成。此次没有发出测试评论，N3 总项保留待测。 | 身份重新验证后，平台与 GitHub 组在同一候选上从夹具 A/B 继续。 |

**主账号复测。** 我打开 WebStorm 的 GitHub 设置，选择「通过 GitHub 登录」。JetBrains 的官方回调页显示 GitHub 授权成功，IDE 提示该账号已添加，与工具窗口唯一已配置的个人账号相同。我退出设置并点击「连接」，看到「正在核对账号与仓库权限」，随后再次得到截图中的错误。这次没有移除原账号。目标 SDK 将非限流的 HTTP 401、402、403 合并为同一种认证异常，目前界面不足以判断失败发生在读取身份还是读取仓库时，具体 HTTP 状态也未取到。

**仓库权限辅助核对。** 我在同一 PowerShell 会话验证 review App 身份，查询 GitHub 的 Get repository permissions for a user 接口，所选个人账号的 `permission` 和 `role_name` 均为 `admin`。这是仓库成员角色的读回值。插件能否访问私仓仍以通过 IDE 执行器实际读取的 Issue 为准。

**原生对照。** 我在同一个项目打开 WebStorm 自带的「拉取请求」窗口，先遇到 TLS 握手中断。使用该窗口的「重新登录」更新既有账号后，原生窗口显示仓库不可见或访问被拒绝，并提示检查组织的 OAuth 应用访问限制。[原生仓库读取错误截图](webstorm-native-repository-denied.png)
