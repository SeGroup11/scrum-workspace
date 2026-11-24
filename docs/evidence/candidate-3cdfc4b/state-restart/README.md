# 0.2.4 正常关闭和跨进程恢复

PyCharm PY-262.9437.214 进程 17016 生成报告并记录 A 的多行、制表符、末字及尾空格，主动清空 B 草稿后正常关闭。进程 46656 重开同一项目，A 报告 ID、精确草稿、B 的清空修订、页签和原生 Tasks 位置均恢复。两次回环模型请求计数分别为 1 和 0；此无账号项目未记录 GitHub 请求。[写入](write.txt) · [读回](read.txt) · [两次启动与关闭](ide-lifecycle.log) · [落盘记录](project-component.xml) · [旧 Tasks 快照](old-context.xml) · [报告编号](report-id.txt) · [原观察器与复跑脚本](../../candidate-e66ed31/state-restart)

本轮唯一发布包 SHA-256 为 `a98dee22e68630481c65107efb13a2a4604da26cd6140f9bb5abd3db3b823709`。
