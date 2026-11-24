# 线下演示与调试

使用小组私仓 [SeGroup11/scrum-workspace](https://github.com/SeGroup11/scrum-workspace)。A 是 [#16 导出文件为空](https://github.com/SeGroup11/scrum-workspace/issues/16)，B 是 [#17 导出搜索结果](https://github.com/SeGroup11/scrum-workspace/issues/17)。下列操作用于现场演示和复测。每次实际执行的证据见 [#14 候选验收](https://github.com/SeGroup11/scrum-workspace/issues/14)。

## 0.2.2 实测视频

`CS5351-Group11-demo-0.2.2.mp4` 长 **138 秒**，1280 × 720，中文字幕，SHA-256 为 `adef4ec003f90aa8a2529700ff8b34ffce5dc6ae51c252feded05fd794133568`。直接从锁定 ZIP 加载模型 SDK 与插件运行受控段。真实窗口截图按其来源版本标注，桌面采样段保留原采集间隔。

| 时间 | 内容 |
| --- | --- |
| 00:00–01:03 | 0.2.2 受控回放。#17 的固定输入、逐字引文、追问、Tab 和改写，随后注入伪引文，检查草稿和旧报告。 |
| 01:03–01:27 | 0.2.1 历史对照。WebStorm 收到真实 #17 回执 7125780145，但作者未知且计数仍是 0。 |
| 01:27–02:11 | 0.2.2 PyCharm 实机。#16 读到作者和创建时间，慢分析期间切 B 编辑，采纳后改写并显式发送。服务器与工具窗口均返回评论 7126271956，计数为 1，在途新草稿保留。 |
| 02:11–02:18 | 0.2.2 首次激活原生任务时，右侧面板清空。其后经原生菜单返回 A、B 的结果另行记录。 |

逐帧核对时，在工具窗口中对照选中 Issue、讨论区、计数、编辑框和状态提示，再比对 GitHub 回执中的作者、时间和正文。[PyCharm 实机证据目录](../evidence/candidate-267063f/gui/pycharm-live) · [0.2.2 独立服务器回执](../evidence/candidate-267063f/github/pycharm-receipt.json)

## 0.2.1 历史演示视频

`CS5351-Group11-demo-0.2.1.mp4` 长 133 秒，1280 × 720，带中文字幕。构建目录位于 `build/evidence/course`，SHA-256 为 `b047a860e64b9c745e4bf6d7b21554cd2ea787164f6f82ed4b667cfeda94af54`。

| 时间 | 画面及其来源 |
| --- | --- |
| 00:00–01:04 | 受控 HTTP 与 Swing 测试回放。#17 的固定输入经过候选网关，展示逐字引文、Tab 插入、改写与伪引文失败后的草稿保留。 |
| 01:04–01:35 | WebStorm 0.2.1 实机截取。显示组织授权前的拒绝、授权后的 #17 原文、显式发送前后的草稿与回执。GitHub 读回 ID 7125780145、作者和全文相符；IDE 当时仍显示未知作者和零评论。 |
| 01:35–02:13 | PyCharm 0.2.1 实机截图与采样时间序列。展示慢成功、503 后旧报告、实际 Tab 采纳，以及重试时切去 B 编辑。十秒等待按原始截图间隔播放。 |

截图字幕标出产品与采集时间，模型回复来自本机受控服务。可通过原始时间戳、生产日志和 GitHub 服务器数据复核相应片段。[WebStorm 窗口](../evidence/candidate-e66ed31/gui/webstorm.md) · [远端发评回执](../evidence/candidate-e66ed31/github/webstorm-receipt.json) · [PyCharm 手动操作与计时](../evidence/candidate-e66ed31/gui/pycharm-live/README.md)

同一候选另有两个独立 PyCharm 进程的退出与恢复实验，归档了真实工作区 XML、旧 Tasks 快照及模型 1/0 次的请求计数。两个进程使用本地测试项目，GitHub 请求未计数。该实验的记录和命令随证据包提供。[恢复实验与复跑方法](../evidence/candidate-e66ed31/state-restart/README.md)

## 准备开发实例

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

最后一条命令启动独立的本机 HTTP 服务，分别运行约 10 秒的成功与 503 回合，并验证 Tab 采纳和报告展示。打开 `build/test-results/test/TEST-team.segroup11.review.SlowWorkflowTest.xml`，核对两轮累计请求计数依次为 1、2。比较绝对耗时时需保留本次测试机器与日志，另行为 GUI 首帧计时。[0.2.1 对照记录](../evidence/candidate-e66ed31/junit/TEST-team.segroup11.review.SlowWorkflowTest.xml)

验收行采用“用例 ID、提交与 ZIP、IDE/build/OS/依赖、输入和操作、预期、实测及计数、证据、结论和负责人”的顺序。设计原文见[架构 05](siyuan://blocks/20261123160601-o204afz)，三项质量目标见[上下文恢复](siyuan://blocks/20261121180458-hys1y8e)、[分析响应性](siyuan://blocks/20261121182410-g0nevmj)和[IDE 兼容性](siyuan://blocks/20261121230231-nlhar9k)。
