# Rider 0.2.2 实机验收

2026-11-24，JetBrains Rider 2026.2.1 正常用户配置，RD-262.9437.287。唯一插件 ZIP 对应提交 `267063f8d7fc2d321f03d386b2ce9c64553c916c`，SHA-256 为 `b2eed1cd59d1f9f6f2231c55bb6c0d97ab82c5a6da27963c2a934053739bedf0`。

| 用例 | 实际观察 | 结果 |
| --- | --- | --- |
| N3 从磁盘安装 | 在 Rider 欢迎页选择「配置 → 插件 → 从磁盘安装插件」。选中唯一候选 ZIP，核对发布者 CS5351 Group 11，保存。16:30:26 日志记录动态加载 `team.segroup11.issue-review, 0.2.2`。已安装插件 JAR 的 SHA-256 与 ZIP 中相同位置的 JAR 一致。 | 安装及加载通过。[文件选择](rider-disk-selection.png) · [动态加载日志](rider-dynamic-load.log) · [JAR 对照](rider-installed-jar.json) |
