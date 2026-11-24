# GitHub Issue 质量评审插件

CS5351 Software Engineering · Group 11 · 项目报告

## 0.2.4 候选交付

本版修复了手选另一仓库和 Issue 后，重新检测使右侧详情消失的问题。干净检出构建了唯一安装包 `github-issue-review-0.2.4.zip`，75,393,535 字节，SHA-256 为 `a98dee22e68630481c65107efb13a2a4604da26cd6140f9bb5abd3db3b823709`。19 项 Gradle 任务全部实际执行，耗时 7 分 37 秒。六个测试套件共 26 项均通过。约 10 秒的受控成功、503 回合各触发一次请求，完成时间分别为 12,915 ms、10,057 ms。[源码提交](https://github.com/SeGroup11/scrum-workspace/commit/3cdfc4b5ccf22dd3792d6ade3c8952e9b32b0835) · [安装包摘要](../evidence/candidate-3cdfc4b/junit/distribution.json) · [慢响应原始结果](../evidence/candidate-3cdfc4b/junit/TEST-team.segroup11.review.SlowWorkflowTest.xml)

### 正常桌面与真实 GitHub 回执

每款产品均检查安装候选 ZIP、重启后的实际 JAR 和正常授权账号 `material4002`。独立 QA 在本轮开始时读得 #16 两条、#17 三条；下列计数仅对应各次观察时刻。[发评前快照](../evidence/candidate-3cdfc4b/github/before.json)

| 产品与目标 | 模型首帧 / 总时间 | GitHub 回执与数量 | 桌面观察 |
| --- | --- | --- | --- |
| IntelliJ IDEA → #17 | 11 ms / 10,843 ms | 09:38:09 UTC，评论 [7127681925](https://github.com/SeGroup11/scrum-workspace/issues/17#issuecomment-7127681925)。当时 #16 为 2 条，#17 为 4 条。 | 在 B 发起分析，去 A 编辑。回 B 核对“导出要快”、预览、Tab 采纳与改写。正式重启后恢复 B 的讨论和指定末字。[IDEA 桌面记录](../evidence/candidate-3cdfc4b/gui/idea.md) · [远端回执](../evidence/candidate-3cdfc4b/github/idea-receipt.json) |
| PyCharm → #16 | 8 ms / 10,513 ms | 09:43:17 UTC，评论 [7127797042](https://github.com/SeGroup11/scrum-workspace/issues/16#issuecomment-7127797042)。当时 #16 为 3 条，#17 为 4 条。 | 慢分析期间滚动、修改 A、切 B 编辑。改写 A 的建议并显式发送后立即继续写下一条，回执到达时新稿仍在。独立读回的正文指出“有三行搜索结果，导出的 CSV 却仅有表头”。[PyCharm 桌面记录](../evidence/candidate-3cdfc4b/gui/pycharm.md) · [远端回执](../evidence/candidate-3cdfc4b/github/pycharm-receipt.json) |
| WebStorm → #17 | 34 ms / 11,182 ms | 09:45:38 UTC，评论 [7127850398](https://github.com/SeGroup11/scrum-workspace/issues/17#issuecomment-7127850398)。当时 #16 为 3 条，#17 为 5 条。 | 点击时的产品标记与追问正文同 GitHub 记录一致。[WebStorm 桌面](../evidence/candidate-3cdfc4b/gui/webstorm.md) · [生产计时](../evidence/candidate-3cdfc4b/gui/webstorm-model.log) · [独立回执](../evidence/candidate-3cdfc4b/github/webstorm-receipt.json) |
| Rider → #17 | 7 ms / 10,482 ms | 09:46:58 UTC，评论 [7127880975](https://github.com/SeGroup11/scrum-workspace/issues/17#issuecomment-7127880975)。当时 #16 为 3 条，#17 为 6 条。 | QA 在原 Issue 上核对了作者、UTC 时间、产品标记与全文。[Rider 桌面](../evidence/candidate-3cdfc4b/gui/rider.md) · [生产计时](../evidence/candidate-3cdfc4b/gui/rider-model.log) · [独立回执](../evidence/candidate-3cdfc4b/github/rider-receipt.json) |

PyCharm 在本轮还比较了同一份报告 `c0868c1b-4a2a-4d9d-9017-859bd5811266` 的三种状态。只在 #16 新增评论后刷新，报告仍为当前；临时修改真实正文后，界面出现“基于较早版本”；逐字还原原文后，同一报告又成为当前。两次远端回读互为逆操作，09:49:41 UTC 的还原结果与修改前正文 SHA-256 均为 `a7e17d6b70bf3e4b3664e937639f92c9aebe660468cf8449d926cd70f7ca180a`，当时仍是三条评论。[仅新增评论](../evidence/candidate-3cdfc4b/gui/pycharm-live/state-same-report-after-single-comment.json) · [改动后提示](../evidence/candidate-3cdfc4b/gui/pycharm-live/2026-11-24T09-48-52-840Z-temporary-body-old-report-and-new-draft.jpg) · [远端变更](../evidence/candidate-3cdfc4b/github/body-apply.json) · [原文还原](../evidence/candidate-3cdfc4b/github/body-restore.json) · [还原后画面](../evidence/candidate-3cdfc4b/gui/pycharm-live/2026-11-24T09-50-04-175Z-restored-original-body-current-report-and-draft.jpg)

在真实 A #16 的慢响应专项中，每款均直接测量“从点击到状态第一次绘制”与“整个请求完成”两段时间，单位为毫秒。四款的成功、503、显式重试共十二次首帧均在 500 ms 内；项目的 10 秒慢模型解释了完成耗时。等待中滚动、修改 A 以及转到 B 编辑的操作，分别保留在各产品的窗口记录中。

| 产品 | 成功，首帧 / 完成 | 503，首帧 / 完成 | 手动重试，首帧 / 完成 |
| --- | --- | --- | --- |
| IntelliJ IDEA | 25 / 10,792 | 63 / 10,042 | 7 / 10,014。[生产日志](../evidence/candidate-3cdfc4b/gui/idea-model.log) |
| WebStorm | 5 / 10,010 | 10 / 10,042 | 6 / 10,012。[生产日志](../evidence/candidate-3cdfc4b/gui/webstorm-model.log) |
| Rider | 8 / 10,011 | 5 / 10,074 | 15 / 10,012。[生产日志](../evidence/candidate-3cdfc4b/gui/rider-model.log) |
| PyCharm | 8 / 10,513 | 9 / 10,045 | 16 / 10,014。[生产日志](../evidence/candidate-3cdfc4b/gui/pycharm-live/production-timings.log) |

### 正常重启是否暗中分析或发评

JBR Flight Recorder（JFR）从新 IDE 进程启动开始，记录插件的 `connect`、`detail`、`postComment` 与 `evaluate` 四个方法入口，直到 A、B 恢复情况可核查。连接与详情的非零记录验证被观察的实际进程确实加载了插件。另有受控发布和正常 IDEA 分析作为阳性对照。方法进入次数由 JFR 取得，GitHub 远端新增评论数由独立 API 读回，模型网络请求数由本机 HTTP 服务日志取得。协程重新进入会产生额外方法事件，它们不能直接折算成 HTTP 次数。[JFR 过滤与复跑](../evidence/candidate-3cdfc4b/restart-calls/README.md) · [模型阳性对照](../evidence/candidate-3cdfc4b/restart-calls/idea-analysis-positive-control.json) · [评论阳性对照](../evidence/candidate-3cdfc4b/restart-calls/ledger-positive-control.json)

PyCharm 正常退出前，A 草稿修订 23，含空行以及末字“丙”后的两个空格；B 的修订 10 是主动清空的空串。重开后活动 B 仍为空，经原生菜单切到 A 后报告和新稿均可见。两个项目状态快照可逐字符比较。该次记录为 connect 1、detail 2、postComment 0、evaluate 0。[退出前](../evidence/candidate-3cdfc4b/gui/pycharm-live/state-normal-close-before-relaunch.json) · [重开后](../evidence/candidate-3cdfc4b/gui/pycharm-live/state-after-normal-relaunch-native-a.json) · [JFR 原始计数](../evidence/candidate-3cdfc4b/restart-calls/pycharm-restart.json)

四个普通产品的最终进程分别完成正常保存、退出和重新打开。每款都核对了 B 主动清空以及 A 的草稿。对同一组方法的结果如下。

| 重启产品 | 进程 PID | connect | detail | postComment | evaluate |
| --- | ---: | ---: | ---: | ---: | ---: |
| [IntelliJ IDEA](../evidence/candidate-3cdfc4b/restart-calls/idea-restart.json) | 16024 | 1 | 2 | 0 | 0 |
| [WebStorm](../evidence/candidate-3cdfc4b/restart-calls/webstorm-restart.json) | 28680 | 1 | 2 | 0 | 0 |
| [Rider](../evidence/candidate-3cdfc4b/restart-calls/rider-restart.json) | 12244 | 1 | 2 | 0 | 0 |
| [PyCharm](../evidence/candidate-3cdfc4b/restart-calls/pycharm-restart.json) | 4556 | 1 | 2 | 0 | 0 |

### 同包验证

| 检查 | 0.2.4 实测 |
| --- | --- |
| 四款兼容性 | IntelliJ IDEA IU-262.10315.125、WebStorm WS-262.10315.144、Rider RD-262.9437.287、PyCharm PY-262.9437.214 均为 Compatible。每款报告均列出 5 处弃用 API、6 处实验性 API；原生 Gradle 验证耗时 5 分 38 秒，下载 0 B。[四份裁决](../evidence/candidate-3cdfc4b/verifier/result.json) · [Gradle 原始输出](../evidence/candidate-3cdfc4b/verifier/gradle-verifyPlugin.txt) |
| 项目恢复与 Tasks | 同一项目在两个 PyCharm 进程中正常关闭、重开，A 的空行、末字、尾空格、页签和报告 ID 保持，B 保持主动清空的修订。重启后模型请求为零，旧 Tasks 定位快照恢复 A 后两条最新草稿均保留。原生 Task 首次激活及已有任务互切另有事件次序记录。[项目复测](../evidence/candidate-3cdfc4b/state-restart/read.txt) · [原生 Tasks 复测](../evidence/candidate-3cdfc4b/task-activation/result.txt) |
| 模型凭据与设置 | 两个项目均从已注册设置面板读到相同地址和模型。受控凭据在端点 A 成功读回；切至 B 时缺少相应凭据，切回 A 后可再次读回。[设置专项](../evidence/candidate-3cdfc4b/settings/result.txt) |
| 发布结果不确定 | 直接加载候选 JAR 后，将测试发布限定在受控 GitHub 响应。确定 403 产生零条远端记录，显式重试落下一条；另一条先写入后断开。两次查询原 Issue #42 按 ID 合并为两条，另一 Issue #43 为零。三个 POST 都来自显式操作，用户等待期间的新稿保留。[逐请求账本](../evidence/candidate-3cdfc4b/comment-recovery/result.txt) · [丢回执 Check](https://github.com/SeGroup11/scrum-workspace/runs/138350066142) |
| 两账号及两仓迟到响应 | 先挂起 Alpha #16 的详情与评论，改选 Beta #17 并编辑，再释放旧响应。标题、正文、回复目标与草稿均属于 Beta。同窗重新检测和正常关闭后重开也保留 Beta。两个合成账号经 SDK 解码，不计为两位真实用户。[0.2.3 失败](../evidence/candidate-3f9c974/window-race/result.txt) · [0.2.4 通过](../evidence/candidate-3cdfc4b/window-race/result.txt) |

干净构建和代码评审都绑定到提交 `3cdfc4b`。实际 SDK 请求、私仓桌面操作与固定模型语义复核各有独立证据，自动化执行没有产生两位组员的人工标注。[干净构建 Check](https://github.com/SeGroup11/scrum-workspace/runs/138349043838) · [PR #21 源码评审](https://github.com/SeGroup11/scrum-workspace/pull/21#pullrequestreview-6036812874) [当前逐用例验收矩阵](../evidence/acceptance.md) · [各产品 SDK 回环](../evidence/candidate-3cdfc4b/runtime/result.json) [四款官方兼容 Check](https://github.com/SeGroup11/scrum-workspace/runs/138352449193) · [四张真实评论回执 Check](https://github.com/SeGroup11/scrum-workspace/runs/138359601812) · [实际正文往返 Check](https://github.com/SeGroup11/scrum-workspace/runs/138359625231)

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

组织看板中的原定故事为 #4 至 #15，三个 Sprint 各 4 张。0.2.1 时 12 张均在进行中。2026 年 11 月 24 日 10:08 UTC 的最终快照显示原定故事 10/12 Done。各 Sprint 分别为 4/4、3/4、3/4。#9 仍等待独立真人标注及线上模型语义核验，#15 留待跨组人员实际复现与反馈。回顾事项 #23 已完成。下表保留每个故事的设计和实现入口。[初期快照](../evidence/candidate-e66ed31/github/project.json) · [最终看板回读](../evidence/candidate-3cdfc4b/github/project.json) · [四产品五组实操 QA](https://github.com/SeGroup11/scrum-workspace/runs/138367248948)

| Sprint | 故事与设计 | 实现、评审与证据 |
| --- | --- | --- |
| 1 · 浏览私有 Issue | 1.1 至 1.4，覆盖账号、仓库、列表、分段详情及模型夹具。[#4](https://github.com/SeGroup11/scrum-workspace/issues/4) · [#5](https://github.com/SeGroup11/scrum-workspace/issues/5) · [#6](https://github.com/SeGroup11/scrum-workspace/issues/6) · [#7](https://github.com/SeGroup11/scrum-workspace/issues/7) | [PR #18](https://github.com/SeGroup11/scrum-workspace/pull/18) 记录首次评审。受控 GitHub 试验对应 [QA Check](https://github.com/SeGroup11/scrum-workspace/runs/138261486179)。首个 ZIP 在 [Verifier 检查](https://github.com/SeGroup11/scrum-workspace/runs/138262252679) 失败，随后修复了插件描述符名称。[修复提交](https://github.com/SeGroup11/scrum-workspace/commit/4bc2b851a6d5f9effea2835b34705c8f59d12d64) |
| 2 · 写出并发布回复 | 2.1 至 2.4，覆盖设置、协议、后台恢复与发送。[#8](https://github.com/SeGroup11/scrum-workspace/issues/8) · [#9](https://github.com/SeGroup11/scrum-workspace/issues/9) · [#10](https://github.com/SeGroup11/scrum-workspace/issues/10) · [#11](https://github.com/SeGroup11/scrum-workspace/issues/11) | [PR #19](https://github.com/SeGroup11/scrum-workspace/pull/19) 汇集模型、工作区存储和编辑器。受控服务实际接收 SDK 请求，用计数区分解析失败与隐藏重试。[模型测试](../../src/test/kotlin/team/segroup11/review/model/ModelGatewayTest.kt) |
| 3 · 回归与验证 | 3.1 至 3.4，覆盖干净检出、乱序与丢回执、同包兼容及交接。[#12](https://github.com/SeGroup11/scrum-workspace/issues/12) · [#13](https://github.com/SeGroup11/scrum-workspace/issues/13) · [#14](https://github.com/SeGroup11/scrum-workspace/issues/14) · [#15](https://github.com/SeGroup11/scrum-workspace/issues/15) | 按 SHA 绑定候选 ZIP 与四项 Verifier 输出。恢复回归对状态做 XML 往返，并覆盖未知版本和单条报告损坏。[状态回归](../../src/test/kotlin/team/segroup11/review/state/ReviewStateTest.kt) · [同包验证脚本](../../scripts/verify-candidate.ps1) |

一次具体的流程改进来自 Sprint 1。受控 GitHub 测试通过后，Verifier 对同一阶段的 ZIP 仍给出了描述符问题。后续验证同时记录源码提交与实际 ZIP 摘要，并保留失败检查以便复现。源码测试回答“逻辑能否工作”，产物检查回答“平台能否接受这个包”。[看板](https://github.com/orgs/SeGroup11/projects/1) · [失败 Check](https://github.com/SeGroup11/scrum-workspace/runs/138262252679)

## 实证结果与限制

固定协议样本中，四份预期有效的输入与报告配对全部被接受（4/4，100%），其中三份覆盖 bug、mixed、undetermined 和空正文。十九份预期无效的报告全部被拒绝（19/19，100%），包括重复字段、伪造引文、跨来源引文和缺建议。该比例的分母为人为构造的协议夹具；实际供应商报告的解析成功率尚待测量。真实 SDK 连接过本机受控服务，分别发送严格 Schema 和 JSON 对象两种请求；请求用户消息只有标题和正文。八种服务错误或包装异常各只产生一次请求，容量拦截产生零次请求。审核独立供应商语义质量还需要冻结真实模型版本、输入与原始人工标注。未收集人工标签，故无法计算人工分歧率。[协议测试](../../src/test/kotlin/team/segroup11/review/model/ReportCodecTest.kt) · [传输测试](../../src/test/kotlin/team/segroup11/review/model/ModelGatewayTest.kt)

### 两条实际专用 Issue 的语义复核

0.2.4 直接从上述 ZIP 加载插件与 SDK，把专用 A、B 的真实标题和正文发给本机固定响应服务。A、B 各一次 POST，各返回六项并通过协议校验。独立复跑脚本从 ZIP 重新解压、调用服务，逐份核对报告内容与输入摘要，两者均相同。自动化复核再逐项对照原文与报告。A 分类为 bug，B 分类为 feature，均符合各自样例。[A 原文](https://github.com/SeGroup11/scrum-workspace/issues/16) · [B 原文](https://github.com/SeGroup11/scrum-workspace/issues/17) · [本版完整报告](../evidence/candidate-3cdfc4b/model/semantic-replay.json) · [请求计数](../evidence/candidate-3cdfc4b/model/loopback-calls.log) · [复跑脚本](../evidence/candidate-3cdfc4b/model/run.ps1)

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

0.2.1 的平台恢复专项使用了两个独立的 PyCharm 进程和同一个无界面测试项目。第一进程保存旧 Tasks 定位快照、显式分析一次、再改写 A 并主动清空 B，通过真实平台 API 正常关闭项目并退出。第二进程重开后，A 的末字、空行、尾空格、页签与报告 ID 均保持，B 仍是清空后的修订。调用已注册 Tasks provider 加载旧快照后，A、B 最新记录均未被覆盖。模型端点在两次进程中分别收到 1、0 次请求。项目没有 GitHub 账号或 remote，未测 GitHub GET/POST。后续版本的真实私仓桌面操作单独列入候选结果。[两次进程和项目 XML](../evidence/candidate-e66ed31/state-restart/README.md)

各候选使用独立的提交、ZIP 摘要、验收矩阵与实机回执。由于尚无可审计的人类用户反馈，当前无法量化插件是否减少了澄清往返次数。后续可请不同成员独立标注一组真实缺陷与功能需求，先保留分歧，再讨论“足以验证”的尺度，并抽查建议是否引入原文未承诺的数值。

## 缺陷如何从实机操作进入下一版

| 可复现的发现 | 实现与复测 |
| --- | --- |
| Sprint 1 的受控 GitHub 测试通过后，Verifier 仍拒绝了同阶段的 ZIP，原因是插件显示名称。 | `4bc2b85` 修改描述符，四款兼容复测。后续 Check 同时固定提交与 ZIP 摘要。[失败记录](https://github.com/SeGroup11/scrum-workspace/runs/138262252679) · [修复复测](https://github.com/SeGroup11/scrum-workspace/runs/138265147006) |
| `f8e1c4a` 缺少分类理由与非适用组折叠。随后 0.2.0 的实机连接又将身份核验与读取仓库失败混作同一提示。 | `2974097` 补足报告视图。0.2.1 加入原账号重新认证及分阶段提示。WebStorm 实机指出账号身份已核实，但 JetBrains OAuth 应用在组织中受限。用户完成 Grant 后，能实际读取 #16、#17。[视图修复](https://github.com/SeGroup11/scrum-workspace/commit/29740976e138e3e3566c1d222e5963f4c4bd5c54) · [0.2.0 实机原始记录](../evidence/acceptance-0.2.0.md) · [0.2.1 权限证据](../evidence/candidate-e66ed31/gui/webstorm.md) |
| 0.2.1 的 WebStorm 在 #17 显式发出评论 7125780145。服务器作者、正文正确；本地仍显示未知作者、日期和 0 条评论。 | 0.2.2 使用真实 GitHub 字段并刷新本地计数。PyCharm 在 #16 发出 7126271956，作者 material4002，计数从 0 增至 1，在途新稿保留；#17 当时仍为 1。前后均有独立 API 回读。[旧版评论](https://github.com/SeGroup11/scrum-workspace/issues/17#issuecomment-7125780145) · [修复提交](https://github.com/SeGroup11/scrum-workspace/commit/267063f8d7fc2d321f03d386b2ce9c64553c916c) · [0.2.2 评论](https://github.com/SeGroup11/scrum-workspace/issues/16#issuecomment-7126271956) |
| 0.2.2 首次激活无历史快照的原生任务后，右侧面板变空；之后由原生菜单切回有快照的 A、B 均正确。 | 0.2.3 改为保留显式选中的 Issue。真实 PyCharm 首次激活先前没有快照的 #20，待原生异步标题稳定，顶栏、选中行及右侧正文都指向 #20。较早的过渡帧里顶栏仍为 #17。[0.2.2 原生菜单证据](../evidence/candidate-267063f/gui/pycharm.md) · [0.2.3 稳定帧](../evidence/candidate-3f9c974/gui/pycharm-live/2026-11-24T09-02-38-990Z-fresh-20-stable-title-and-detail.jpg) |
| 0.2.3 在有两个账号、两个受控仓库的生产工具窗口中，手选 B 后再重新检测，账号与仓库仍正确，但遗留 A 定位让右侧变空。正常关闭再打开项目则可恢复 B。 | 0.2.4 已按同一顺序复测，使用同一生产窗口观察器与相同模拟身份和仓库。显式选择 B 后，待恢复的 A 指针立即清除。A 的详情和评论延迟返回、同窗重新检测、正常关闭再开，三次观察均保持 B 的详情、回复目标和最新草稿。[0.2.3 原失败序列](../evidence/candidate-3f9c974/window-race/result.txt) · [0.2.4 原始通过序列](../evidence/candidate-3cdfc4b/window-race/result.txt) · [0.2.4 修复提交](https://github.com/SeGroup11/scrum-workspace/commit/3cdfc4b5ccf22dd3792d6ade3c8952e9b32b0835) |

0.2.1 的真实 PyCharm 在慢成功、503 和重试三次操作中，首帧为 6、5、19 ms，完成为 10,910、10,041、10,014 ms，各产生一次模型 POST。0.2.2 在真实 PyCharm 的成功回合测得 9 ms 首帧和 10,622 ms 完成。各版都单独保留了原始证据和课程文件。[0.2.1 历史草稿](https://github.com/SeGroup11/scrum-workspace/releases/tag/untagged-c616ca1c718dbf3bdfbf) · [0.2.2 历史草稿](https://github.com/SeGroup11/scrum-workspace/releases/tag/untagged-9ac4cd6d61db5fa5e910) · [0.2.3 历史草稿](https://github.com/SeGroup11/scrum-workspace/releases/tag/untagged-07d4af22255ffb0c13ac)

0.2.3 PyCharm 的报告 `ce24ab60-0102-44e1-b833-ab252a3d02dd` 在真实 #16 经历了三种状态。仅新增评论后刷新，仍显示当前报告。临时修改正文再刷新，界面提示“基于较早版本”。09:09:57 UTC 从 GitHub 读回原正文时，摘要与改动前逐字相符；再刷新后，同一报告恢复为当前。两份工作区记录的报告 ID、输入摘要、草稿修订 15 和末尾两个空格全部相同。[改动前的报告](../evidence/candidate-3f9c974/gui/pycharm-live/state-after-comment-before-body.json) · [过期提示](../evidence/candidate-3f9c974/gui/pycharm-live/2026-11-24T09-09-18-254Z-temporary-body-stale-report-with-unchanged-new-draft.jpg) · [GitHub 还原回读](../evidence/candidate-3f9c974/github/body-restore.json) · [最终报告与草稿](../evidence/candidate-3f9c974/gui/pycharm-live/state-restored-original-body.json)

在 0.2.3 的丢回执专项中，直接载入候选 JAR 接受受控 GitHub 响应。已确定的 403 没有新增远端记录，显式重试落下一条；另一次模拟先写入再断开。两次查询原 Issue #42 后按评论 ID 合并为两条，另一 Issue #43 始终为零。三个 POST 均来自显式动作，重复点击和重启没有增加 POST。这项使用受控服务器，和下面三款主账号发评的 GitHub 远端回执分别核验。[丢回执原始记录](../evidence/candidate-3f9c974/comment-recovery/result.txt) · [丢回执 Check](https://github.com/SeGroup11/scrum-workspace/runs/138344717870) · [双仓重检失败 Check](https://github.com/SeGroup11/scrum-workspace/runs/138343396521)

### 从服务器独立回读的 0.2.3 评论

发评前两条专用 Issue 各有一条历史评论。下表的数量取自每次发布后的 GitHub API，只计到当次观察时刻。各次回执作者均为 `material4002`，不同产品在正文中使用不同标记。[发评前基线](../evidence/candidate-3f9c974/github/before.json)

| IDE 与目标 | 远端时间（UTC） | 评论 ID | 当时 #16、#17 数量 |
| --- | --- | --- | --- |
| PyCharm → #16 | 09:04:03 | [7126914405](https://github.com/SeGroup11/scrum-workspace/issues/16#issuecomment-7126914405) | 2、1。[独立回执](../evidence/candidate-3f9c974/github/pycharm-receipt.json) |
| IntelliJ IDEA → #17 | 09:12:58 | [7127110320](https://github.com/SeGroup11/scrum-workspace/issues/17#issuecomment-7127110320) | 2、2。[独立回执](../evidence/candidate-3f9c974/github/idea-receipt.json) |
| WebStorm → #17 | 09:16:29 | [7127188425](https://github.com/SeGroup11/scrum-workspace/issues/17#issuecomment-7127188425) | 2、3。[独立回执](../evidence/candidate-3f9c974/github/webstorm-receipt.json) |
