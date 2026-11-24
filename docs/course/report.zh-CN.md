# GitHub Issue 质量评审插件

CS5351 Software Engineering · Group 11 · 项目报告

## 当前候选 0.2.2

安装包 `github-issue-review-0.2.2.zip` 为 **75,390,776 字节**，SHA-256 为 `b2eed1cd59d1f9f6f2231c55bb6c0d97ab82c5a6da27963c2a934053739bedf0`。本版修复了 0.2.1 在真实私仓中把作者与日期显示为未知、发布成功后评论计数仍显示 0 的问题。修复提交为 [267063f8d7fc2d321f03d386b2ce9c64553c916c](https://github.com/SeGroup11/scrum-workspace/commit/267063f8d7fc2d321f03d386b2ce9c64553c916c)。

干净构建耗时 7 分 52 秒，19 个 Gradle 任务实际执行，五个套件共 **23/23 项通过**，失败、错误和跳过均为零。SDK 回环成功与 HTTP 503 测试各发一次 POST，完成耗时为 11,476 ms 与 10,041 ms。[构建与原始 JUnit](../evidence/candidate-267063f/junit) · [GitHub 独立检查](https://github.com/SeGroup11/scrum-workspace/runs/138315005604)

同一 0.2.2 ZIP 在 IU-262.10315.125、WS-262.10315.144、RD-262.9437.287 和 PY-262.9437.214 上均获 Compatible。每款有 5 处弃用和 6 处实验性 API。隔离运行时的 IntelliJ IDEA 和 PyCharm 各以两种模式完成一次模型回环请求，WebStorm 与 Rider 的隔离配置在启动观察器前因缺许可证退出。[四款 Verifier 原始结果](../evidence/candidate-267063f/verifier/result.json) · [四款运行时边界](../evidence/candidate-267063f/runtime/result.json)

Gradle 原生 `verifyPlugin` 还对同一分发包执行两项任务，用时 3 分 54 秒，再次取得四款 Compatible。[原生任务输出](../evidence/candidate-267063f/verifier/gradle-verifyPlugin.txt)

0.2.2 使用模型协议相同的一对真实 Issue 原文再次调用回环服务。直接从上述 ZIP 加载插件和模型 SDK，A、B 各发一次 POST，两份报告都包含六项。A 的固定答复仍把已存在的两步复现判为缺失，B 的 CSV 目标和“导出要快”引文仍可逐字核对。修复模型之外的显示问题独立于这两份固定答案。[0.2.2 语义回放](../evidence/candidate-267063f/model/semantic-replay.json) · [调用计数](../evidence/candidate-267063f/model/loopback-calls.log)

在独立加载 0.2.2 候选的反序列化检查中，Issue 读到创建者 `segroup11-backlog[bot]` 及 2026-11-24 06:11:34 UTC，真实评论读到 `material4002` 及 08:08:49 UTC。实机复测前，专用 #16 为 0 条评论，#17 已有 0.2.1 的一条评论 7125780145。[字段复测输出](../evidence/candidate-267063f/github/isolated-loader-result.txt) · [0.2.2 实机前基线](../evidence/candidate-267063f/github/before.json)

0.2.2 的独立 PyCharm 运行时再次验证了应用级设置和项目恢复。两个项目通过原生注册表分别打开设置表单，读到同一模型与地址；PasswordSafe 只为保存过凭据的端点提供对应密钥。另两个独立进程正常关闭、重开同一项目，A 的空行、制表符、末字、尾空格及报告 ID 保留，B 保持主动清空。恢复旧 Tasks 定位后两项均未改变，测试端点第一次收到一个模型请求，重启时为零。本实验没有统计 GitHub GET/POST。[配置与凭据实证](../evidence/candidate-267063f/settings/README.md) · [工作区与 Tasks 实证](../evidence/candidate-267063f/state-restart/README.md)

在真实 PyCharm 窗口中，安装后主 JAR 与候选 ZIP 中对应 JAR 的 SHA-256 相等。主账号打开 #16 后，详情正确显示 Issue 作者和创建时间。Tab 采纳后，测试者把草稿改为“[CS5351 PY 0.2.2] 根据复现记录，导出的 CSV 只有表头。请补充输入行数、预期导出的行数，以及用于复现的版本。”点击发送后继续编辑。GitHub 于 08:33:05 UTC 在 #16 返回评论 **7126271956**，作者 **material4002**，全文逐字一致。IDE 显示 1 条评论和正确的评论作者、时间、ID，在途新写的本地草稿也保留。#17 原评论未增加。[安装 JAR 对照](../evidence/candidate-267063f/gui/pycharm-jars.json) · [发送后保留新编辑](../evidence/candidate-267063f/gui/pycharm-live/2026-11-24T08-33-18-487Z-published-and-pending-edit-retained.jpg) · [原始回执](../evidence/candidate-267063f/github/pycharm-receipt.json) · [真实评论](https://github.com/SeGroup11/scrum-workspace/issues/16#issuecomment-7126271956)

随后使用原生 Tasks 菜单实测时，首次激活没有历史上下文快照的任务后，右侧 Issue 面板变空。这个操作路径在之前的双进程旧快照测试中没有触发。0.2.2 的真实评论修复通过，首次任务激活仍需修复复测。[A 首次激活时的实机画面](../evidence/candidate-267063f/gui/pycharm-live/2026-11-24T08-33-47-832Z-first-activation-a.jpg) · [B 首次激活](../evidence/candidate-267063f/gui/pycharm-live/2026-11-24T08-34-32-963Z-first-activation-b.jpg)

被激活的 A、B 后来已各有上下文记录。用原生任务菜单再次切回 A、再回 B，两次均恢复了对应 Issue 与本地草稿。0.2.2 的本次正常窗口慢成功首帧为 **9 ms**，整个调用为 **10,622 ms**，模型服务收到一次请求。[A 的原生菜单恢复](../evidence/candidate-267063f/gui/pycharm-live/2026-11-24T08-35-16-475Z-task-menu-restored-a.jpg) · [B 的恢复](../evidence/candidate-267063f/gui/pycharm-live/2026-11-24T08-35-45-829Z-task-menu-restored-b.jpg) · [生产日志](../evidence/candidate-267063f/gui/pycharm-live/production-timings.log)

## 一条可以核实的评审意见

私仓 Issue #17 的标题是“导出搜索结果”，正文写着“希望将搜索结果导出为 CSV。导出要快。”导出对象与格式已经清楚，但“快”没有给出数据规模或允许耗时。设计中的追问是“请说明典型导出数据量，以及该规模下可以接受的完成时间。”数据量和时限由提交者决定。这个例子贯穿插件的模型协议、编辑流程和验收用例。[测试 Issue B](https://github.com/SeGroup11/scrum-workspace/issues/17) · [固定输入](../../src/test/resources/model/csv-input.json) · [固定报告](../../src/test/resources/model/csv-report.json)

项目的贡献是让一条机器生成的建议可供开发者逐层核实。插件在报告中保留模型实际看到的原句，在用户按 Tab 采纳后允许改写，点击发送时锁定目标 Issue 和最终文字。用户可以用 GitHub 返回的评论 ID、作者和正文检验发布结果。报告格式、语义质量、远端发布分别有独立的检验方式。

## 研究背景与产品决策

Bettenburg 等人调查了 Apache、Eclipse 和 Mozilla 的开发者及报告者，共获 466 份回复。开发者重视的复现步骤、堆栈和测试用例恰是报告者较难提供的信息。他们开发的 CUEZILLA 对缺陷报告质量给出估计，并建议补充材料。该研究支持本项目检查复现信息的动机，研究对象是缺陷报告。我们在功能需求中另外检查目标、可验证的验收标准与边界情况，分类效果仍需用本项目样本核验。[What Makes a Good Bug Report?（FSE 2008）](https://thomas-zimmermann.com/publications/files/bettenburg-fse-2008.pdf)

GitHub Issue Forms 可在提交时要求填写特定输入，并把表单内容转换为 Markdown。即使某个必填框已填写，“导出要快”仍有多种解释。我们的工具面向已提交的 Issue，让阅读代码的开发者在 IDE 中引用现有表述、追问缺失信息。此次原型没有做与表单、CUEZILLA 或人工评审之间的效率对照实验。[GitHub Issue Forms](https://docs.github.com/en/communities/using-templates-to-encourage-useful-issues-and-pull-requests/syntax-for-issue-forms)

## 系统设计与实现

| 用户动作 | 数据如何流动 | 约束与设计理由 |
| --- | --- | --- |
| 打开项目与 Issue | 从 Git 根与 fetch remote 找仓库，用所选 IDE GitHub 账号验证身份和权限。Issue 转成 Tasks API 的任务视图。 | 全仓库标识和账号区分不同身份下的同号 Issue。浏览列表过滤 PR，并把评论读取留给讨论页。 |
| 点击分析 | 复制已加载标题和原始正文，统一换行。后台构造系统模板与双字段 JSON 用户消息，通过 OpenAI Java SDK 请求一个候选。 | 不发送讨论、草稿或项目文件。应用级设置共享模型配置，PasswordSafe 按服务地址保存密钥。请求默认 60 秒且无 SDK 自动重试。 |
| 打开报告 | 先验证 JSON、必需键、枚举和四态字段组合，再逐字检查引文是否出现在指定标题或正文中。 | 错引文足以使整份报告失败。旧报告与未发草稿仍在。引文存在仅证明可定位，分类理由仍需要人工评价。 |
| 切任务或重启 | 任务快照仅存账号、仓库、Issue 号与页签。草稿、修订号、成功报告和待核对发送存于项目工作区。 | 旧任务快照不能覆盖新草稿。分析与评论返回均按发起键归档，保留等待期间的新编辑。 |
| 显式发送 | 保存发送文本与修订号，通过当时的 GitHub 会话向原 Issue 发布，按回执 ID 合并。 | 回执丢失时保留待核对记录。先查询原 Issue，再由用户确认已发或允许重发。仅凭相同正文无法区分两次合法发送。 |

分析结果分为 bug、feature、mixed、undetermined。模型必须交齐实际现象、复现步骤、预期结果、目标、验收标准与边界情况六项，每项取清楚、缺失、表述不清或不适用之一。模型不能改变行序或动作。对 #17，“目标”应引用 CSV 这句，“验收标准”应引用“导出要快”并追问数据量和耗时。[模型协议](../../src/main/kotlin/team/segroup11/review/model/ModelProtocol.kt) · [校验实现](../../src/main/kotlin/team/segroup11/review/model/ReportCodec.kt) · [思源报告协议](siyuan://blocks/20261123160324-e990gl3)

## 三项质量目标

| 目标 | 架构选择与取舍 | 判定方法 |
| --- | --- | --- |
| 上下文可靠 | 单一项目记录保存草稿与报告，按账号和完整 Issue 键隔离。拷贝原文的摘要只覆盖标题与正文，评论变化不使报告过期。代价是每个项目拥有独立的本地状态。 | 对比末字、空行、尾空格和主动清空。恢复旧任务、正常重启，检查模型和评论新增请求均为零。 |
| 分析期间可操作 | 项目拥有后台协程，窗口只订阅当前键。独立的请求 ID 去重，点击至状态首次绘制与模型耗时分开记录。 | 固定延迟 10 秒。首次绘制须 ≤500 毫秒，期间滚动、编辑与切换都可用。失败时原草稿与报告仍可读。 |
| 四产品兼容 | 复用 IntelliJ 平台、GitHub 与 Tasks，构建一个 ZIP，逐个实测相同 SHA-256。公共适配层缩小产品差异，具体 build 仍可能拒绝加载。 | 每款检查 Verifier 与五组实操。Verifier 成功不能证明 IDE 登录、焦点、重启或真实发布已成功。 |

## 三轮开发与证据

本仓库把项目拆为三个 Sprint。下表按实际工作切片组织。单次执行中累积的证据不代表三个自然周已经过去，也不能替代八位成员的真实认领、同伴评审、会议记录或用户反馈。

2026 年 11 月 24 日从组织看板读回 12 张故事卡，每个 Sprint 各 4 张。当次快照中 12 张均为 In Progress，尚缺实机、人工或跨组证据的故事继续保留该状态。[看板原始快照](../evidence/candidate-e66ed31/github/project.json)

| Sprint | 故事与设计 | 实现、评审与证据 |
| --- | --- | --- |
| 1 · 浏览私有 Issue | 1.1 至 1.4，覆盖账号、仓库、列表、分段详情及模型夹具。[#4](https://github.com/SeGroup11/scrum-workspace/issues/4) · [#5](https://github.com/SeGroup11/scrum-workspace/issues/5) · [#6](https://github.com/SeGroup11/scrum-workspace/issues/6) · [#7](https://github.com/SeGroup11/scrum-workspace/issues/7) | [PR #18](https://github.com/SeGroup11/scrum-workspace/pull/18) 记录首次评审。受控 GitHub 试验对应 [QA Check](https://github.com/SeGroup11/scrum-workspace/runs/138261486179)。首个 ZIP 在 [Verifier 检查](https://github.com/SeGroup11/scrum-workspace/runs/138262252679) 失败，随后修复了插件描述符名称。[修复提交](https://github.com/SeGroup11/scrum-workspace/commit/4bc2b851a6d5f9effea2835b34705c8f59d12d64) |
| 2 · 写出并发布回复 | 2.1 至 2.4，覆盖设置、协议、后台恢复与发送。[#8](https://github.com/SeGroup11/scrum-workspace/issues/8) · [#9](https://github.com/SeGroup11/scrum-workspace/issues/9) · [#10](https://github.com/SeGroup11/scrum-workspace/issues/10) · [#11](https://github.com/SeGroup11/scrum-workspace/issues/11) | [PR #19](https://github.com/SeGroup11/scrum-workspace/pull/19) 汇集模型、工作区存储和编辑器。受控服务实际接收 SDK 请求，用计数区分解析失败与隐藏重试。[模型测试](../../src/test/kotlin/team/segroup11/review/model/ModelGatewayTest.kt) |
| 3 · 回归与验证 | 3.1 至 3.4，覆盖干净检出、乱序与丢回执、同包兼容及交接。[#12](https://github.com/SeGroup11/scrum-workspace/issues/12) · [#13](https://github.com/SeGroup11/scrum-workspace/issues/13) · [#14](https://github.com/SeGroup11/scrum-workspace/issues/14) · [#15](https://github.com/SeGroup11/scrum-workspace/issues/15) | 按 SHA 绑定候选 ZIP 与四项 Verifier 输出。恢复回归对状态做 XML 往返，并覆盖未知版本和单条报告损坏。[状态回归](../../src/test/kotlin/team/segroup11/review/state/ReviewStateTest.kt) · [同包验证脚本](../../scripts/verify-candidate.ps1) |

一次具体的流程改进来自 Sprint 1。受控 GitHub 测试通过后，Verifier 对同一阶段的 ZIP 仍给出了描述符问题。后续验证同时记录源码提交与实际 ZIP 摘要，并保留失败检查以便复现。源码测试回答“逻辑能否工作”，产物检查回答“平台能否接受这个包”。[看板](https://github.com/orgs/SeGroup11/projects/1) · [失败 Check](https://github.com/SeGroup11/scrum-workspace/runs/138262252679)

## 实证结果与限制

固定协议样本中，四份预期有效的输入与报告配对全部被接受（4/4，100%），其中三份覆盖 bug、mixed、undetermined 和空正文。十九份预期无效的报告全部被拒绝（19/19，100%），包括重复字段、伪造引文、跨来源引文和缺建议。该比例的分母为人为构造的协议夹具；实际供应商报告的解析成功率尚待测量。真实 SDK 连接过本机受控服务，分别发送严格 Schema 和 JSON 对象两种请求；请求用户消息只有标题和正文。八种服务错误或包装异常各只产生一次请求，容量拦截产生零次请求。审核独立供应商语义质量还需要冻结真实模型版本、输入与原始人工标注。未收集人工标签，故无法计算人工分歧率。[协议测试](../../src/test/kotlin/team/segroup11/review/model/ReportCodecTest.kt) · [传输测试](../../src/test/kotlin/team/segroup11/review/model/ModelGatewayTest.kt)

### 两条实际专用 Issue 的语义复核

独立自动化复核使用专用 A/B 的实际原文，由 0.2.1 与 0.2.2 候选分别调用本机固定响应服务。每版的 A、B 各发一次 POST，各返回六项并通过协议校验。自动化评审随后逐项比对原文、模型判定和建议。A 被分为 bug，B 被分为 feature，两种分类均与这两个样例吻合。[夹具 A](https://github.com/SeGroup11/scrum-workspace/issues/16) · [夹具 B](https://github.com/SeGroup11/scrum-workspace/issues/17) · [0.2.1 原始报告](../evidence/candidate-e66ed31/model/semantic-replay.json) · [0.2.2 原始报告](../evidence/candidate-267063f/model/semantic-replay.json)

| 检查项 | A #16 导出文件为空 | B #17 导出搜索结果 |
| --- | --- | --- |
| 实际现象 | 判清楚，标题引文逐字吻合。正文把“空”具体化为“文件只含表头”，引用后者会更精确。 | 判不适用。原文提出新功能，没有报告故障。 |
| 复现步骤 | **发现偏差。** 报告判缺失，实际正文已给出两步，即打开有结果的搜索页面，再点击导出并打开生成的 CSV。重复索要最小步骤忽略了原有信息。 | 判不适用。此功能需求没有待复现的故障。 |
| 预期结果 | 判缺失。从三行搜索结果和只有表头的样例能推知期望有数据行，但没有明确说明应导出哪些行与列。追问预期输出有依据，同时索要复现步骤则重复。 | 判不适用。功能需求的预期已由目标和验收标准两项评价。 |
| 功能目标 | 判不适用。用户报告已有导出功能发生故障。 | 判清楚。引文“希望将搜索结果导出为 CSV。”直接给出对象和格式。 |
| 验收标准 | 判不适用。本协议将该缺陷的预期放在缺陷组三项。 | 判表述不清。“导出要快”可逐字定位，追问数据量和耗时切中歧义。 |
| 边界情况 | 判不适用。本文没有提出新功能，缺陷组检查仍适用。 | 判缺失。原文未说明空结果如何反馈，建议请提交者决定该场景的行为。 |

在这两个固定样例中，格式有效的受控答复仍出现了一处明确的语义误判。这个结果揭示固定模型夹具的局限，不能估计实际供应商的误判率。独立真人标注份数为零，人工分歧率无可用分母。

状态单元测试按 XML 字节写出再读回，检查了草稿尾空格、最后一次编辑、清空、跨账号与跨库隔离。测试还模拟了成功回执在编辑后到达、发送被拒绝、响应丢失后重启，以及损坏报告保留可读草稿。[恢复测试](../../src/test/kotlin/team/segroup11/review/state/ReviewStateTest.kt) · [质量目标与完整矩阵](siyuan://blocks/20261123160601-o204afz)

进一步的恢复验证使用了两个独立的 PyCharm 进程和同一个无界面测试项目。第一进程保存旧 Tasks 定位快照、显式分析一次、再改写 A 并主动清空 B，通过真实平台 API 正常关闭项目并退出。第二进程重开后，A 的末字、空行、尾空格、页签与报告 ID 均保持，B 仍是清空后的修订。调用已注册 Tasks provider 加载旧快照后，A、B 最新记录均未被覆盖。模型端点在两次进程中分别收到 1、0 次请求。项目没有 GitHub 账号或 remote，未测 GitHub GET/POST。带真实私仓的 Tasks 菜单操作仍需桌面验收。PyCharm 的首帧计时见下文。[两次进程和项目 XML](../evidence/candidate-e66ed31/state-restart/README.md)

同一候选的版本、ZIP 摘要、逐项结果及四款实机情况见下节。由于尚无可审计的人类用户反馈，当前无法量化插件是否减少了澄清往返次数。后续可请不同成员独立标注一组真实缺陷与功能需求，先保留分歧，再讨论“足以验证”的尺度，并抽查建议是否引入原文未承诺的数值。

## 0.2.1 历史候选与缺陷复现

| 字段 | 候选实测 |
| --- | --- |
| 版本与制品 | `github-issue-review-0.2.1.zip`，75,389,798 字节 |
| 实现提交 | [e66ed311cf986d876814f7737f897b526322236a](https://github.com/SeGroup11/scrum-workspace/commit/e66ed311cf986d876814f7737f897b526322236a) |
| ZIP SHA-256 | `71c549a8203edb80060d37411ff344a32b10cec534982121163fe920c52b4cd4` |
| 干净检出 | 构建用时 8 分钟，19 个 Gradle 任务实际执行。5 套件共 21 项测试全部通过，无跳过。[QA Check](https://github.com/SeGroup11/scrum-workspace/runs/138292777291) |
| 慢模型 | 成功与 HTTP 503 各一次 POST，从分析开始到请求完成分别为 10,811 ms 与 10,029 ms。真实 PyCharm 的首帧计时见本节后文。[原始计时](../evidence/candidate-e66ed31/junit/TEST-team.segroup11.review.SlowWorkflowTest.xml) |
| 候选视频 | 133 秒中文字幕。前 64 秒用候选 SDK 与 Swing 测试编辑框演示逐字引文、Tab、改写和伪引文失败。后 69 秒使用 WebStorm 与 PyCharm 实机截帧，含真实评论回执、旧版显示缺陷及按原采样间隔回放的十秒重试。[演示说明](demo-and-debug.zh-CN.md) |

[Sprint 3 PR #21](https://github.com/SeGroup11/scrum-workspace/pull/21) · [正式候选的逐项证据](../evidence/index.md) · [四款 IDE 验收故事](https://github.com/SeGroup11/scrum-workspace/issues/14)

Plugin Verifier 1.411 对同一 SHA-256 的四次判定均为 Compatible，每个产品报告 5 处弃用 API、6 处实验性 API。此次二进制检查覆盖 IU-262.10315.125、WS-262.10315.144、RD-262.9437.287 和 PY-262.9437.214。版本升级时需重新核对这些调用。N3 同时要求磁盘安装、私仓、慢分析、任务与重启、真实评论五组观察。[四款逐项检查](../evidence/candidate-e66ed31/verifier/result.json) · [Verifier Check](https://github.com/SeGroup11/scrum-workspace/runs/138294759169)

随后以这份锁定的 ZIP 执行 Gradle 原生 `verifyPlugin`。两项任务实际运行，四个目标均再次判定 Compatible，总耗时 12 分 57 秒，插件与依赖下载量为 0 B。[原生任务输出](../evidence/candidate-e66ed31/verifier/gradle-verifyPlugin.txt)

在各产品的隔离配置中另运行了无界面模型探针。IntelliJ IDEA 和 PyCharm 各通过候选包内的 SDK 向回环服务发出两次请求，JSON 对象与严格 Schema 模式均解析出 feature。WebStorm 和 Rider 的隔离配置在执行探针前因缺少有效许可证而退出。[四款运行时结果](../evidence/candidate-e66ed31/runtime/result.json) · [运行时 Check](https://github.com/SeGroup11/scrum-workspace/runs/138294767613)

正常配置的 IntelliJ IDEA 从磁盘安装了同一 ZIP，动态加载日志列出 0.2.1，工具窗口成功打开。账号列表为空时，窗口提示前往 IDE GitHub 设置。通过 GitHub 官方登录后，重新检测识别了用户主账号和项目远端；身份已核实，读取组织私仓仍被拒绝。[IDEA 日志与实机截图](../evidence/candidate-e66ed31/gui/idea.md)

在正常配置的 WebStorm 中，从磁盘选取 0.2.1 ZIP 并正常重启，启动日志读回了版本，Issue Review 工具窗口可见。所选 IDE 主账号身份核验成功。读取组织私仓时窗口明确提示“账号身份已核实，GitHub 未允许访问此仓库”。GitHub 官方应用授权页面当时将 SeGroup11 列为 Restricted，并提供 Grant。该次观测验证了安装和分阶段提示，私仓正文、任务与重启恢复、首帧和远端评论回执仍待实测。[WebStorm 观察](../evidence/candidate-e66ed31/gui/webstorm.md) · [实机截图](../evidence/candidate-e66ed31/gui/webstorm-repository-denied.png)

在 PyCharm 中，从磁盘安装时 IDE 先提示依赖未知，随后明确确认插件已安装并要求重启。正常重启后，日志列出 0.2.1。打开 Issue Review、选取项目仓库与账号后，插件同样核实了账号身份，读取私仓时遭到拒绝。这次观察验证了该产品的安装与权限提示。[依赖提示](../evidence/candidate-e66ed31/gui/pycharm-disk-unknown-dependencies.jpg) · [安装确认](../evidence/candidate-e66ed31/gui/pycharm-disk-installed-restart.jpg) · [权限反馈](../evidence/candidate-e66ed31/gui/pycharm-repository-denied.jpg)

在同一 PyCharm 的模型设置页填写回环地址、模型 ID 和临时密钥，点击 Apply 后，地址与模型 ID 保留，密码框清空。另输入一次尚未提交的密钥，再修改地址，密码框随即清空。读回应用配置 XML 时，仅看到回环地址和模型 ID。此轮没有从 GUI 的私仓 Issue 触发模型请求。[PyCharm 设置过程和原始截图](../evidence/candidate-e66ed31/gui/pycharm.md)

同一 ZIP 另在 PyCharm 的无界面运行时验证了凭据的实际读取。地址 A 保存的临时密钥可取回；改为地址 B 且留空后，候选提示该服务缺少密钥；切回 A 时再次取回原值。两个不同项目分别打开已注册的设置表单，都读到相同的应用级地址与模型。这次观察器没有发送模型请求。[实际验证输出](../evidence/candidate-e66ed31/settings/result.txt) · [复跑方法](../evidence/candidate-e66ed31/settings/README.md)

QA 独立记录了专用 Issue 的基线。测试开始前 #16、#17 的评论数均为 0，WebStorm 与 PyCharm 首轮仓库访问被拒后仍为 0。这两次读回为后续显式发送的增量核验提供了对照。[测试前](../evidence/candidate-e66ed31/github/before.json) · [拒绝后的读回](../evidence/candidate-e66ed31/github/after-repository-denial.json) · [实机 Check](https://github.com/SeGroup11/scrum-workspace/runs/138297958418)

用户授予 SeGroup11 的组织权限后，WebStorm 0.2.1 读取了私仓列表、#16 长正文与 #17 的 CSV 需求。在 B 的草稿中输入“[CS5351 WS 0.2.1] 请确认典型导出数据量，以及该规模下可接受的完成时间。”并显式发送一次。GitHub 原始 API 在 #17 读回评论 **7125780145**，作者 **material4002**，全文逐字一致。时间为 2026-11-24 08:08:49 UTC。#16 仍为 0 条，#17 从 0 增至 1 条。[真实 B 正文](../evidence/candidate-e66ed31/gui/webstorm-real-b.png) · [发送前](../evidence/candidate-e66ed31/gui/webstorm-before-send.png) · [远端评论](https://github.com/SeGroup11/scrum-workspace/issues/17#issuecomment-7125780145) · [独立读回](../evidence/candidate-e66ed31/github/webstorm-receipt.json)

这次成功发评揭示了 0.2.1 的两处显示错误。Issue 与评论作者、日期显示未知；回执和评论 ID 已进入讨论窗口，本地计数仍显示 0。该版本通过了这次远端发评子项，本地元数据与计数仍需修复复测。[发布后实机截图](../evidence/candidate-e66ed31/gui/webstorm-after-send.png)

获授权的 PyCharm 随后从私仓读到 A/B，对 #16 在真实工具窗口完成三轮慢分析。成功、503 失败、显式重试的首次状态绘制分别为 **6、5、19 ms**，插件记录的完成耗时为 **10,910、10,041、10,014 ms**。服务每轮各收到一次 POST。等待期间可滚动 A、编辑或切到 B。503 后 A 的旧报告可读，Tab 将预览建议插入已有草稿。重试返回时仍停留在 B，切回 A 可见成功报告。该产品在此轮未发送评论。[按时间排序的 GUI 截图与实测说明](../evidence/candidate-e66ed31/gui/pycharm-live/README.md)

## 两次实测推动的修复

答辩走查发现 `f8e1c4a` 只显示分类结果，分类理由与非适用组的折叠缺失。`2974097` 补上两项，并新增默认列表、展开顺序、mixed 与 undetermined 展示的回归。[报告展示修复](https://github.com/SeGroup11/scrum-workspace/commit/29740976e138e3e3566c1d222e5963f4c4bd5c54)

0.2.0 的普通 WebStorm 和隔离 PyCharm 均能选中主账号，连接私仓时却只显示笼统的认证失败。0.2.1 提供原账号重新认证入口，区分核验身份、检查仓库和读取 Issue 的错误，并显示项目根短名与 remote 别名。WebStorm 的复测已把阻点定位到组织授权；上表与本节实机结论绑定到各自的候选。[0.2.0 原始实机记录](../evidence/acceptance-0.2.0.md) · [认证修复](https://github.com/SeGroup11/scrum-workspace/commit/380be1b) · [当前逐项矩阵](../evidence/acceptance.md) · [演示步骤与跨组复现](demo-and-debug.zh-CN.md)
