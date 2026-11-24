# WebStorm 0.2.1 实机验收记录

2026-11-24，Windows build 26300.9974，WebStorm 2026.2.2（WS-262.10315.144），同一项目的 Git 仓库为 SeGroup11/scrum-workspace。

候选提交 `e66ed31`，安装文件 `github-issue-review-0.2.1.zip`，SHA-256 `71c549a8203edb80060d37411ff344a32b10cec534982121163fe920c52b4cd4`。

| 用例 | 操作与实测 | 状态 |
| --- | --- | --- |
| N3 安装和启用子项 | 我从 WebStorm 的「设置 → 插件 → 从磁盘安装插件」选取候选 ZIP，按 IDE 提示正常重启，15:41 在启动日志读回 `GitHub Issue Quality Review (0.2.1)`。项目重新打开后可见 Issue Review 工具窗口。 | 通过。[启动日志](webstorm-startup.log) · [工具窗口](webstorm-repository-denied.png) |
| G1 身份与仓库授权分阶段提示 | 我在项目根可用后点击「重新检测」。插件选中项目的 origin 和 GitHub 个人账号，先显示正在核对账号与仓库，随后显示「账号身份已核实，GitHub 未允许访问此仓库」。身份验证成功，仓库读取受阻。该账号在仓库的成员角色经独立只读核对为 admin。 | 失败阶段提示通过。私仓读取待复测。[实机截图，已遮盖账号名](webstorm-repository-denied.png) |
| G1 组织授权状态对照 | 我在该主账号的 GitHub 官方 Authorized OAuth Apps 页面打开 JetBrains IDE Integration。Organization access 下的 SeGroup11 一行显示 Restricted，并提供 Grant。只读核对时未修改授权。 | 已观察到组织级限制。[GitHub 应用设置入口](https://github.com/settings/connections/applications/58566862bd2a5ff748fb) |

### 授权后的实机复测

用户确认已向 SeGroup11 授权后，我从同一个 WebStorm 工具窗口点击「连接」。插件显示 18 张 Issue。以下时刻为本机 2026-11-24 的截图保存时间，时区为 UTC+8。

| 时刻 | 实际结果 | 证据 |
| --- | --- | --- |
| 16:08:15 | 在列表选择 #16「导出文件为空」。右侧显示目标仓库、复现步骤、原始批次观察及 0 条评论。 | [A 正文](webstorm-real-a.png) |
| 16:08:30 | 选择 #17「导出搜索结果」。正文为「希望将搜索结果导出为 CSV。导出要快。」；发送前为 0 条评论。 | [B 正文](webstorm-real-b.png) |
| 16:08:47 | 编辑框目标为 SeGroup11/scrum-workspace #17，最终正文为「[CS5351 WS 0.2.1] 请确认典型导出数据量，以及该规模下可接受的完成时间。」 | [发送前](webstorm-before-send.png) |
| 16:09:33 | 点击一次「发送评论」，回复框清空，界面显示「已发送评论 #7125780145」。GitHub 的独立读回证实作者为用户主账号，发送时间为 08:08:49Z。 | [发送后](webstorm-after-send.png) · [GitHub 评论](https://github.com/SeGroup11/scrum-workspace/issues/17#issuecomment-7125780145) |

**0.2.1 实机失败项。** 这两张 Issue 的作者和创建时间均显示未知，发送后的新评论也显示未知作者，讨论计数停在 0。GitHub 原始数据含有效作者和日期。此轮验证通过私仓读取、正文和服务器确认发布；作者、时间和发布后计数须修复复测。[原始字段](../github/field-regression-input.json)
