# 0.2.3 原生 Tasks 首次绑定

使用 SHA-256 为 `d0e5c1af07918baa377a0a81151dabaace0ade7f5d7c5617a4b447315a24000f` 的实际发布 ZIP，在全新的 PyCharm PY-262.9437.214 配置与项目中，观察器先确认两项任务不存在。通过候选的 ReviewStore.activateTask(Task, ViewPointer) 进入真实 TaskManager，A、B 第一次激活均保持各自正确的编号和页签；原生管理器再次切回 A/B，也都恢复对应记录。四次转换后独立的两个草稿均完整。该项目只使用本地合成任务，不连接 GitHub。[逐次原生事件与断言](result.txt) · [真实加载与退出](ide-lifecycle.log)

同流程在 0.2.2 的首激 A、B 均失败。[旧包基线及同一观察器](../../candidate-267063f/task-activation) 使用该目录的 run.ps1，传入本次 ZIP、上述 SHA-256、正确的 PyCharm 安装路径和独立输出目录即可复跑。第一次编译新包观察器时，旧包分支仍静态引用已经移除的方法；将旧分支改成反射兼容后重新编译并完成上述运行。期间候选 ZIP 没有变化。
