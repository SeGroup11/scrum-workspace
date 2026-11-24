# GitHub Issue 质量评审插件

CS5351 Software Engineering · Group 11 · 项目报告

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

| Sprint | 故事与设计 | 实现、评审与证据 |
| --- | --- | --- |
| 1 · 浏览私有 Issue | 1.1 至 1.4，覆盖账号、仓库、列表、分段详情及模型夹具。[#4](https://github.com/SeGroup11/scrum-workspace/issues/4) · [#5](https://github.com/SeGroup11/scrum-workspace/issues/5) · [#6](https://github.com/SeGroup11/scrum-workspace/issues/6) · [#7](https://github.com/SeGroup11/scrum-workspace/issues/7) | [PR #18](https://github.com/SeGroup11/scrum-workspace/pull/18) 记录首次评审。受控 GitHub 试验对应 [QA Check](https://github.com/SeGroup11/scrum-workspace/runs/138261486179)。首个 ZIP 在 [Verifier 检查](https://github.com/SeGroup11/scrum-workspace/runs/138262252679) 失败，随后修复了插件描述符名称。[修复提交](https://github.com/SeGroup11/scrum-workspace/commit/4bc2b851a6d5f9effea2835b34705c8f59d12d64) |
| 2 · 写出并发布回复 | 2.1 至 2.4，覆盖设置、协议、后台恢复与发送。[#8](https://github.com/SeGroup11/scrum-workspace/issues/8) · [#9](https://github.com/SeGroup11/scrum-workspace/issues/9) · [#10](https://github.com/SeGroup11/scrum-workspace/issues/10) · [#11](https://github.com/SeGroup11/scrum-workspace/issues/11) | [PR #19](https://github.com/SeGroup11/scrum-workspace/pull/19) 汇集模型、工作区存储和编辑器。受控服务实际接收 SDK 请求，用计数区分解析失败与隐藏重试。[模型测试](../../src/test/kotlin/team/segroup11/review/model/ModelGatewayTest.kt) |
| 3 · 回归与验证 | 3.1 至 3.4，覆盖干净检出、乱序与丢回执、同包兼容及交接。[#12](https://github.com/SeGroup11/scrum-workspace/issues/12) · [#13](https://github.com/SeGroup11/scrum-workspace/issues/13) · [#14](https://github.com/SeGroup11/scrum-workspace/issues/14) · [#15](https://github.com/SeGroup11/scrum-workspace/issues/15) | 按 SHA 绑定候选 ZIP 与四项 Verifier 输出。恢复回归对状态做 XML 往返，并覆盖未知版本和单条报告损坏。[状态回归](../../src/test/kotlin/team/segroup11/review/state/ReviewStateTest.kt) · [同包验证脚本](../../scripts/verify-candidate.ps1) |

一次具体的流程改进来自 Sprint 1。受控 GitHub 测试通过后，Verifier 对同一阶段的 ZIP 仍给出了描述符问题。后续验证同时记录源码提交与实际 ZIP 摘要，并保留失败检查以便复现。源码测试回答“逻辑能否工作”，产物检查回答“平台能否接受这个包”。[看板](https://github.com/orgs/SeGroup11/projects/1) · [失败 Check](https://github.com/SeGroup11/scrum-workspace/runs/138262252679)

## 实证结果与限制

固定协议样本中，四份预期有效的输入与报告配对全部被接受（4/4，100%），其中三份覆盖 bug、mixed、undetermined 和空正文。十九份预期无效的报告全部被拒绝（19/19，100%），包括重复字段、伪造引文、跨来源引文和缺建议。该比例的分母为人为构造的协议夹具；实际供应商报告的解析成功率尚待测量。真实 SDK 连接过本机受控服务，分别发送严格 Schema 和 JSON 对象两种请求；请求用户消息只有标题和正文。八种服务错误或包装异常各只产生一次请求，容量拦截产生零次请求。审核独立供应商语义质量还需要冻结真实模型版本、输入与原始人工标注。未收集人工标签，故无法计算人工分歧率。[协议测试](../../src/test/kotlin/team/segroup11/review/model/ReportCodecTest.kt) · [传输测试](../../src/test/kotlin/team/segroup11/review/model/ModelGatewayTest.kt)

状态测试按 XML 字节写出再读回，检查了草稿尾空格、最后一次编辑、清空、跨账号与跨库隔离；也模拟了成功回执在编辑后到达、发送被拒绝、响应丢失后重启，以及损坏报告保留可读草稿。正常关闭真实 IDE 时最后一笔输入是否进入磁盘、提示首次画出用了多少时间，都须单独观察。[恢复测试](../../src/test/kotlin/team/segroup11/review/state/ReviewStateTest.kt) · [质量目标与完整矩阵](siyuan://blocks/20261123160601-o204afz)

同一候选的版本、ZIP 摘要、逐项结果及四款实机情况见下节。由于尚无可审计的人类用户反馈，当前无法量化插件是否减少了澄清往返次数。后续可请不同成员独立标注一组真实缺陷与功能需求，先保留分歧，再讨论“足以验证”的尺度，并抽查建议是否引入原文未承诺的数值。

## 候选与验收记录

| 字段 | 候选实测 |
| --- | --- |
| 版本与制品 | `github-issue-review-0.2.0.zip`，75,386,326 字节 |
| 实现提交 | [29740976e138e3e3566c1d222e5963f4c4bd5c54](https://github.com/SeGroup11/scrum-workspace/commit/29740976e138e3e3566c1d222e5963f4c4bd5c54) |
| ZIP SHA-256 | `e0b10cede8dc310990fc1dad747cf46a9c6319a4cb1c89db9e40156cc394a0d3` |
| 干净检出 | 构建用时 2 分 8 秒，20 项自动测试全部通过，无跳过 |
| 慢模型 | 成功与 HTTP 503 各一次 POST，从分析开始到请求完成分别为 10,782 ms 与 10,023 ms。首帧、滚动和焦点另行取证 |
| 视频片段 | 63 秒中文字幕受控回放。候选编译的模型网关实际请求本机服务，生产 Tab 动作在 Swing 编辑框中插入建议，随后注入伪引文并比对原报告和改写后的草稿 |

[Sprint 3 PR #21](https://github.com/SeGroup11/scrum-workspace/pull/21) · [正式候选的逐项证据](../evidence/index.md) · [四款 IDE 验收故事](https://github.com/SeGroup11/scrum-workspace/issues/14)

Plugin Verifier 1.411 对同一 SHA-256 的四次判定均为 Compatible，每个产品报告 5 处弃用 API、6 处实验性 API。此次二进制检查覆盖 IU-262.10315.125、WS-262.10315.144、RD-262.9437.287 和 PY-262.9437.214。版本升级时需重新核对这些调用。N3 同时要求磁盘安装、私仓、慢分析、任务与重启、真实评论五组观察，缺少任一实机证据时保留待验。

在实际产品的隔离配置中另运行了无界面模型探针。IntelliJ IDEA 和 PyCharm 各通过候选包内的 SDK 向回环服务发出两次请求，JSON 对象与严格 Schema 模式均解析出 feature。WebStorm 和 Rider 在执行探针前退出，隔离配置报告没有有效许可证。该实验只覆盖运行时装载及模型链路；可见工具窗口、IDE 测试账号和远端真实评论仍需单独核对。[四款运行时结果](../evidence/candidate-2974097/runtime/result.json)

答辩走查发现 `f8e1c4a` 只显示分类结果，分类理由与非适用组的折叠缺失。实现补上这两项，并新增默认列表、展开顺序、mixed 与 undetermined 展示的回归。上表记录修复后的候选。较早的 Sprint 1 还曾发现描述符问题，可查到[失败检查](https://github.com/SeGroup11/scrum-workspace/runs/138262252679)和[修复复测](https://github.com/SeGroup11/scrum-workspace/runs/138265147006)。[本轮修复](https://github.com/SeGroup11/scrum-workspace/commit/29740976e138e3e3566c1d222e5963f4c4bd5c54) · [演示步骤与跨组复现](demo-and-debug.zh-CN.md)
