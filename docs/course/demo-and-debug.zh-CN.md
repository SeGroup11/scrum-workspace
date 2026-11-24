# 线下演示与调试

使用小组私仓 [SeGroup11/scrum-workspace](https://github.com/SeGroup11/scrum-workspace)。A 是 [#16 导出文件为空](https://github.com/SeGroup11/scrum-workspace/issues/16)，B 是 [#17 导出搜索结果](https://github.com/SeGroup11/scrum-workspace/issues/17)。下列操作用于现场演示和复测。每次实际执行的证据见 [#14 候选验收](https://github.com/SeGroup11/scrum-workspace/issues/14)。

## 候选演示视频

`CS5351-Group11-demo-0.2.4.mp4` 共 **193 秒**，H.264、1280 × 720，中文字幕，SHA-256 为 `3f1a154eef6e9d351e9d2908ea65998d9d489bcb1c0b4914d107f9564e432be4`。影片使用实际执行的输出及有来源的窗口画面。只有 01:23–01:39 属于真实平台窗口在受控仓库中的观察日志摘录；其余带产品名的桌面段来自正常授权私仓，按屏幕实际采集顺序节选。

| 时间 | 可以当场核对的内容 |
| --- | --- |
| 00:00–01:04 | 从 0.2.4 安装包启动固定模型的 HTTP 请求与组件回放，展示 #17 输入、逐字引文、追问、Tab、改写及拒收伪造引文。两次实际请求是慢成功、错误引文。 |
| 01:04–01:23 | 0.2.1 的未知作者与计数未更新、0.2.2 的回执修复，以及 0.2.2 首次设为原生任务空窗与 0.2.3 稳定后的 #20。 |
| 01:23–01:39 | 对照两个版本的真实生产窗口观察日志。0.2.3 重检后是否仍选 B 为 false；0.2.4 同一步为 true。 |
| 01:39–02:12 | 0.2.4 普通 IDEA 读取 #17 原文与报告，依次预览、Tab、改写、显式发送及读回评论 7127681925。 |
| 02:12–02:34 | 0.2.4 普通 PyCharm 用 #16 运行慢模型，等待时改 B。发送后立即输入新稿，远端评论 7127797042 到达时新稿仍在。 |
| 02:34–02:49 | 仅加评论、临时改正文、逐字还原正文，在 #16 上核对同一份报告的时效提示。 |
| 02:49–03:01 | 正常 PyCharm 的 503、旧报告和草稿，以及手动重试后成功的真实帧。 |
| 03:01–03:13 | 普通 WebStorm 与 Rider 各向 #17 发出一次评论。回执分别为 7127850398、7127880975，#17 分别显示 5 和 6 条。 |

原始截图、毫秒级生产日志、回环模型计数和四张 GitHub 远端回执一并纳入候选证据归档。两仓实验及 R1 语义回放可按下述步骤重复。只有标明“实机截帧”的画面来自普通产品桌面。[四产品采集记录](../evidence/candidate-3cdfc4b/gui) · [独立远端回执](../evidence/candidate-3cdfc4b/github)

此前三段私有验收影片分别保留了实机发现与修复的过程。0.2.1 记录真实评论已落地但本地作者和数量错误；0.2.2 显示该问题修复后，首次激活无旧快照的原生任务又暴露空面板；0.2.3 记录任务首次激活修复、三款 IDE 的真实远端评论及同一报告随正文改变而过期。[0.2.1 历史视频与回执](https://github.com/SeGroup11/scrum-workspace/releases/tag/untagged-c616ca1c718dbf3bdfbf) · [0.2.2 历史视频与回执](https://github.com/SeGroup11/scrum-workspace/releases/tag/untagged-9ac4cd6d61db5fa5e910) · [0.2.3 历史视频与回执](https://github.com/SeGroup11/scrum-workspace/releases/tag/untagged-07d4af22255ffb0c13ac)

## 准备开发实例

要逐份复跑0.2.4 的 A/B 语义实验，在仓库根设置 `IDEA_HOME` 为 IntelliJ IDEA 的安装目录、`PLUGIN_ZIP` 为最终 ZIP，然后运行下方 PowerShell 7.4 命令。需要本机 Python 3，输出目录应为一个新目录。脚本核对 ZIP 摘要，从包内加载实际插件与 SDK，用已冻结的两条输入分别请求回环服务，逐份比较报告及输入摘要。成功会打印 PASS 和两条服务计数。[读取和比较两份报告的源码](../evidence/candidate-3cdfc4b/model/ReplayIssues.java)

```powershell
./docs/evidence/candidate-3cdfc4b/model/run.ps1 -Zip $env:PLUGIN_ZIP -IdeaHome $env:IDEA_HOME -Output ./build/semantic-replay
```

用 JDK 25 和仓库自带的 Gradle Wrapper。在 PowerShell 中从仓库根目录运行。若使用已安装的目标 IDE，将 `IDE_HOME` 设置为该产品的安装目录，再把 `"-PlocalPlatformPath=$env:IDE_HOME"` 放在 Gradle 命令的任务名前。首次解析依赖需要网络，之后可使用已有缓存。

```powershell
.\gradlew.bat test buildPlugin
.\gradlew.bat runIde
```

标准 ZIP 在 `build/distributions`。正式验收先锁定提交，只构建一次，在四款产品的 Plugins → Install Plugin from Disk 选择同一文件，核对 Help → About 与 SHA-256。开发实例通常有独立的配置目录，请在 IDE 内确认或登录小组测试账号。

```powershell
Get-FileHash -LiteralPath $env:PLUGIN_ZIP -Algorithm SHA256
.\scripts\verify-candidate.ps1 -PluginZip $env:PLUGIN_ZIP -Sha256 $env:CANDIDATE_SHA256 -VerifierJar $env:VERIFIER_JAR
```

本机受控模型只接收上述 A/B 的输入。它不访问云服务。开一个终端，运行下面的命令，在 Settings → Tools → GitHub Issue 质量评审填写 `http://127.0.0.1:8535/v1`、一个非空的测试密钥和下表的模型 ID。保存后，下一次分析会重新读取连接配置。用真实供应商做对照实验时，另行选择其地址与模型并为该地址设置自己的凭据。

```powershell
python src/test/resources/model/mock_provider.py
```

| 模型 ID | 固定行为 | 观察位置 |
| --- | --- | --- |
| `success` | 返回完整 v1 报告 | #17 验收标准行应引用“导出要快” |
| `slow-success` / `slow-error` | 等待 10 秒后成功 / 返回 HTTP 503 | 分别复测滚动、编辑、切 Issue；记录首帧和完成耗时 |
| `bad-json` / `bad-quote` | 损坏 JSON / 返回原文中不存在的引文 | 失败原因归原 Issue，原报告和草稿仍在 |
| `refusal` / `truncated` | 返回拒答 / 结束原因为 length | 全份判失败，保持草稿 |
| `timeout` | 请求保持 65 秒 | SDK 默认 60 秒超时；检查每次点击仅一次调用 |

## 现场演示和录制顺序

1. 打开工具窗口 **Issue Review**，选择正确的仓库与 IDE 账号，读取 A、B 的正文。展示 B 的原文和 GitHub 原始链接。记录 B 在发送前的评论数量。
2. 将模型设为 `slow-success`，在 B 双击 **分析**。在录制中标记第一次点击和“分析中”首个可见帧。等待时滚动正文，在草稿中输入文字，切到 A 编辑另一段草稿，并等 B 返回。分别记录首帧差值、模型总耗时以及模拟服务的调用数。
3. 切回 B 的 **评估报告**。展示验收标准的引文“导出要快”和追问数据规模、完成时间的原因。点 **用于回复**，让编辑框获得焦点后按 Tab，再手动改写一处。检查到这里 GitHub 评论数量未变。
4. 在 B 草稿末尾加入本次产品标记，显式点击 **发送评论**。随即继续编辑或切去 A。打开 GitHub 的 B，核对评论 ID、作者、数量，以及整段文字是否与点击瞬间一致。检查 A 无新增评论，B 在途新编辑保留。
5. 分别给 A、B 留含末字、空行和尾空格的草稿，切换页签，显式设为 IDE 任务。通过 IDE 任务切换回 A，正常退出并重开，再清空草稿并重复。比较逐字符结果，记录恢复期间模型、评论 POST 两项新增次数。
6. 把模型切为 `slow-error`，重复 B→A，返回后确认旧报告、新编辑与显式重试入口。视频尾声展示一次真实的失败检查与修复，再交代当前候选尚缺的实机或人工证据。

Demos 使用真实输入画面、匿名计时和专用测试评论。暂停录制后再填写凭据。给每个实机记录保留产品 build、插件版本、ZIP 摘要、评论 ID 和可复查的证据链接。需要停帧核时的录屏保留原始帧率。

## 两个受控仓库如何复现同窗选择丢失

在两个独立目录分别检出 0.2.3 与 0.2.4，取各自 Release 中的 ZIP。两个版本共用旧候选证据目录里的观察器。它使用真实 PyCharm 平台、生产工具窗口控件和 GitHub SDK 解码器；账号 one/two 和仓库 Alpha/Beta 由内存服务提供。每轮使用新输出目录。将下例中的环境变量指向正在复测的版本。[对照实验和脚本](../evidence/candidate-3f9c974/window-race) · [0.2.4 原始记录](../evidence/candidate-3cdfc4b/window-race/result.txt)

```powershell
./docs/evidence/candidate-3f9c974/window-race/run.ps1 -Zip $env:PLUGIN_ZIP -Sha256 $env:CANDIDATE_SHA256 -PyCharm $env:PYCHARM_HOME -Output ./build/dual-repo-replay
```

先让 Alpha #16 的详情和评论挂起，改选账号 two、Beta #17，输入 `Beta draft 17`，再让 Alpha 的两项响应返回。此时两版都应显示 Beta。点击“重新检测”，在 0.2.3 原始记录中查找 `same-window selected B survived rediscovery=false`，0.2.4 在同一步为 `true`。正常关闭并重开后，两版均返回 Beta #17 和原草稿。这个额外步骤能区分本次修复的同窗重检缺陷与项目重启恢复。[0.2.3 失败结果](../evidence/candidate-3f9c974/window-race/result.txt) · [0.2.4 通过结果](../evidence/candidate-3cdfc4b/window-race/result.txt)

## 复现一次已修缺陷

Sprint 1 的候选包曾因插件显示名称被 Verifier 拒绝。失败状态绑定到了提交 `ae3e8ee` 的 [Check Run](https://github.com/SeGroup11/scrum-workspace/runs/138262252679)。[修复提交 4bc2b85](https://github.com/SeGroup11/scrum-workspace/commit/4bc2b851a6d5f9effea2835b34705c8f59d12d64) 修改描述符的名称。另一组可以在独立检出中分别构建两个提交，记录各自的 ZIP SHA-256，再调用上面的验证脚本比较 Verifier 结果。脚本会核对四款实际 build 并将摘要写入 `build/verification`。请在 [#15](https://github.com/SeGroup11/scrum-workspace/issues/15) 记录复现者、两个 SHA、观察到的错误、修复后结果和原始日志链接。

## 故障定位

| 现象 | 检查点 | 恢复方式 |
| --- | --- | --- |
| 项目没有候选仓库或账号 | 当前 Git 根是否有匹配该 GitHub 服务器的 fetch remote；IDE GitHub 页面中是否已选择有权限的测试账号 | 调整 remote 或登录后点击“重新检测”，遇到多个候选时显式选择 |
| 所选账号连接失败 | 0.2.1 的连接反馈区分身份验证阶段与读取仓库阶段。注意当前仓库候选的项目短名与 remote 别名 | 身份阶段失败时点击“重新认证所选账号”，完成 IDE 的原生认证后重连。若身份已验证、仓库不可读，检查该账号的私仓访问权限与所选仓库 |
| 列表含 PR 或翻页后假空 | 调试 `GitHubGateway.list`、PR 标志和下一页；对照 `GitHubGatewayTest` 中实际解码过的 wire 夹具 | 重跑受控分页测试，再在相同私有仓库用目标账号对照 |
| 模型输出被拒 | 在 `ReportCodec.parse` 检查错误类别。对同一 B 依次使用 `success`、`bad-quote` 和 `success` | 检查旧报告是否保留，并显式重新分析。记录请求计数，无需复制完整模型报文 |
| 恢复了旧草稿或覆盖新编辑 | 检查账号、完整仓库键、Issue 号、draftRevision，以及是否由任务快照写入正文 | 重跑状态归约和 XML 往返测试，修复后逐字比对正常退出前后的实际编辑 |
| 点击发送后网络中断 | 查看“发布结果待确认”，在原 Issue 重新读取评论，核对 ID、作者、时间和正文 | 由用户确认已发布，或确认未找到并允许手动重发，保留待核对期间的新草稿 |

```powershell
.\gradlew.bat test --tests '*ReportCodecTest' --tests '*ModelGatewayTest'
.\gradlew.bat test --tests '*ReviewStateTest' --tests '*GitHubGatewayTest'
.\gradlew.bat test --rerun-tasks --tests '*SlowWorkflowTest'
```

最后一条命令启动独立的本机 HTTP 服务，分别运行约 10 秒的成功与 503 回合，并验证 Tab 采纳和报告展示。打开 `build/test-results/test/TEST-team.segroup11.review.SlowWorkflowTest.xml`，核对两轮累计请求计数依次为 1、2。比较绝对耗时时需保留本次测试机器与日志，另行为 GUI 首帧计时。[0.2.4 对照记录](../evidence/candidate-3cdfc4b/junit/TEST-team.segroup11.review.SlowWorkflowTest.xml)

验收行采用“用例 ID、提交与 ZIP、IDE/build/OS/依赖、输入和操作、预期、实测及计数、证据、结论和负责人”的顺序。设计原文见[架构 05](siyuan://blocks/20261123160601-o204afz)，三项质量目标见[上下文恢复](siyuan://blocks/20261121180458-hys1y8e)、[分析响应性](siyuan://blocks/20261121182410-g0nevmj)和[IDE 兼容性](siyuan://blocks/20261121230231-nlhar9k)。
