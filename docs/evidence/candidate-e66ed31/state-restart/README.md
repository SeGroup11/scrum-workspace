# 真实项目工作区和 Tasks 快照的重启验证

2026-11-24 16:01，在 Windows 同一空白配置、同一 QA-Project 上连续运行两个独立 PyCharm 进程，构建号 `PY-262.9437.214`。每次加载同一 0.2.1 候选及一个外置 QA 观察器，源码提交 `e66ed31`，ZIP SHA-256 为 `71c549a8203edb80060d37411ff344a32b10cec534982121163fe920c52b4cd4`。两个进程都通过原生 IDE API 关闭并释放项目，再走应用正常退出。第二个进程在第一个进程退出后才启动。[两次 IDE 启动和关闭日志](ide-lifecycle.log)

| 操作 | 实际观察 |
| --- | --- |
| 第一次进程写入 | 测试用定位器 A、B 分别使用 16、17，项目没有 GitHub remote 或登录账号。先编辑 A，再调用真实注册的 Tasks provider 在默认活动任务下保存 A 的旧快照。通过项目服务对固定模型输入发起一次真实回环请求，取得报告。随后更新 A 草稿，选择 B、输入后主动清空。关闭项目时检查平台实际生成的 workspace.xml 中存在本插件的组件。[第一进程](write.txt) · [实际组件 XML](project-component.xml) |
| 第二次进程读取 | 重新打开同一个项目，最后的 B 定位器和页签一致，B 草稿为空且修订号仍为 2；A 的草稿逐字符相等，包括空行、制表符、末字和两个尾随空格。A 的页签和报告请求 ID 与关闭前一致，B 没有 A 的报告。[第二进程](read.txt) · [关闭前报告 ID](report-id.txt) |
| 加载旧 Tasks 快照 | 两次均从平台扩展点定位生产 ReviewContextProvider。旧快照只有账号、仓库、Issue 号和页签。调用其 loadContext 后，当前定位器和页签回到 A，A/B 的完整记录与恢复前分别相等。[最初的任务快照](old-context.xml) · [断言源码](StateProbe.java) |
| 流量统计 | 测试注册的 chat/completions 端点收到第一次进程显式分析请求 1 次、第二次恢复 0 次。没有插桩 GitHub GET/POST，也没有真实私仓 Issue，因此本记录不能确定 GitHub 请求或远端评论条数。 |

本轮覆盖真实 IDE 的无界面生命周期、项目存储、模型请求和已注册 Tasks provider。桌面账号的 Issue 打开、从原生 Tasks 菜单来回切换，以及恢复时的 GitHub 流量仍需独立实机证据。

使用 PowerShell 7.4 或以上版本，在本仓库内将 PYCHARM_HOME 指向本机精确构建并运行下列命令。测试使用源码树中的 CSV 样例，在输出目录新建独立配置、项目和日志。

```powershell
./docs/evidence/candidate-e66ed31/state-restart/run.ps1 -Zip ./github-issue-review-0.2.1.zip -PyCharm $env:PYCHARM_HOME -Output ./build/state-restart-check
```
