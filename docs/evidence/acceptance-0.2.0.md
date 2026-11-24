# 0.2.0 候选验收矩阵

候选提交 [2974097](https://github.com/SeGroup11/scrum-workspace/commit/29740976e138e3e3566c1d222e5963f4c4bd5c54)。`github-issue-review-0.2.0.zip` 共 75,386,326 字节，SHA-256 为 `e0b10cede8dc310990fc1dad747cf46a9c6319a4cb1c89db9e40156cc394a0d3`。下表各行均适用这份源码与候选包。实际执行日期为 2026-11-24。环境为 Windows NT 10.0.26300.0、Gradle 9.8.1、JBR 25、OpenAI Java 4.112.0 和 Plugin Verifier 1.411。原始结果见[JUnit](candidate-2974097/junit)、[四产品逐构建报告](candidate-2974097/verifier/result.json)和[实际 IDE 类加载器记录](candidate-2974097/runtime/result.json)。

`test buildPlugin` 从干净源码归档执行，19 个 Gradle 任务均实际执行，20 个 JUnit 测试通过，无失败或跳过。模型慢响应测试中，首次成功耗时 10,782 ms，第二轮 503 耗时 10,023 ms，各产生一次模型 POST。两项时间的起点和终点是测试发起及模型调用完成。500 ms 指标以显示分析中的实际绘制帧为终点，须另测。[自动回归与 Verifier Check](https://github.com/SeGroup11/scrum-workspace/runs/138281512509) · [IDE 运行时范围 Check](https://github.com/SeGroup11/scrum-workspace/runs/138281520633)

| 用例与主责组 | 输入及预期 | 实测范围、原始证据 | 尚待验收的条件 |
| --- | --- | --- | --- |
| G1 · 平台与 GitHub | 两个仓库、两个账号、权限和限流；只读所选仓库 | 受控身份、remote、数字 ID、错误分支已测。[网关测试](candidate-2974097/junit/TEST-team.segroup11.review.github.GitHubGatewayTest.xml) | 实际账号选择、切换、移除、真实私仓读回 |
| I1 · 平台与 GitHub | 整页 PR 后仍能找到 Issue；列表评论 GET=0；A 晚到不覆盖 B | SDK 解码带 Link 的两页回放，整页 PR 被过滤，评论 GET 单独计数。[网关测试](candidate-2974097/junit/TEST-team.segroup11.review.github.GitHubGatewayTest.xml) | 真实列表字段、快速切换、刷新后的选中行 |
| I2 · 窗口与 Tasks | 空正文与未加载可辨；评论次页失败保留正文，按 ID 去重 | 空正文、缺作者、评论次页失败及重试有受控断言。[网关测试](candidate-2974097/junit/TEST-team.segroup11.review.github.GitHubGatewayTest.xml) | 实际私仓正文、富文本/窄窗及评论区走查 |
| 设置专项 · 报告与模型 | 跨项目共享，Apply/OK 后切地址只使用对应凭据；缺配置可浏览编辑 | 受控 SDK 的两种模式和容量拦截已测；IU/PY 内实跑同包模型客户端。[原始记录](candidate-2974097/runtime/result.json) | 两项目原生面板、地址切换和密钥匹配实机验证 |
| R1 · 报告与模型 | 六项四态、有效类型、逐字引文；“导出要快”追问规模和耗时 | 使用固定输入与报告、结构化模式检查字段。新修复显示分类原因并能展开全部六项。[解析测试](candidate-2974097/junit/TEST-team.segroup11.review.model.ReportCodecTest.xml) · [报告展示与慢请求](candidate-2974097/junit/TEST-team.segroup11.review.SlowWorkflowTest.xml) | 独立人工标注、真实供应商语义分歧与解析率分别统计 |
| R2 · 报告与模型 | 多余/重复字段、伪引文、拒答、截断和超时保留旧报告 | 解析负例、受控 HTTP 失败、旧报告与编辑合并均有断言。[模型网关](candidate-2974097/junit/TEST-team.segroup11.review.model.ModelGatewayTest.xml) · [状态归约](candidate-2974097/junit/TEST-team.segroup11.review.state.ReviewStateTest.xml) | 真实 IDE 的失败提示、重试与供应商行为 |
| N1a · 状态与测试 | 切换与正常重启保留最后一字、空白、主动清空的草稿、报告和页签；零自动提交 | XML/JDOM 字节往返、跨仓同号/账号隔离已测。[状态测试](candidate-2974097/junit/TEST-team.segroup11.review.state.ReviewStateTest.xml) | IDE 正常退出重开逐字符核对，记录恢复期 GET/POST 与模型调用 |
| N1b · 状态与测试 | 旧快照只恢复指针；正文修改令报告过时，仅新增评论不影响 | 恢复旧指针、草稿/报告合并及标题正文 hash 有断言。[状态测试](candidate-2974097/junit/TEST-team.segroup11.review.state.ReviewStateTest.xml) | 真实 Tasks 往返、正文/仅评论两种刷新对照 |
| N2a · 状态与测试 | 延迟成功，仅一请求；首帧 ≤500 ms，等待时可滚动、编辑、切换 | 受控 HTTP 成功 10,782 ms。等待期间在 EDT 编辑 A/B 并切换，结果归 A。[慢请求测试](candidate-2974097/junit/TEST-team.segroup11.review.SlowWorkflowTest.xml) | 可见首帧、滚动与焦点实测 |
| N2b · 状态与测试 | 10 秒后失败保留旧报告、新草稿，允许显式重试 | 受控 503 耗时 10,023 ms，同键去重、归属和旧报告有断言。[慢请求测试](candidate-2974097/junit/TEST-team.segroup11.review.SlowWorkflowTest.xml) | 实机错误首帧与三项交互及恢复后的显式重试 |
| C1 · 窗口与 Tasks | 预览/Tab 零 POST；显式发送只发点击时文字，保留新编辑 | 真实 Swing 编辑组件保留选区，连续采纳两条；状态与回放验证修订及目标。[编辑测试](candidate-2974097/junit/TEST-team.segroup11.review.SlowWorkflowTest.xml) · [状态测试](candidate-2974097/junit/TEST-team.segroup11.review.state.ReviewStateTest.xml) | 以 IDE 账号核对专用 Issue 的评论 ID、作者、条数和逐字正文 |
| C2 · 状态与测试 | 明确拒绝可显式重试；回执未知时先 GET、由用户核对和决定 | 回放 I/O 错误、丢回执、重开期间无自动重发及人工决策门槛已测。[网关](candidate-2974097/junit/TEST-team.segroup11.review.github.GitHubGatewayTest.xml) · [状态](candidate-2974097/junit/TEST-team.segroup11.review.state.ReviewStateTest.xml) | 真实发评结果对账与重启交互；本轮未以 Scrum 角色代发产品评论 |
| N3 · 四组 | 同一 ZIP 在四个精确 build 验证兼容并执行五组实操 | 四份 Compatible，每款 5 处弃用和 6 处实验 API；IU/PY 实跑两种回环模型模式。[逐 IDE 原始报告](candidate-2974097/verifier) | 四款安装、私仓、慢分析、任务重启、远端评论五组全部取证 |

## 实机交接

| IDE build | 二进制 | 运行时模型 | 五组可见窗口实操 |
| --- | --- | --- | --- |
| IU-262.10315.125 | Compatible | 实际 IDE 内的两种模式各发一次回环请求 | 待实测 |
| WS-262.10315.144 | Compatible | 隔离配置启动观察器前提示 No valid license found | 正常配置从磁盘安装成功，已打开本仓库的 Issue Review 窗口。首次使用已配置账号时界面显示 GitHub 身份验证失败，后续操作见[WebStorm 实测](candidate-2974097/gui/webstorm.md)。 |
| RD-262.9437.287 | Compatible | 隔离配置启动观察器前提示 No valid license found | 待实测 |
| PY-262.9437.214 | Compatible | 实际 IDE 内的两种模式各发一次回环请求 | 图形进程从隔离插件目录加载了同一 ZIP 的 0.2.0，并打开测试项目。[启动证据](candidate-2974097/gui/pycharm-startup.json)。工具窗口交互待验。 |

四款的 Git4Idea、GitHub 和平台 Tasks 模块各自采用对应产品 build 的版本。逐项依赖标识与原始依赖报告指纹见[四款构建依赖记录](candidate-2974097/gui)。

在真实夹具 A/B 的原文上，受控模型各请求一次，协议解析 2/2 成功。逐项对照发现 A 正文已经列出两步，受控报告仍把复现步骤判为缺失。B 的“导出要快”引文能定位，追问同时涉及规模和时限。实验覆盖 12 个判断项，这一自动化复核不计入真人标注率。[语义复核原始记录](candidate-2974097/model/semantic-replay.json)

机器可判定的分项使用原始 XML、逐产品 verdict 与 Check 作证。可见窗口的测试计时、实际 IDE 账号及评论回执以单独的操作记录为准。另一组复现、人工语义评审和用户反馈等待真实参与者提供。
