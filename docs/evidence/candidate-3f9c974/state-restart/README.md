# 0.2.3 项目存储与正常重启

0.2.3 同包在两个独立的真实 PyCharm PY-262.9437.214 进程中打开同一个项目。第一次 PID 37348 显式分析生成 A 的报告和带空行、制表符、末字及尾随空格的草稿，主动清空 B。正常保存和关闭项目后，用 PID 33416 重开。逐字符草稿、报告请求 ID、页签、B 的清空修订及原生 Tasks 旧定位器均正确。回环模型端点第一次收到一次请求，恢复时收到零次。未对 GitHub 流量插桩。[写入](write.txt) · [读回](read.txt) · [两启两关日志](ide-lifecycle.log) · [项目组件](project-component.xml) · [旧快照](old-context.xml) · [模型报告 ID](report-id.txt)

使用[独立观察器和复跑脚本](../../candidate-e66ed31/state-restart)，传入 0.2.3 ZIP 及 `-Sha256 d0e5c1af07918baa377a0a81151dabaace0ade7f5d7c5617a4b447315a24000f`。
