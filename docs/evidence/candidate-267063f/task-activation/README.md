# 原生 Tasks 冷启动回归

0.2.2 的 ZIP 在独立 PyCharm PY-262.9437.214 下产生了两次可重复的失败。观察器创建全新项目，先确认两个人工 Issue 均未出现在原生 Tasks 中，再使用 0.2.2 生产按钮相同的 bindTask 与 TaskManager.activateTask 调用，不连接 GitHub 或模型服务。每次转换后等待事件队列完成并读取活动任务、插件记录和两个草稿。[实际日志](result.txt) · [候选加载与关闭](ide-lifecycle.log)

| 转换 | 正常 IDE 的真实事件与结果 |
| --- | --- |
| 无旧快照时第一次绑定 A 或 B | 原生任务先变为目标，随后原生上下文发出 clear，观察器收到 restore(null)。1.2 秒后，活动任务正确，右侧记录仍为空。两个草稿仍完整。两次均失败。 |
| 已有关联后通过原生管理器切回 A 或 B | 原生管理器先清空，再通过旧快照加载想要的 ViewPointer。A/tab0 和 B/tab1 分别恢复，草稿完整。两次均通过。 |

对新候选，观察器查找产品的 ReviewStore.activateTask(Task, ViewPointer) 入口并直接调用；对旧候选，使用其按钮内原有的两条调用。每个 ZIP 都在独立的全新测试项目测试。[完整观察器](TaskProbe.java) · [复跑脚本](run.ps1)

```powershell
./docs/evidence/candidate-267063f/task-activation/run.ps1 -Zip ./github-issue-review-0.2.2.zip -Sha256 b2eed1cd59d1f9f6f2231c55bb6c0d97ab82c5a6da27963c2a934053739bedf0 -PyCharm $env:PYCHARM_HOME -Output ./build/native-task-probe
```
