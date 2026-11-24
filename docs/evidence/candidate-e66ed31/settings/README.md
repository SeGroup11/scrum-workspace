# PyCharm 设置、凭据与双项目验证

在 Windows 的 PyCharm `PY-262.9437.214`、全新隔离配置中挂载同一 `0.2.1` ZIP。原生日志列出候选及独立测试观察器。观察器生成随机的回环测试地址路径和临时密钥，仅访问该测试生成的两个凭据条目，并在结束前删除这两个条目。运行期间没有发送 HTTP 请求。[加载日志](startup.log) · [观察器源码](SettingsProbe.java)

1. 候选的应用级 ModelSettings 通过 IDE PasswordSafe 保存地址 A 的测试密钥，随后解析模型配置，实际取回相同值。
2. 将主机名由 127.0.0.1 换成 localhost，保留相同端口和路径，留空密钥并保存。候选抛出“请在设置中为此服务地址保存 API 密钥”。
3. 切回 A，保留空密钥并保存。候选重新取回 A 的原值。
4. 用 PyCharm ProjectManager 创建两个不同的临时项目，分别从平台的设置注册表查找“GitHub Issue 质量评审”，在事件线程创建并重置表单。两份表单均显示 A 的服务地址和 slow-success 模型 ID。

以上四步在 15:55 运行通过。[实际输出](result.txt) 公共模型字段的真实桌面输入、Apply 和保存结果另见 [GUI 记录](../gui/pycharm.md)。双项目检查采用真实 IDE 平台无界面实例。

首次尝试的测试观察器遗漏平台模块依赖，PyCharm 在调用观察器之前报告不支持该 headless 命令。向观察器声明 com.intellij.modules.platform 后重新运行得到上述结果。期间候选 ZIP 和哈希未改变。

使用 PowerShell 7.4 或更新版本复跑，将环境变量 PYCHARM_HOME 设为本机 PY-262.9437.214 的安装目录，传入同一 ZIP 及测试输出目录。脚本在启动前核对 ZIP 的 SHA-256。

```powershell
./docs/evidence/candidate-e66ed31/settings/run.ps1 -Zip ./github-issue-review-0.2.1.zip -PyCharm $env:PYCHARM_HOME -Output ./build/model-settings-check
```
