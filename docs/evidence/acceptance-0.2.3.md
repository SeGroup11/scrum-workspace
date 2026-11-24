# 0.2.3 候选验收矩阵

2026-11-24 在 Windows NT 10.0.26300.0 上，从 [3f9c974b84a716ffbf8aa6c04d732601c0e3b4e0](https://github.com/SeGroup11/scrum-workspace/commit/3f9c974b84a716ffbf8aa6c04d732601c0e3b4e0) 的干净源码归档构建一次。唯一安装包 github-issue-review-0.2.3.zip 共 75,393,438 字节，SHA-256 为 d0e5c1af07918baa377a0a81151dabaace0ade7f5d7c5617a4b447315a24000f。Gradle 9.8.1 与 JBR 25 运行 test buildPlugin，19 项任务实际执行，6 分 30 秒结束。六个套件共 26 项通过，失败、错误、跳过均为 0。模型客户端为 OpenAI Java 4.112.0。[归档、描述符与 JUnit XML](candidate-3f9c974/junit) · [QA Check](https://github.com/SeGroup11/scrum-workspace/runs/138328199869)

A 是打开的缺陷 [#16 导出文件为空](https://github.com/SeGroup11/scrum-workspace/issues/16)，B 是打开的功能需求 [#17 导出搜索结果](https://github.com/SeGroup11/scrum-workspace/issues/17)。两张专用 Issue 在 0.2.3 实测开始时各有 1 条既有评论，分别源于 0.2.2 PyCharm 和 0.2.1 WebStorm。该基线和后续的逐字回执均由独立的只读 GitHub API 查询取得。[0.2.3 基线](candidate-3f9c974/github/before.json) · [0.2.2 矩阵](acceptance-0.2.2.md) · [0.2.1 矩阵](acceptance-0.2.1.md) · [0.2.0 矩阵](acceptance-0.2.0.md)

## 逐项验收

| 用例、主责 | 输入与通过条件 | 同包的已测行为与证据 | 补充验收范围 |
| --- | --- | --- | --- |
| G1 · 平台与 GitHub | 选择并记住仓库和 IDE 账号；权限失败与晚到事件可恢复 | SDK 回放验证数字身份、账号移除、HTTP 拒绝与限流。启动前后仓库映射事件合并重查，已有选择保持。[网关测试](candidate-3f9c974/junit/TEST-team.segroup11.review.github.GitHubGatewayTest.xml) · [启动回归](candidate-3f9c974/junit/TEST-team.segroup11.review.github.RepositoryDiscoveryTest.xml) | 正常产品使用已获授权的主账号；双账号及第二仓库情形属于受控覆盖 |
| I1 · 平台与 GitHub | PR 整页过滤后找到下一页的 Issue；列表评论 GET=0 | 真实 SDK 解码分页形态，逐请求记录列表与评论，使用远端原始编号、状态和日期。[网关回归](candidate-3f9c974/junit/TEST-team.segroup11.review.github.GitHubGatewayTest.xml) | 真私仓的固定 A/B 与合成的整页 PR 分别核验 |
| I2 · 窗口与 Tasks | 空正文、未加载、评论分页失败可分辨，去重按远端 ID | 受控缺作者/日期、空正文、评论第二页失败与重试通过，真实 #17 的 API 形态能映射到正确字段。[SDK 回放](candidate-3f9c974/junit/TEST-team.segroup11.review.github.GitHubGatewayTest.xml) | 深分页与空正文的反例采用受控数据；正常窗口记录另见下表 |
| 设置专项 · 报告与模型 | 两项目共享，Apply 后生效，换地址匹配对应密钥，缺配置仍可浏览编辑 | 同包已在真实 PyCharm 的两个项目读回相同表单值，受控地址 A 有密钥、B 缺密钥、回 A 恢复原值。[实际设置、凭据与复跑](candidate-3f9c974/settings) | 0.2.2 的正常 IDEA 曾以空模型配置读写私仓草稿；同包的可见配置与模型调用另列 |
| R1 · 报告与模型 | 六项四态、逐字原文、建议追问规模和时限 | 本包以 A/B 原文向本机确定性服务各发一次，两份六项报告解析成功。B 的“导出要快”引文正确；A 有两步，固定结果却判为缺失。[协议回归](candidate-3f9c974/junit/TEST-team.segroup11.review.model.ReportCodecTest.xml) · [原始模型结果](candidate-3f9c974/model/semantic-replay.json) | 协议通过率、真人标注与外部模型输出分别统计 |
| R2 · 报告与模型 | 额外/重复键、拒答、截断、假引文、超时不覆盖旧内容 | 模型与解析负例只调用一次，失败保留旧报告和后续编辑。[模型测试](candidate-3f9c974/junit/TEST-team.segroup11.review.model.ModelGatewayTest.xml) · [受控慢失败](candidate-3f9c974/junit/TEST-team.segroup11.review.SlowWorkflowTest.xml) | 可见窗口的 503 与用户重试另计 |
| N1a · 状态与测试 | 重启保留末字、换行、尾空格、主动清空、页签及报告；无自动发送/分析 | 状态 XML/JDOM 往返、账户与仓库隔离通过。真实 Tasks 宿主验证初次 A/B 与切回 A/B 均保持两份不同草稿和目标视图。[状态](candidate-3f9c974/junit/TEST-team.segroup11.review.state.ReviewStateTest.xml) · [原生 Tasks](candidate-3f9c974/task-activation) | 真实两进程重启的回环计数和可见 IDE 重开分别观测 |
| N1b · 状态与测试 | 旧任务仅恢复位置，正文变化才标旧 | 已注册原生 TaskManager 和生产激活入口实证，首次及已有快照 A/B 均正确。旧任务 XML 只恢复定位器，新的草稿与报告从项目记录读取。[冷任务回归](candidate-3f9c974/task-activation/result.txt) · [状态与摘要断言](candidate-3f9c974/junit/TEST-team.segroup11.review.state.ReviewStateTest.xml) | 主账号真私仓正文/仅评论变化需分别观察 |
| N2a · 状态与测试 | 首个可见绘制帧 ≤500 ms，慢成功只发一次；等待中滚动、编辑和切换 | 受控请求 11,498 ms，一次 POST。等待期间实际 EDT 可编辑 A/B，成功只更新 A。[慢回归](candidate-3f9c974/junit/TEST-team.segroup11.review.SlowWorkflowTest.xml) | 首帧由正常产品按钮 paintComponent 的单调时钟记录 |
| N2b · 状态与测试 | 同一操作的慢失败保留旧报告/新稿，允许显式重试 | 受控 503 耗时 10,029 ms，一次 POST；旧报告、新编辑和归属均经断言。[慢失败](candidate-3f9c974/junit/TEST-team.segroup11.review.SlowWorkflowTest.xml) | 失败、重试的可见首帧及连续截图按产品登记 |
| C1 · 窗口与 Tasks | 预览及 Tab 零 POST；点击只发该修订，后编辑保留 | 生产 Swing 键绑定覆盖光标、选区和连续采纳；发送锁定目标和修订，已加载评论数由唯一远端 ID 集合决定。[UI](candidate-3f9c974/junit/TEST-team.segroup11.review.SlowWorkflowTest.xml) · [发送状态](candidate-3f9c974/junit/TEST-team.segroup11.review.state.ReviewStateTest.xml) | 各产品实发对照下方独立回执 |
| C2 · 状态与测试 | 明确拒绝可重试；未知回执先 GET 原 Issue，用户决定 | 注入已收后超时、确定拒绝和重开，检查未自动补发、检查后由用户核对。[网关](candidate-3f9c974/junit/TEST-team.segroup11.review.github.GitHubGatewayTest.xml) · [状态](candidate-3f9c974/junit/TEST-team.segroup11.review.state.ReviewStateTest.xml) | 已产生的真实成功回执和受控丢回执是两组不同证据 |
| N3 · 四组 | 同一 ZIP 在四个精确 build 上通过二进制及五组操作 | Verifier 1.411 检查四款，每款得到 Compatible，5 处弃用、6 处实验 API，无报告的必需依赖缺失。[四款逐构建及依赖报告](candidate-3f9c974/verifier) · [IDE 内 SDK 探针](candidate-3f9c974/runtime) | 安装/私仓/慢分析/Tasks 与正常重启/真实评论按每款独立登记 |

## 失败与修复

0.2.2 能通过原生 Tasks 菜单返回已有 A/B，却在首次绑定无历史快照时把右窗清空。两个草稿仍在磁盘。0.2.3 使用已登记的目标区分这一次原生 clear 回调；其他任务或以后用户主动清除仍走常规处理。相同独立宿主对两版本执行真实 TaskManager 首次 A、首次 B、返回 A、返回 B。旧包前两次失败，新包四次目标、页签及草稿均正确。正常重启后过早查询 Git remote 的路径也改为监听 IDE 的仓库映射和 Git 配置事件。[旧包原始输出](candidate-267063f/task-activation/result.txt) · [新包原始输出](candidate-3f9c974/task-activation/result.txt) · [回顾行动 #23](https://github.com/SeGroup11/scrum-workspace/issues/23)

0.2.1 的旧包在真实 WebStorm 发评时丢了作者/日期，并显示旧评论条数。0.2.2 对实际字段和账号回执完成了实机复核。0.2.3 的网关回归继续使用同一条真实评论的 JSON 形态。[最初的远端回执](candidate-e66ed31/github/webstorm-receipt.json) · [修复后的 PyCharm 实发](candidate-267063f/github/pycharm-receipt.json)

## 同包的可见窗口操作


| 正常用户配置 | 从磁盘安装及私仓 | 10 秒受控慢成功（首帧 / 完成） | 原生 Tasks、重启与真实评论 |
| --- | --- | --- | --- |
| IntelliJ IDEA IU-262.10315.125 | 0.2.3 启动日志、安装 JAR 哈希和主账号私仓 A/B 均核实 | 9 / 10,683 ms；请求期间在 A 编辑，结果属于 B | #17 单发一次，编号 7127110320。详见[产品记录](candidate-3f9c974/gui/idea.md)和[独立回执](candidate-3f9c974/github/idea-receipt.json)。 |
| WebStorm WS-262.10315.144 | 同包安装和正常配置私仓通过，空模型配置下也可读原文 | 12 / 10,725 ms；请求期间在 A 编辑，B 报告引文正确 | 通过原生菜单 A→B→A 保留独立草稿。#17 单发一次，编号 7127188425。[产品记录](candidate-3f9c974/gui/webstorm.md) · [独立回执](candidate-3f9c974/github/webstorm-receipt.json) |
| Rider RD-262.9437.287 | 同包安装、组织授权账号及私仓 A/B 均核实 | 8 / 10,591 ms；等待期间切到 A 编辑 | 发现新的跨账号恢复缺陷后，本包未在 Rider 发评。其同包发送分项待测。[产品记录](candidate-3f9c974/gui/rider.md) |
| PyCharm PY-262.9437.214 | 同包安装并正常重开后自动恢复私仓、账号及 #17；打开 #16 读取讨论 | 6 / 10,485 ms；滚动、编辑 A/B、等待在 B，返回 A 可见原报告 | 新绑定的原生 #20 稳定后标题与详情一致。#16 单发一次，编号 7126914405。仅增评论报告仍当前，正文临改后标旧，还原后同一 UUID 恢复当前状态。[产品记录](candidate-3f9c974/gui/pycharm.md) · [远端回执](candidate-3f9c974/github/pycharm-receipt.json) · [真实正文对照 Check](https://github.com/SeGroup11/scrum-workspace/runs/138336756357) |

PyCharm 的本包慢失败和显式重试首帧分别为 5、71 ms，完成分别为 10,084、10,015 ms；各一次请求且保留新稿。[慢失败原始计时](candidate-3f9c974/gui/pycharm-live/production-timings.log)

在四个实际产品中，慢模型均为本机回环服务，私仓读取和评论回执来自 GitHub。四份 Verifier 的依赖与警告可以逐一复核。0.2.3 的 N3 尚未全部通过。各产品记录说明对应步骤的实测范围。[Gradle 原始汇总](candidate-3f9c974/verifier/gradle-verifyPlugin.txt) · [独立四款验证](https://github.com/SeGroup11/scrum-workspace/runs/138332736163)

## 双账号重新检测与丢回执故障注入

真实 PyCharm 生产窗口使用受控两账号与两仓库后，A 的详情及评论都晚于 B 返回，B 的正文、评论、草稿正确。PR 整页过滤、closed 筛选、精确编号均通过。在同一窗口从 A 手选 B 后点击“重新检测”，连接和列表属于 B，右侧却为空并提示选择旧 A；正常关闭重开可恢复 B 和新稿。该失败归入故事 #5，0.2.4 的修复由独立观察器按同一输入复测。[原始事件与源码](candidate-3f9c974/window-race) · [失败 Check](https://github.com/SeGroup11/scrum-workspace/runs/138343396521) · [修复 3cdfc4b](https://github.com/SeGroup11/scrum-workspace/commit/3cdfc4b5ccf22dd3792d6ade3c8952e9b32b0835)

丢回执专项直接加载冻结 ZIP 内的产品 JAR、真实 SDK 请求与响应解码器，并使用可读回的受控评论记录。403 拒绝未写入，人工重试才新增第一条；第二个场景先写入再抛出断线错误。重建项目状态后，双击与恢复均未产生额外 POST；对原 Issue 两次 GET 合并为两条唯一评论，确认编号 7101 后两份新稿保留，另一个 Issue 为零条。该专项不接触真实 GitHub。[观察器、结果与复跑入口](candidate-3f9c974/comment-recovery)
