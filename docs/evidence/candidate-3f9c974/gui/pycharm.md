# PyCharm 0.2.3 实机验收

2026-11-24 在 PyCharm PY-262.9437.214 的受信任项目中，从磁盘安装候选 `3f9c974b84a716ffbf8aa6c04d732601c0e3b4e0`，使用私仓账号 material4002。安装包 SHA-256 为 `d0e5c1af07918baa377a0a81151dabaace0ade7f5d7c5617a4b447315a24000f`。重启后，IDE 日志列出了插件 0.2.3。已安装的主 JAR 与候选 ZIP 中对应条目的 SHA-256 一致。[安装完成与重启](pycharm-live/2026-11-24T08-55-40-387Z-installed-restart.jpg) · [启动日志](pycharm-startup.log) · [安装摘要](pycharm-jars.json)

| 用例 | 实际结果 |
| --- | --- |
| 冷启动恢复仓库 | 重启后第一次查看窗口，仓库已自动恢复到目标 origin，账号为 material4002，列表有 19 张 Issue。原生顶栏和插件均恢复到 #17，选中报告页，B 的旧草稿仍在。期间没有点击“连接”或“重新检测”。[首次观察](pycharm-live/2026-11-24T08-59-23-039Z-automatic-startup-before-any-controls.jpg) |
| 首次绑定原生任务 | 打开此前没有原生任务快照的 #20，只点一次“设为当前 IDE 任务”。右侧持续显示 #20 正文，原生 Tasks 顶部异步从 #17 更新为 #20。在该 Issue 上未编辑或发送任何内容。[激活前正文](pycharm-live/2026-11-24T09-02-12-366Z-fresh-20-loaded-before-binding.jpg) · [原生标题与右侧详情稳定一致](pycharm-live/2026-11-24T09-02-38-990Z-fresh-20-stable-title-and-detail.jpg) · [独立原生 Tasks 首次和恢复回归](../task-activation/README.md) |
| A 在途分析与 B 独立编辑 | A #16 显示真实作者、时间、长正文和 1 条评论。点击“分析”，再次点击禁用的按钮，滚动 A、编辑 A 草稿，再切到 B #17 改写 B 的草稿。A 的请求完成时窗口仍显示 B，B 未收到 A 的报告。切回 A 可见报告以及等待中编辑的末字甲。生产代码记录按钮首帧 6 ms，分析完成 10,485 ms；回环服务仅记录一次 slow-success，耗时 10,000 ms。[A 初始状态](pycharm-live/2026-11-24T09-02-55-866Z-a-before-slow-success-count.jpg) · [滚动中](pycharm-live/2026-11-24T09-03-07-161Z-a-scroll-during-analysis.jpg) · [B 编辑中](pycharm-live/2026-11-24T09-03-10-480Z-b-edited-while-a-in-flight.jpg) · [A 完成后仍在 B](pycharm-live/2026-11-24T09-03-30-979Z-b-remains-after-a-result.jpg) · [IDE 计时](pycharm-live/production-timings.log) · [模型请求](pycharm-live/loopback-calls.log) |
| 人工采纳与发送 | 在 A 报告选中“复现步骤”，点击“用于回复”，按 Tab 采纳，再改成组员自己的完整评论。核对目标后只点一次“发送评论”。“正在发送…”期间将编辑器改为后续草稿。回执返回后新草稿和空行仍在。[预览](pycharm-live/2026-11-24T09-03-40-263Z-suggestion-preview.jpg) · [Tab 采纳](pycharm-live/2026-11-24T09-03-40-650Z-suggestion-accepted-with-tab.jpg) · [发送前人工改写](pycharm-live/2026-11-24T09-03-50-087Z-final-rewrite-before-explicit-send.jpg) · [在途新编辑](pycharm-live/2026-11-24T09-04-01-943Z-edited-before-send-returned.jpg) |
| 远端确认与只加评论的报告状态 | PyCharm 重新读取讨论，显示 2 条评论，最新作者 material4002、回执 7126914405 与输入文字一致。独立 GitHub 检查确认 #16 从 1 变为 2，#17 当时仍为 1。只加评论后点“刷新详情”，A 报告仍显示“缺陷报告 · slow-success”，没有旧版本提示。[发评前报告](pycharm-live/2026-11-24T09-03-39-284Z-a-current-report-before-comment.jpg) · [发评后刷新报告](pycharm-live/2026-11-24T09-04-41-015Z-comment-only-refresh-report-current.jpg) · [IDE 重读评论与新草稿](pycharm-live/2026-11-24T09-04-56-959Z-discussion-refetched-count-body-and-receipt.jpg) · [真实评论](https://github.com/SeGroup11/scrum-workspace/issues/16#issuecomment-7126914405) |

### 正文变更与还原

09:08:58 UTC 独立 QA 在专用 #16 短暂增补 Markdown 图、十一行 CSV，再读回已修改的正文。PyCharm 刷新后，原报告显示“基于较早版本 · slow-success”。讨论页能读到图片替代文字、原链接及多行代码文本。09:09:57 UTC，GitHub 确认正文逐字恢复原始 SHA-256。PyCharm 再次刷新后，旧标消失，标题恢复“缺陷报告 · slow-success”。两次远端回读的评论数均为 2。[远端变更](../github/body-apply.json) · [旧版本提示](pycharm-live/2026-11-24T09-09-18-254Z-temporary-body-stale-report-with-unchanged-new-draft.jpg) · [图片与代码降级显示](pycharm-live/2026-11-24T09-09-24-261Z-temporary-markdown-image-alt-and-csv.jpg) · [滚动后](pycharm-live/2026-11-24T09-09-38-089Z-temporary-csv-lower-lines-original-body.jpg) · [远端还原](../github/body-restore.json) · [还原后当前报告](pycharm-live/2026-11-24T09-10-14-527Z-restored-body-same-report-current-and-draft.jpg)

两份正常保存的项目组件摘录显示，发评后和还原后均为请求 `ce24ab60-0102-44e1-b833-ab252a3d02dd`，完成时间和输入摘要相同，新草稿修订均为 15，末字乙后两个空格及中间空行均保留。B 草稿仍是独立的修订 7。从正文变更到还原未再点击“分析”或“发送”，回环服务仍停在第 5 次请求。[变更前状态](pycharm-live/state-after-comment-before-body.json) · [还原后状态](pycharm-live/state-restored-original-body.json) · [独立发评读回](../github/pycharm-receipt.json)

### 慢失败、显式重试与 Tasks

在模型设置切为 slow-error 后，点一次“分析”，等待中重按禁用按钮、滚动和修改 A 草稿，接着切到 B。B 仍显示自己的草稿和报告页。回 A 后，旧成功报告、原发送回执、在途增加的末字丙与 HTTP 503 提示同时可见。生产按钮首帧 5 ms，返回共 10,084 ms，服务收到一次请求。回到模型设置改为 slow-success，人工再次点击“分析”，首帧 71 ms，共 10,015 ms，服务又收到一次请求。新报告 UUID 为 `8003bca7-302b-43cf-8864-47cce710ad54`，在途草稿保留，未再发评。[失败后 A 的旧报告和新稿](pycharm-live/2026-11-24T09-24-27-369Z-a-old-report-and-new-draft-after-503.jpg) · [人工重试](pycharm-live/2026-11-24T09-24-37-890Z-explicit-retry-in-progress.jpg) · [新报告及草稿](pycharm-live/2026-11-24T09-25-11-275Z-retry-completed-expected-report-and-draft.jpg) · [IDE 计时](pycharm-live/production-timings.log) · [请求计数](pycharm-live/loopback-calls.log)

从 IDE 标题栏的 Tasks 列表依次选择现有任务的 Switch to。A 恢复讨论页与末字丙的最新草稿，B 恢复报告页及自己的草稿。[原生 A](pycharm-live/2026-11-24T09-25-10-795Z-native-task-a-settled-latest-draft.jpg) · [原生 B](pycharm-live/2026-11-24T09-25-38-023Z-native-task-b-settled-independent-draft.jpg) · [项目组件与活动 B](pycharm-live/state-after-error-retry-before-restart.json)

选择原生 Restart IDE 并确认。IDE 于 17:26:28 正常关闭，17:26:29 启动新进程，再次加载 0.2.3 并打开同一项目。第一次获取重开窗口时，原生顶栏、Issue 选中行、报告页与独立草稿均指向 B #17。[重启日志](pycharm-live/same-version-restart.log) · [复开 B 的实机画面](pycharm-live/2026-11-24T09-34-35-872Z-same-version-restart-automatic-b-task-and-draft.jpg)

所有截图均来自本次授权测试项目窗口，文件名使用 UTC 采集时间。受控服务仅在本机回环地址提供模型响应；私仓正文、评论和远端回执由实际 GitHub 账号读取。独立双进程的精确草稿、主动清空、旧 Tasks 定位及恢复流量详见[状态重启证据](../state-restart/README.md)。
