# IntelliJ IDEA 0.2.4 实机记录

2026-11-24 在正常配置的 IntelliJ IDEA 2026.2.2（IU-262.10315.125）打开课程私仓。候选提交为 3cdfc4b5ccf22dd3792d6ade3c8952e9b32b0835，ZIP 的 SHA-256 为 a98dee22e68630481c65107efb13a2a4604da26cd6140f9bb5abd3db3b823709。下表的截图均取自实际 IDE 窗口。

| 验收操作 | 实际结果与证据 |
| --- | --- |
| 安装并加载 | 从原生设置选择候选 ZIP，正式重启。17:34:10 加载 0.2.4。安装后的生产 JAR 与候选 ZIP 条目逐字节哈希一致。[磁盘选择](idea-024-file.png) · [重启确认](idea-024-restart.png) · [启动日志](idea-starts.log) · [哈希](idea-jar.json) |
| 私仓与真实字段 | 主账号 material4002 自动恢复连接。B #17 显示 segroup11-backlog[bot]、创建时间 14:11:34 CST、原文「希望将搜索结果导出为 CSV。导出要快。」以及原有三条评论。[发前讨论](idea-024-before.png) |
| 慢分析与跨任务编辑 | 点击 B 的分析，转到 A #16 编辑具名草稿并设为当前 IDE 任务。回到 B，报告引用「导出要快」，说明缺少数据量和可接受耗时。首绘 11 ms，实际分析历时 10,843 ms。[发起分析](idea-024-pending.png) · [A 草稿](idea-024-draft-a.png) · [B 报告](idea-024-report.png) · [生产计时](idea-model.log) |
| 单次发布与回读 | 用于回复显示预览，Tab 后改写，专向 B #17 显式发送一次。UTC 09:38:09 返回评论 7127681925，material4002，列表从 3 到 4。改写文本为「[CS5351 IDEA 0.2.4] 请确认典型导出数据量，以及该规模下可接受的完成时间。」[预览](idea-024-preview.png) · [Tab](idea-024-tab.png) · [改写](idea-024-compose.png) · [返回与讨论](idea-024-receipt.png) |
| 原生任务切换与正常重启 | 使用插件内的 JetBrains TaskManager 按钮实际激活 B、A、B。最后一次激活 B 恢复它已保存的讨论页。正常重启再次自动打开 B 的讨论、原文和四条评论，保留 B 草稿「IDEA B 0.2.4：重启后保留末字乙」。[重启前 B 与原页签](idea-024-task-b.png) · [重启确认](idea-024-second-restart.png) · [重启恢复](idea-024-restored.png) |

该正常 IDEA 安装含平台 intellij.platform.tasks 与 intellij.platform.tasks.impl，未安装 WebStorm 自带的 tasks-core 独立任务菜单。以上任务激活均走插件按钮与平台 TaskManager。

## A 的成功、503 和显式重试

2026-11-24 在 GitHub 原文恢复后，对真实 A #16 双击分析。成功和 503 两轮均在请求未结束时滚动长正文、编辑 A 草稿、切 B 并独立输入，等待期结束时窗口仍显示 B。返回 A，成功报告指向「导出文件为空」；503 后仍显示上次报告、故障提示和本轮末字甲。把测试模型恢复为 slow-success，再显式点击一次分析。回环服务按次序只新增 18 / 19 / 20 三条记录。三轮的 IDE 首次绘制和完成耗时（各为 ms）依次为 25 / 10,792；63 / 10,042；7 / 10,014。[成功期间滚动](idea-024-a-success-scroll.png) · [成功后留在 B](idea-024-a-success-b-complete.png) · [A 报告](idea-024-a-success-report.png) · [错误期间滚动](idea-024-a-error-scroll.png) · [错误后留在 B](idea-024-a-error-b-complete.png) · [503 与旧报告](idea-024-a-error-result.png) · [模型计数](idea-loopback.log) · [IDE 时间](idea-model.log)

## 正常退出后的逐字符与请求计数

使用原生任务来回激活 A 和 B，A 写入含空行、制表符、末字甲和两个尾空格的独立草稿。B 先写入含末字乙的草稿，再主动清空。关闭前选择 B 的评估报告页。正常退出进程 5272 后按相同用户配置重开。进程 16024 自动进入 B 的报告页，编辑器为空。再进入 A 能读取原文和完整草稿。仅抽取项目内本插件组件作逐字符比较，A 共 22 个字符，B 为 0；两条记录均与期望相符，均有成功报告的请求 ID。每款的专属草稿在该产品关开期间单独写入、核验。[A 原稿](idea-024-a-exact.png) · [B 主动清空前](idea-024-b-before-clear.png) · [B 清空后](idea-024-b-cleared.png) · [正常退出](idea-024-normal-exit.png) · [重开 B](idea-024-cold-b.png) · [重开 A](idea-024-cold-a.png) · [逐字符、修订号和报告 ID](idea-exact-state.json)

恢复期正常仓库连接 1 次，读取详情 2 次。按进程独立记录的模型请求为 0、评论 POST 为 0。该统计仅覆盖本插件的选定方法。真实评论另由独立角色读回，记录在 GitHub 回执中。[进程恢复计数](../restart-calls/idea-restart.json) · [远端回执](../github/idea-receipt.json)

实测前该产品的模型地址和模型 ID 为空。验收结束时在插件原生设置中先删除本次 127.0.0.1:18535/v1 端点的演示密钥，再清空该地址与模型 ID，重新打开设置核对二者均为空。[恢复原设置](idea-024-settings-restored.png)
