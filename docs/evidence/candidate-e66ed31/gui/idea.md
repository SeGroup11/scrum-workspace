# IntelliJ IDEA 0.2.1 实机记录

2026-11-24 在 Windows 的 IntelliJ IDEA 2026.2.2 正常用户配置打开课程项目。IDE build 为 IU-262.10315.125，当前 Git remote 为 SeGroup11/scrum-workspace。

我从「设置 → 插件 → 从磁盘安装插件」选择提交 `e66ed311cf986d876814f7737f897b526322236a` 的 `github-issue-review-0.2.1.zip`。文件大小为 75,389,798 字节，SHA-256 为 `71c549a8203edb80060d37411ff344a32b10cec534982121163fe920c52b4cd4`。

| 用例 | 实际操作和结果 | 判定 |
| --- | --- | --- |
| N3 安装并打开工具窗口 | IDE 显示第三方插件发布者 CS5351 Group 11。应用后，15:59 的本机动态加载日志出现 `team.segroup11.issue-review, 0.2.1`。在 IDE 的操作搜索中选中 Issue Review，工具窗口打开。 | 通过此产品的安装与工具窗口子项。[运行日志](idea-dynamic-load.log) · [工具窗口](idea-empty-account.png) |
| G1 空账号 | 首次进入时，仓库自动指向项目的 origin，账号选择框为空，并提示「请在 IDE 的 GitHub 设置登录，然后重新检测」。点击「GitHub 账号」打开原生设置并确认账号列表为空。 | 空账号提示通过。[实机截图](idea-empty-account.png) |
| G1 主账号与仓库访问 | 在原生 GitHub 设置选择「通过 GitHub 登录」，已有浏览器会话返回用户指定的个人主账号。保存后点击插件的「重新检测」，选中的仓库和身份正确。窗口随后显示「账号身份已核实，GitHub 未允许访问此仓库」。Issue 列表仍为空。 | 身份核对、仓库授权失败提示通过。私仓读取受阻。[实机截图，已遮盖账号名](idea-repository-denied.png) |

本轮没有读取到 #16 或 #17，也没有从 IDEA 发送评论。依赖真实 Issue 数据的模型、草稿、Tab 与发送用例尚待复测。

本机状态变化包括当前课程项目在此 IDEA 配置中受信任、插件已安装，以及主账号已加入此配置。项目打开前已取消 Defender 排除选项，也未选择信任父级 Code 目录。
