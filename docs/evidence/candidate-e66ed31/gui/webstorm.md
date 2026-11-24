# WebStorm 0.2.1 实机验收记录

2026-11-24，Windows build 26300.9974，WebStorm 2026.2.2（WS-262.10315.144），同一项目的 Git 仓库为 SeGroup11/scrum-workspace。

候选提交 `e66ed31`，安装文件 `github-issue-review-0.2.1.zip`，SHA-256 `71c549a8203edb80060d37411ff344a32b10cec534982121163fe920c52b4cd4`。

| 用例 | 操作与实测 | 状态 |
| --- | --- | --- |
| N3 安装和启用子项 | 我从 WebStorm 的「设置 → 插件 → 从磁盘安装插件」选取候选 ZIP，按 IDE 提示正常重启，15:41 在启动日志读回 `GitHub Issue Quality Review (0.2.1)`。项目重新打开后可见 Issue Review 工具窗口。 | 通过。[启动日志](webstorm-startup.log) · [工具窗口](webstorm-repository-denied.png) |
| G1 身份与仓库授权分阶段提示 | 我在项目根可用后点击「重新检测」。插件选中项目的 origin 和 GitHub 个人账号，先显示正在核对账号与仓库，随后显示「账号身份已核实，GitHub 未允许访问此仓库」。身份验证成功，仓库读取受阻。该账号在仓库的成员角色经独立只读核对为 admin。 | 失败阶段提示通过。私仓读取待复测。[实机截图，已遮盖账号名](webstorm-repository-denied.png) |
| G1 组织授权状态对照 | 我在该主账号的 GitHub 官方 Authorized OAuth Apps 页面打开 JetBrains IDE Integration。Organization access 下的 SeGroup11 一行显示 Restricted，并提供 Grant。只读核对时未修改授权。 | 已观察到组织级限制。[GitHub 应用设置入口](https://github.com/settings/connections/applications/58566862bd2a5ff748fb) |

此轮观察未取得 A #16 或 B #17 的正文，也没有由插件发布评论。N1/N2、C1/C2 和四产品 N3 总项仍需分别用同一候选核验。
