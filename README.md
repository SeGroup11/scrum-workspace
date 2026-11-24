# GitHub Issue Quality Review

CS5351 Group 11 的 JetBrains 插件。打开项目中的 **Issue Review** 窗口，选定 GitHub 仓库和 IDE 账号，即可浏览 Issue，在原文旁查看六项质量检查，并把改写后的建议作为评论发回当前 Issue。

## 安装与使用

1. 在目标 IDE 的 **Settings → Plugins → Install Plugin from Disk** 选择小组候选 ZIP。加载后打开该仓库的 Git 项目和 **View → Tool Windows → Issue Review**。
2. 在 IDE 的 GitHub 设置登录有项目读取权限的账号。工具窗口中选取 fetch remote 和账号，点击“连接”。账号登录过期时，点击“重新认证所选账号”更新当前绑定。若窗口提示身份已核实但仓库访问被拒绝，请检查目标组织对 JetBrains IDE Integration 的授权。连接后可以按状态、标题或 `#编号` 浏览，讨论页按需加载评论。
3. 在 **Settings → Tools → GitHub Issue 质量评审** 填写服务地址、模型 ID、密钥、响应模式与预算。密钥按服务地址存入 IDE 凭据库。一次分析只向配置好的服务提交所选 Issue 已加载的标题与正文。
4. 检查报告中的引文和理由。点击“用于回复”后在编辑框按 Tab 采纳，继续修改，最后点击“发送评论”。如果发布结果待确认，请先点击“核对原 Issue 的评论”。

## 开发与调试

需要 JDK 25。Windows 可复用目标 IDE 的 JBR 与本地 SDK。将 `IDE_HOME` 指向含 `product-info.json` 的安装目录后运行下列命令。

```powershell
$env:JAVA_HOME = Join-Path $env:IDE_HOME 'jbr'
.\gradlew.bat "-PlocalPlatformPath=$env:IDE_HOME" test buildPlugin
.\gradlew.bat "-PlocalPlatformPath=$env:IDE_HOME" runIde
```

分发 ZIP 在 `build/distributions/`。验收使用四个固定 build，模型测试提供独立回环服务、延迟和故障注入。参见[演示与故障复现](docs/course/demo-and-debug.zh-CN.md)和[故事与证据](docs/evidence/index.md)。GitHub Actions 定义在 `scripts/github-actions-build.yml.example`。仓库 dev App 对活动工作流没有写权限，当前运行记录由本地 Gradle 与按提交登记的 QA 检查提供。

已构建的安装包可交给 Gradle 的 `verifyPlugin` 直接检查。`verificationArchive` 指向 ZIP，`verificationSha256` 传入交付时的摘要。四款参考版本已在构建脚本配置；如使用本机安装，在命令中加入 `-PverificationIUPath`、`-PverificationWSPath`、`-PverificationRDPath`、`-PverificationPYPath`，每项赋值为对应安装目录。离线时可再传 `-PverificationCli` 指向本地的 Verifier JAR。[JetBrains 任务说明](https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-tasks.html#verifyplugin)

```powershell
.\gradlew.bat verifyPlugin "-PverificationArchive=$zip" "-PverificationSha256=$sha256"
```

Gradle Wrapper 源于 [JetBrains IntelliJ Platform Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template)，许可证见 [LICENSE](LICENSE)。
