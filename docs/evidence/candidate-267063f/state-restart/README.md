# 0.2.2 的工作区重启和旧 Tasks 快照回归

同一 0.2.2 ZIP 的 SHA-256 为 `b2eed1cd59d1f9f6f2231c55bb6c0d97ab82c5a6da27963c2a934053739bedf0`。PyCharm PY-262.9437.214 在隔离配置正常启动、关闭，再次启动相同项目。原生日志记录两个完整生命周期。[启动与关闭](ide-lifecycle.log)

第一次进程 PID 2872 为 A 写入含空行、制表符、末字和尾空格的草稿，明确生成报告并把 B 草稿主动清空。第二次进程 PID 32160 检查 A 的逐字符草稿、报告 ID 和页签，B 清空后的第 2 次修订，以及真实注册 Tasks provider 加载旧快照后两条记录均保持完整。测试自己的模型端点收到 1/0 次请求，未插桩 GitHub GET/POST。[第一次](write.txt) · [第二次](read.txt) · [项目组件 XML](project-component.xml) · [旧定位器](old-context.xml) · [报告 ID](report-id.txt)

沿用[观察器与复跑脚本](../../candidate-e66ed31/state-restart)，传入 0.2.2 ZIP 及其 `-Sha256`，并使用独立输出目录。
