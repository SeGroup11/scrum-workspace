# 线下演示与调试

使用小组私仓 [SeGroup11/scrum-workspace](https://github.com/SeGroup11/scrum-workspace)。A 是 [#16 导出文件为空](https://github.com/SeGroup11/scrum-workspace/issues/16)，B 是 [#17 导出搜索结果](https://github.com/SeGroup11/scrum-workspace/issues/17)。下列操作用于现场演示和复测。每次实际执行的证据见 [#14 候选验收](https://github.com/SeGroup11/scrum-workspace/issues/14)。

## 已有受控演示片段

`CS5351-Group11-controlled-demo.mp4` 长 63 秒，1280 × 720，带中文字幕。构建目录位于 `build/evidence/course`。前半段使用 #17 的固定输入，通过候选插件实际运行一次 10 秒的慢成功调用；中段显示生产 Tab 动作把建议插入 Swing 测试编辑框，随后改写；后半段注入伪引文，检查草稿与上次成功报告仍在，并回查描述符失败与修复记录。视频 SHA-256 为 `74a441d77919111b4d22a9fb23812bbe67f7d350072a73847028c925d7028c5b`。真实 IDE 交互、GitHub 接收的评论和作者需要在专用 Issue 另行录制。

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
| 列表含 PR 或翻页后假空 | 调试 `GitHubGateway.list`、PR 标志和下一页；对照 `GitHubGatewayTest` 中实际解码过的 wire 夹具 | 重跑受控分页测试，再在相同私有仓库用目标账号对照 |
| 模型输出被拒 | 在 `ReportCodec.parse` 检查错误类别。对同一 B 依次使用 `success`、`bad-quote` 和 `success` | 检查旧报告是否保留，并显式重新分析。记录请求计数，无需复制完整模型报文 |
| 恢复了旧草稿或覆盖新编辑 | 检查账号、完整仓库键、Issue 号、draftRevision，以及是否由任务快照写入正文 | 重跑状态归约和 XML 往返测试，修复后逐字比对正常退出前后的实际编辑 |
| 点击发送后网络中断 | 查看“发布结果待确认”，在原 Issue 重新读取评论，核对 ID、作者、时间和正文 | 由用户确认已发布，或确认未找到并允许手动重发，保留待核对期间的新草稿 |

```powershell
.\gradlew.bat test --tests '*ReportCodecTest' --tests '*ModelGatewayTest'
.\gradlew.bat test --tests '*ReviewStateTest' --tests '*GitHubGatewayTest'
```

验收行采用“用例 ID、提交与 ZIP、IDE/build/OS/依赖、输入和操作、预期、实测及计数、证据、结论和负责人”的顺序。设计原文见[架构 05](siyuan://blocks/20261123160601-o204afz)，三项质量目标见[上下文恢复](siyuan://blocks/20261121180458-hys1y8e)、[分析响应性](siyuan://blocks/20261121182410-g0nevmj)和[IDE 兼容性](siyuan://blocks/20261121230231-nlhar9k)。
