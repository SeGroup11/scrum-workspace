# 0.2.1 候选验收矩阵

候选源码为 [e66ed31](https://github.com/SeGroup11/scrum-workspace/commit/e66ed311cf986d876814f7737f897b526322236a)。安装包 github-issue-review-0.2.1.zip 共 75,389,798 字节，SHA-256 为 71c549a8203edb80060d37411ff344a32b10cec534982121163fe920c52b4cd4。测试日期为 2026-11-24，环境为 Windows NT 10.0.26300.0、Gradle 9.8.1、JBR 25、OpenAI Java 4.112.0。

从该提交的干净归档执行 test buildPlugin，19 个 Gradle 任务均执行。5 个套件共 21 个测试通过，失败、错误、跳过均为 0。受控成功响应耗时 10,811 ms，受控 HTTP 503 耗时 10,029 ms，每轮调用一次模型。下表列出每个用例已观测的行为与仍待单独实测的条件。500 ms 指标需测量从实际点击到窗口绘出反馈的时间。[Gradle 与原始 JUnit XML](candidate-e66ed31/junit) · [QA Check](https://github.com/SeGroup11/scrum-workspace/runs/138292777291) · [0.2.0 原始矩阵](acceptance-0.2.0.md)

| 用例与主责组 | 输入及预期 | 实测范围、原始证据 | 尚待验收的条件 |
| --- | --- | --- | --- |
| G1 · 平台与 GitHub | 两个仓库、两个账号、权限和限流；只读所选仓库 | 受控身份、remote、数字 ID、身份/仓库不同提示及取消与确认均已测。WebStorm 实机识别主账号，准确提示组织授权阻止读取。[实机](candidate-e66ed31/gui/webstorm.md) · [网关测试](candidate-e66ed31/junit/TEST-team.segroup11.review.github.GitHubGatewayTest.xml) | 切换与移除账号、真实私仓读回 |
| I1 · 平台与 GitHub | 整页 PR 后仍能找到 Issue；列表评论 GET=0；A 晚到不覆盖 B | SDK 解码带 Link 的两页回放，整页 PR 被过滤，评论 GET 单独计数。[网关测试](candidate-e66ed31/junit/TEST-team.segroup11.review.github.GitHubGatewayTest.xml) | 真实列表字段、快速切换、刷新后的选中行 |
| I2 · 窗口与 Tasks | 空正文与未加载可辨；评论次页失败保留正文，按 ID 去重 | 空正文、缺作者、评论次页失败及重试有受控断言。[网关测试](candidate-e66ed31/junit/TEST-team.segroup11.review.github.GitHubGatewayTest.xml) | 实际私仓正文、富文本/窄窗及评论区走查 |
| 设置专项 · 报告与模型 | 跨项目共享，Apply/OK 后切地址只使用对应凭据；缺配置可浏览编辑 | PyCharm 实际 Apply 后地址和模型已落盘，切地址清掉未提交密钥；插件配置 XML 中无明文密钥。[PyCharm 原始记录](candidate-e66ed31/gui/pycharm.md) · IU/PY 在本次候选中各使用实际 SDK 发出两次回环请求，分别完成两种模式。[原始记录](candidate-e66ed31/runtime/result.json) | 复开第二项目、切换地址后按相应密钥执行模型请求、缺配置浏览编辑 |
| R1 · 报告与模型 | 六项四态、有效类型、逐字引文；“导出要快”追问规模和耗时 | 使用固定输入与报告、结构化模式检查字段。新修复显示分类原因并能展开全部六项。[解析测试](candidate-e66ed31/junit/TEST-team.segroup11.review.model.ReportCodecTest.xml) · [报告展示与慢请求](candidate-e66ed31/junit/TEST-team.segroup11.review.SlowWorkflowTest.xml) | 独立人工标注、真实供应商语义分歧与解析率分别统计 |
| R2 · 报告与模型 | 多余/重复字段、伪引文、拒答、截断和超时保留旧报告 | 解析负例、受控 HTTP 失败、旧报告与编辑合并均有断言。[模型网关](candidate-e66ed31/junit/TEST-team.segroup11.review.model.ModelGatewayTest.xml) · [状态归约](candidate-e66ed31/junit/TEST-team.segroup11.review.state.ReviewStateTest.xml) | 真实 IDE 的失败提示、重试与供应商行为 |
| N1a · 状态与测试 | 切换与正常重启保留最后一字、空白、主动清空的草稿、报告和页签；零自动提交 | XML/JDOM 字节往返、跨仓同号/账号隔离已测。[状态测试](candidate-e66ed31/junit/TEST-team.segroup11.review.state.ReviewStateTest.xml) | IDE 正常退出重开逐字符核对，记录恢复期 GET/POST 与模型调用 |
| N1b · 状态与测试 | 旧快照只恢复指针；正文修改令报告过时，仅新增评论不影响 | 恢复旧指针、草稿/报告合并及标题正文 hash 有断言。[状态测试](candidate-e66ed31/junit/TEST-team.segroup11.review.state.ReviewStateTest.xml) | 真实 Tasks 往返、正文/仅评论两种刷新对照 |
| N2a · 状态与测试 | 延迟成功，仅一请求；首帧 ≤500 ms，等待时可滚动、编辑、切换 | 受控 HTTP 成功 10,811 ms。等待期间在 EDT 编辑 A/B 并切换，结果归 A。[慢请求测试](candidate-e66ed31/junit/TEST-team.segroup11.review.SlowWorkflowTest.xml) | 可见首帧、滚动与焦点实测 |
| N2b · 状态与测试 | 10 秒后失败保留旧报告、新草稿，允许显式重试 | 受控 503 耗时 10,029 ms，同键去重、归属和旧报告有断言。[慢请求测试](candidate-e66ed31/junit/TEST-team.segroup11.review.SlowWorkflowTest.xml) | 实机错误首帧与三项交互及恢复后的显式重试 |
| C1 · 窗口与 Tasks | 预览/Tab 零 POST；显式发送只发点击时文字，保留新编辑 | 真实 Swing 编辑组件保留选区，连续采纳两条；状态与回放验证修订及目标。[编辑测试](candidate-e66ed31/junit/TEST-team.segroup11.review.SlowWorkflowTest.xml) · [状态测试](candidate-e66ed31/junit/TEST-team.segroup11.review.state.ReviewStateTest.xml) | 以 IDE 账号核对专用 Issue 的评论 ID、作者、条数和逐字正文 |
| C2 · 状态与测试 | 明确拒绝可显式重试；回执未知时先 GET、由用户核对和决定 | 回放 I/O 错误、丢回执、重开期间无自动重发及人工决策门槛已测。[网关](candidate-e66ed31/junit/TEST-team.segroup11.review.github.GitHubGatewayTest.xml) · [状态](candidate-e66ed31/junit/TEST-team.segroup11.review.state.ReviewStateTest.xml) | 真实发评结果对账与重启交互；本轮未以 Scrum 角色代发产品评论 |
| N3 · 四组 | 同一 ZIP 在四个精确 build 验证兼容并执行五组实操 | 同一 ZIP 四份 Compatible，每款报告 5 处弃用及 6 处实验 API。WebStorm 从磁盘更新后正常重启并打开工具窗口。[Verifier](candidate-e66ed31/verifier/result.json) · [实机](candidate-e66ed31/gui/webstorm.md) | 四款安装、私仓、慢分析、任务重启、远端评论五组全部取证 |

## 四款 IDE 的实际运行范围

| 构建 | Plugin Verifier | 本次运行观察 |
| --- | --- | --- |
| IU-262.10315.125 | Compatible | 同包的模型 SDK 在 IDE 内完成两种模式，各有一次回环请求。 |
| WS-262.10315.144 | Compatible | 正常配置从磁盘更新 0.2.1、重启并打开工具窗口。所选主账号身份已核实，组织当时限制访问私仓。隔离配置在许可证检查阶段退出。 |
| RD-262.9437.287 | Compatible | 隔离配置在许可证检查阶段退出，尚无该配置中的模型或窗口验收结果。 |
| PY-262.9437.214 | Compatible | 磁盘安装并重启，复现仓库授权提示。已操作原生模型设置的 Apply 和地址切换。IDE 内运行了两种回环响应模式。[工具窗口](candidate-e66ed31/gui/pycharm-repository-denied.jpg) · [设置](candidate-e66ed31/gui/pycharm-model-applied.jpg) |

四份原始 verdict、逐产品依赖与 API 警告、各运行时观察均按候选 ZIP 的 SHA-256 归档。[四款报告](candidate-e66ed31/verifier) · [四款 QA Check](https://github.com/SeGroup11/scrum-workspace/runs/138294759169) · [运行时证据](candidate-e66ed31/runtime) · [运行时范围 Check](https://github.com/SeGroup11/scrum-workspace/runs/138294767613)

## 主账号与测试 Issue

在 0.2.0 中，WebStorm 与独立 PyCharm 配置均能选择主账号 material4002，连接目标私仓时显示笼统的认证失败。WebStorm 的原生 GitHub 窗口重新认证后仍提示仓库不可见或访问被拒绝。登录该主账号后，在 GitHub 的 JetBrains IDE Integration 授权详情中实见 SeGroup11 为 Restricted。0.2.1 修复原账号重新认证入口，并分别提示账号身份核验、仓库授权和后续操作错误。[WebStorm 基线证据](candidate-2974097/gui/webstorm.md) · [修复提交](https://github.com/SeGroup11/scrum-workspace/commit/380be1b)

QA 在 0.2.1 实机测试前读回专用 Issue #16 与 #17，两者均为打开状态、各有 0 条评论。WebStorm 与 PyCharm 首次仓库授权失败后，再次分别读取两张 Issue 及评论列表，仍各为 0。此次结果尚无显式发送的回执。[初始快照](candidate-e66ed31/github/before.json) · [授权失败后复核](candidate-e66ed31/github/after-repository-denial.json) · [实机 Check](https://github.com/SeGroup11/scrum-workspace/runs/138297958418)

## 语义与人工验收

0.2.1 重新用固定模型处理 A/B 的 GitHub 原文，每个输入实际发送一次回环 POST，两份报告均成功解析，各有六项。复核 A 时发现原文已有两步，报告仍判复现步骤缺失。B 的引文和追问可逐字核对。真人标注、学生独立复现与真实供应商输出须分别提供原始记录。[0.2.1 原始语义实验](candidate-e66ed31/model/semantic-replay.json) · [0.2.0 对照](candidate-2974097/model/semantic-replay.json)
