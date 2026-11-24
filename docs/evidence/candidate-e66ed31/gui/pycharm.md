# PyCharm 0.2.1 实机验收

2026-11-24，Windows，PyCharm `PY-262.9437.214`。本次沿用已受信任的 Group11-Candidate-PyCharm 测试项目和独立 IDE 配置。项目 origin 指向 SeGroup11/scrum-workspace。候选来自提交 `e66ed311cf986d876814f7737f897b526322236a`，ZIP 的 SHA-256 为 `71c549a8203edb80060d37411ff344a32b10cec534982121163fe920c52b4cd4`。

| 测试 | 操作与观察 | 结果 |
| --- | --- | --- |
| 从磁盘升级 | 在 IDE 动作搜索运行 Install Plugin from Disk，选择 0.2.1 ZIP。确认第三方插件提示后，IDE 提示 Git4Idea 和 org.jetbrains.plugins.github 为未知依赖。点 OK 后，IDE 明确显示插件已安装，要求重启。 | 已安装。[依赖提示](pycharm-disk-unknown-dependencies.jpg) · [安装后重启提示](pycharm-disk-installed-restart.jpg) |
| 重启、启用、打开工具窗口 | 重启同一实例后，15:47 的启动日志列出 GitHub Issue Quality Review (0.2.1)。工具窗口中可见“重新认证所选账号”等控件。安装后的主 JAR 的 SHA-256 为 `5f20a023d0327acd80ef29577082bbd57444e0fbfa2a91dce8d9a773e647e42a`。 | 通过。[原生启动日志](pycharm-startup.log) · [更新后工具窗口](pycharm-tool-window-updated.jpg) |
| 身份和私仓权限 | 点击“重新检测”，工具窗口选中测试项目的 origin 和主 GitHub 账号，并显示“账号身份已核实，GitHub 未允许访问此仓库”。Issue 列表为空。 | 身份核实及分阶段错误提示通过，私仓读取受阻。此轮未取得 #16/#17 或发布评论。[实机画面](pycharm-repository-denied.jpg) |
| 模型配置 | 在“模型设置”填写回环服务地址、模型 ID 和临时测试密钥，保持 JSON 对象模式，点击 Apply。模型地址和模型 ID 保留，密码框清空，Apply 按钮停用。 | 通过。[输入时遮挡密钥](pycharm-model-entered.jpg) · [应用后](pycharm-model-applied.jpg) |
| 切换模型地址 | 在密码框重新键入另一个临时值，把地址从 127.0.0.1 改为 localhost，未保存的密码框随编辑立即清空。应用该地址后，再把地址改回原回环地址并点击 OK。 | 界面对未提交密钥的清除通过。[切换后的表单](pycharm-model-switched-url.jpg) |
| 配置落盘 | 读取隔离配置中属于本插件的 group11IssueReviewModel.xml，只含回环服务地址和模型 ID。 | 实测配置文件未出现测试密钥。[实际配置文件](pycharm-model-options.xml) |

工具窗口未读取到真实私仓 Issue，本次 GUI 未触发模型请求。真实插件类加载器发出的回环协议请求另见[运行记录](../runtime/result.json)。
