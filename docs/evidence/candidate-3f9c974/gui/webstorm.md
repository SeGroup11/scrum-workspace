# WebStorm 0.2.3 实机记录

产品为正常配置的 WebStorm 2026.2.2（WS-262.10315.144）。候选提交 `3f9c974b84a716ffbf8aa6c04d732601c0e3b4e0`，ZIP SHA-256 为 `d0e5c1af07918baa377a0a81151dabaace0ade7f5d7c5617a4b447315a24000f`。

| 用例 | 实际观察 | 判定 |
| --- | --- | --- |
| 从磁盘安装并启动 | 从原生磁盘安装入口选取候选 ZIP，确认安装并重启。16:59:11 的启动日志读回插件 0.2.3。安装后的插件 JAR 与 ZIP 内的条目逐哈希一致。 | 安装和加载通过。[重启提示](webstorm-restart-prompt.png) · [启动日志](webstorm-startup.log) · [JAR 哈希](webstorm-installed-jar.json) |
| 私仓及模型 | 启动后恢复真实仓库；#17 显示作者、日期、原文及两条既有评论。原生模型页最初为空，保存本机回环 slow-success 后返回。 | 通过。[讨论](webstorm-023-original.png) · [空配置](webstorm-023-blank-settings.png) |
| 慢分析与编辑 | B 请求期间编辑 A 草稿。B 报告引用「导出要快」，给出数据量与时限追问。IDE 首绘 12 ms，全请求 10,725 ms。 | 通过。[请求](webstorm-023-pending.png) · [A 草稿](webstorm-023-draft-a.png) · [报告](webstorm-023-report.png) · [计时](webstorm-023-model.log) |
| 评论单发 | 用于回复、Tab 采纳、改写、点击发送后，远端和窗口均显示 #17 新评论 7127188425，material4002，09:16:29Z；讨论总数由 2 到 3。 | 通过。[预览](webstorm-023-preview.png) · [Tab](webstorm-023-tab.png) · [改写](webstorm-023-before-send.png) · [回执](webstorm-023-receipt.png) |
| 原生任务 | 通过 JetBrains 标题栏任务菜单 A→B→A，视图及草稿按 Issue 切换。 | 通过。[菜单](webstorm-023-native-menu.png) · [B](webstorm-023-native-b.png) · [A](webstorm-023-native-a.png) |
| 正常重启 | 17:18:04 再次加载 0.2.3，自动回到原生任务 #16 的讨论页，完整 A 正文、两条评论和本次输入的 WebStorm 草稿可见。 | 通过。[系统确认](webstorm-023-normal-restart.png) · [重启恢复](webstorm-023-after-restart.png) |
