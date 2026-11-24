# WebStorm 0.2.4 实机记录

WebStorm 2026.2.2（WS-262.10315.144）使用正常用户配置、主账号 material4002 和真实 SeGroup11/scrum-workspace。候选 3cdfc4b5ccf22dd3792d6ade3c8952e9b32b0835 的 ZIP SHA-256 为 a98dee22e68630481c65107efb13a2a4604da26cd6140f9bb5abd3db3b823709。

| 验收操作 | 结果与证据 |
| --- | --- |
| 安装并加载 | 从磁盘安装同一份 ZIP，原生提示重启。17:40:18 载入 0.2.4。安装 JAR 与压缩包的生产 JAR 哈希相等。[磁盘安装](webstorm-024-file.png) · [重启](webstorm-024-restart.png) · [启动日志](webstorm-starts.log) · [JAR](webstorm-jar.json) |
| 私仓与原文 | 真实 B #17 显示作者、日期与原文「希望将搜索结果导出为 CSV。导出要快。」。点击 GitHub 原文后核对浏览器标题为「导出搜索结果 · Issue #17 · SeGroup11/scrum-workspace」。[B 原文和四条已有评论](webstorm-024-before.png) |
| B 报告、预览和单发 | B 的 slow-success 报告引用「导出要快」，在 A 编辑后回 B，预览建议、Tab 采纳、改写。显式发布一次后返回 #17 评论 7127850398，主账号 material4002，09:45:38Z，计数 4→5。[A 编辑](webstorm-024-draft-a.png) · [报告](webstorm-024-report.png) · [预览](webstorm-024-preview.png) · [Tab](webstorm-024-tab.png) · [改写](webstorm-024-compose.png) · [回执](webstorm-024-receipt.png) |
| 原生任务 | A 通过插件按钮成为原生任务后，使用 WebStorm 标题栏 Tasks 菜单，选 B，再选「切换到」。B 正文及草稿可见。[任务 A](webstorm-024-task-a.png) · [原生菜单](webstorm-024-switch-menu.png) |

## A 的成功、503 和显式重试

2026-11-24 在 GitHub 原文恢复后，对真实 A #16 双击分析。成功和 503 两轮均在请求未结束时滚动长正文、编辑 A 草稿、切 B 并独立输入，等待期结束时窗口仍显示 B。返回 A，成功报告指向「导出文件为空」；503 后仍显示上次报告、故障提示和本轮末字甲。把测试模型恢复为 slow-success，再显式点击一次分析。回环服务按次序只新增 21 / 22 / 23 三条记录。三轮的 IDE 首次绘制和完成耗时（各为 ms）依次为 5 / 10,010；10 / 10,042；6 / 10,012。[成功期间滚动](webstorm-024-a-success-scroll.png) · [成功后留在 B](webstorm-024-a-success-b-complete.png) · [A 报告](webstorm-024-a-success-report.png) · [错误期间滚动](webstorm-024-a-error-scroll.png) · [错误后留在 B](webstorm-024-a-error-b-complete.png) · [503 与旧报告](webstorm-024-a-error-result.png) · [模型计数](webstorm-loopback.log) · [IDE 时间](webstorm-model.log)

## 正常退出后的逐字符与请求计数

使用原生任务来回激活 A 和 B，A 写入含空行、制表符、末字甲和两个尾空格的独立草稿。B 先写入含末字乙的草稿，再主动清空。关闭前选择 B 的评估报告页。正常退出进程 43940 后按相同用户配置重开。进程 28680 自动进入 B 的报告页，编辑器为空。再进入 A 能读取原文和完整草稿。仅抽取项目内本插件组件作逐字符比较，A 共 20 个字符，B 为 0；两条记录均与期望相符，均有成功报告的请求 ID。每款的专属草稿在该产品关开期间单独写入、核验。[A 原稿](webstorm-024-a-exact.png) · [B 主动清空前](webstorm-024-b-before-clear.png) · [B 清空后](webstorm-024-b-cleared.png) · [正常退出](webstorm-024-normal-exit.png) · [重开 B](webstorm-024-cold-b.png) · [重开 A](webstorm-024-cold-a.png) · [逐字符、修订号和报告 ID](webstorm-exact-state.json)

恢复期正常仓库连接 1 次，读取详情 2 次。按进程独立记录的模型请求为 0、评论 POST 为 0。该统计仅覆盖本插件的选定方法。真实评论另由独立角色读回，记录在 GitHub 回执中。[进程恢复计数](../restart-calls/webstorm-restart.json) · [远端回执](../github/webstorm-receipt.json)

实测前该产品的模型地址和模型 ID 为空。验收结束时在插件原生设置中先删除本次 127.0.0.1:18535/v1 端点的演示密钥，再清空该地址与模型 ID，重新打开设置核对二者均为空。[恢复原设置](webstorm-024-settings-restored.png)
