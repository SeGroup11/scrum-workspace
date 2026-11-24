# 在真实 IDE 的插件类加载器中验证模型 SDK

在 Windows、PowerShell 7.4 及项目根目录中运行。先从候选交接记录取得 ZIP 和预期 SHA-256。脚本用本机 IDEA 的 JBR 25 编译测试观察器，为四个目标安装各建独立的 config、system、plugins 和 logs，加载同一插件 ZIP。观察器只在这些测试目录中安装。

```powershell
./scripts/probe-runtime.ps1 -PluginZip $zip -Sha256 $candidateSha256
```

安装位置不同时，通过 `-Idea`、`-WebStorm`、`-Rider` 和 `-PyCharm` 指定含 `product-info.json` 的目录。脚本按实际 build 核对目标。每次运行产生新的 `build/model-runtime/<时间>`。每个产品的 `result.txt` 包含 Kotlin、Jackson、OkHttp、OpenAI SDK 的类来源及协议结果。`request-1.json`、`request-2.json` 为 loopback 服务收到的原始请求。异常时查看同目录中的 `stderr.log`、`logs/idea.log` 和顶层 `result.json`。

若产品提示 `No valid license found`，在专用测试配置中通过 JetBrains 正常登录并激活，关闭使用该配置的 IDE，然后加 `-TestProfiles @{ WS = $webStormTestConfig; RD = $riderTestConfig }` 重试。参数值是已激活的专用配置目录。系统、插件和日志仍在新的测试目录。

观察器从插件的类加载器调用模型网关，显式传入本地服务地址和一次性夹具密钥。两次请求分别使用 JSON object 和 strict schema，都必须由 SDK 解析出 CSV 夹具的 feature 报告。脚本逐字核对 user message 的标题与正文，并核对请求数和输出模式。此试验覆盖无界面运行时和受控模型。界面首帧、焦点、账号、任务恢复、真实评论和供应商请求在验收矩阵中各自取证。
