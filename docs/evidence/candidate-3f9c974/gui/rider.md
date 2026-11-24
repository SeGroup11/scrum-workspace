# Rider 0.2.3 实机记录

产品为正常配置的 Rider 2026.2.1（RD-262.9437.287）。用户在此产品中确认打开课程项目。候选提交 `3f9c974b84a716ffbf8aa6c04d732601c0e3b4e0`，ZIP SHA-256 为 `d0e5c1af07918baa377a0a81151dabaace0ade7f5d7c5617a4b447315a24000f`。

| 用例 | 实际观察 | 判定 |
| --- | --- | --- |
| 从磁盘安装并启动 | 在原生插件管理器从磁盘选择同一候选 ZIP，保存并正常重启。17:08:16 启动日志读回插件 0.2.3，安装目录的产品 JAR 与 ZIP 条目哈希相同。 | 安装和加载通过。[重启提示](rider-restart-prompt.png) · [启动日志](rider-startup.log) · [JAR 哈希](rider-installed-jar.json) |
