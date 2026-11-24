# WebStorm 0.2.3 实机记录

产品为正常配置的 WebStorm 2026.2.2（WS-262.10315.144）。候选提交 `3f9c974b84a716ffbf8aa6c04d732601c0e3b4e0`，ZIP SHA-256 为 `d0e5c1af07918baa377a0a81151dabaace0ade7f5d7c5617a4b447315a24000f`。

| 用例 | 实际观察 | 判定 |
| --- | --- | --- |
| 从磁盘安装并启动 | 从原生磁盘安装入口选取候选 ZIP，确认安装并重启。16:59:11 的启动日志读回插件 0.2.3。安装后的插件 JAR 与 ZIP 内的条目逐哈希一致。 | 安装和加载通过。[重启提示](webstorm-restart-prompt.png) · [启动日志](webstorm-startup.log) · [JAR 哈希](webstorm-installed-jar.json) |
