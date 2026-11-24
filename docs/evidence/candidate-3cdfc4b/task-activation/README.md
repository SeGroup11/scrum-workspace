# 0.2.4 原生 Tasks 首次与切回

在 PyCharm PY-262.9437.214 的全新项目中先确认两项合成任务均不存在，随后通过生产入口 ReviewStore.activateTask 调用真实 TaskManager。A、B 首次激活以及已有 A、B 切回四次都显示预期的 Issue 编号、页签，两个独立草稿逐字相同。测试项目没有远端 Git 连接。[逐步事件及断言](result.txt) · [IDE 加载](ide-lifecycle.log) · [原观察器与复跑脚本](../../candidate-267063f/task-activation)

本轮唯一发布包 SHA-256 为 `a98dee22e68630481c65107efb13a2a4604da26cd6140f9bb5abd3db3b823709`。
