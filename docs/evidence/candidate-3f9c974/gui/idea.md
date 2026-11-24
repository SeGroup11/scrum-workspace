# IntelliJ IDEA 0.2.3 实机记录

产品为正常配置的 IntelliJ IDEA 2026.2.2（IU-262.10315.125）。候选提交 `3f9c974b84a716ffbf8aa6c04d732601c0e3b4e0`，ZIP SHA-256 为 `d0e5c1af07918baa377a0a81151dabaace0ade7f5d7c5617a4b447315a24000f`。

| 用例 | 实际观察 | 判定 |
| --- | --- | --- |
| 从磁盘安装并启动 | 通过原生「从磁盘安装插件」打开候选 ZIP，确认安装并重启。16:57:52 的启动日志读回插件 0.2.3。安装后的插件 JAR 与 ZIP 内的条目逐哈希一致。 | 安装和加载通过。[重启提示](idea-restart-prompt.png) · [启动日志](idea-startup.log) · [JAR 哈希](idea-installed-jar.json) |
| 私仓和原生任务 | 启动后自动恢复仓库和主账号。打开 #17 可见作者、创建时间及讨论。首次设 B 为原生任务后详情完整。 | 通过。[恢复](idea-023-autorestore.png) · [激活 B](idea-023-active-b.png) |
| 慢分析与编辑 | B 发起慢分析，随后进入 A 并输入未发送草稿。B 得到 slow-success 报告，原文「导出要快」及追问可核。IDE 首绘 9 ms，全请求 10,683 ms。 | 通过。[A 草稿](idea-023-draft-a.png) · [报告](idea-023-report.png) · [计时](idea-023-model.log) |
| 评论单发 | 预览后用 Tab 采纳，再改写并显式发送到 #17。远端回执 7127110320，主账号 material4002，09:12:56Z，评论数由 1 到 2。 | 通过。[预览](idea-023-preview.png) · [Tab](idea-023-tab.png) · [改写](idea-023-before-send.png) · [发送](idea-023-sent.png) · [讨论](idea-023-receipt.png) |
| 原生任务及正常重启 | 通过插件内的平台 TaskManager 入口分别激活 B、A。17:29:39 正常重启后自动恢复 #16 讨论页、原文、两条评论和专属草稿「IDEA A 0.2.3：正常重启前的草稿」。该 IDEA 安装包含平台原生 Tasks 实现，未提供 WebStorm 的独立 Tasks 菜单。 | 通过。[任务 B](idea-023-native-b.png) · [任务 A](idea-023-native-a.png) · [启动时间](idea-023-loads.log) · [重启后](idea-023-after-restart.png) |
