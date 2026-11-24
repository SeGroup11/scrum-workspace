# 0.2.4 候选验收矩阵

2026-11-24 在 Windows NT 10.0.26300.0 上，对源码 [3cdfc4b5ccf22dd3792d6ade3c8952e9b32b0835](https://github.com/SeGroup11/scrum-workspace/commit/3cdfc4b5ccf22dd3792d6ade3c8952e9b32b0835) 的干净 Git 归档运行 Gradle 9.8.1、JBR 25、`test buildPlugin --no-build-cache`。全部 19 项任务执行，7 分 37 秒。6 个套件 26 项通过，失败、错误、跳过均为零。唯一 ZIP 为 `github-issue-review-0.2.4.zip`，75,393,535 字节，SHA-256 `a98dee22e68630481c65107efb13a2a4604da26cd6140f9bb5abd3db3b823709`。包内主 JAR 的 SHA-256 为 `ca19ab74f491a75237672cb0467b68450870925ee26a4808ec0770adefe76c36`。插件 ID 为 `team.segroup11.issue-review`，声明的四项必需依赖与各产品实际解析记录见附件。[独立干净构建 Check](https://github.com/SeGroup11/scrum-workspace/runs/138349043838) · [四款兼容性 Check](https://github.com/SeGroup11/scrum-workspace/runs/138352449193) · [包描述符](candidate-3cdfc4b/junit/distribution.json) · [JUnit XML](candidate-3cdfc4b/junit) · [逐构建依赖清单](candidate-3cdfc4b/verifier)

测试 A 是私仓 [#16 导出文件为空](https://github.com/SeGroup11/scrum-workspace/issues/16)，B 是 [#17 导出搜索结果](https://github.com/SeGroup11/scrum-workspace/issues/17)。0.2.4 开始时 A 有 2 条既有评论，B 有 3 条。正常 IDE 使用获授权的 material4002。SDK 模型测试使用本机回环服务，正式外部供应商仍待具备服务配置后单独验收。[本轮独立基线](candidate-3cdfc4b/github/before.json) · [0.2.3](acceptance-0.2.3.md) · [0.2.2](acceptance-0.2.2.md) · [0.2.1](acceptance-0.2.1.md) · [0.2.0](acceptance-0.2.0.md)

## 逐项输入、通过条件和证据

每行均使用上述提交与 ZIP，通过条件来自[思源 05 验收设计](siyuan://blocks/20261123160601-o204afz)。表中的“受控”指测试可重复注入指定请求、顺序或错误。各正常产品的前台实测另列。

| 用例和主责 | 输入、操作与预期 | 本轮实测及范围 | 判定 |
| --- | --- | --- | --- |
| G1 平台与 GitHub | 两个账号和 remote、旧任务与新选 Issue、账号失效及 401/403/404/429；恢复选择和地址、隔离请求 | 生产网关的两组数字身份及错误回归通过。生产窗口的双账号和两仓使用受控传输，正常私仓使用单一获授权账号。[网关回归](candidate-3cdfc4b/junit/TEST-team.segroup11.review.github.GitHubGatewayTest.xml) · [双仓窗口](candidate-3cdfc4b/window-race) | 通过，双账号/双仓使用受控连接 |
| I1 平台与 GitHub | 第一页全是 PR，切筛选、按完整编号与标题搜索；A 详情晚于 B；列表阶段评论 GET=0 | 真 SDK 回放对齐 GitHub 编号、开闭、更新时间，无早读评论。生产窗口由观察器安排 A 的详情、评论晚到，检查 B 的正文、草稿和目标归属。[分页与搜索](candidate-3cdfc4b/junit/TEST-team.segroup11.review.github.GitHubGatewayTest.xml) · [真实窗口与受控连接](candidate-3cdfc4b/window-race) | 通过 |
| I2 平台与窗口 | 区分无正文与尚未加载；评论次页断开后单独重试；按时间和 ID 排序去重；查看表格、图与代码 | 回放验证空正文、缺字段、第二页失败、保留已有正文以及合并两个 ID。0.2.4 正常 PyCharm 直接核对了原文表格、图片替代文字和长 fenced CSV 的降级显示。[本包网关](candidate-3cdfc4b/junit/TEST-team.segroup11.review.github.GitHubGatewayTest.xml) · [0.2.4 的真实 Markdown 显示](candidate-3cdfc4b/gui/pycharm.md) | 通过，Markdown 采用可读降级显示 |
| 设置专项 报告与模型 | A/B 两地址、两个真实项目；Apply 生效、密钥按地址隔离、空设置仍可浏览编辑 | 应用级设置与 PasswordSafe 单独复测，正常产品的私仓交互另见下表。空设置时可编辑私仓草稿的原始实测在 0.2.2 IDEA，0.2.3 WebStorm 也用空配置读取了原文。[本包设置](candidate-3cdfc4b/settings) · [缺配置实机](candidate-267063f/gui/idea.md) | 通过，缺配置实机保留历史版本证据 |
| R1 报告与模型 | 六项、四态、顺序、分类及逐字引文；B 应询问规模与耗时；对照人工标签 | 协议有效与无效输入逐份断言。供课件演示的固定模型会把 A 写明的两步误判为缺失，B 的含糊词应给出原句与追问。独立真人标注与外部供应商的语义准确率尚无可计分分母。[协议](candidate-3cdfc4b/junit/TEST-team.segroup11.review.model.ReportCodecTest.xml) · [A/B 回放](candidate-3cdfc4b/model) | 协议通过，固定输出有一处误判，外部供应商和独立真人标注待测 |
| R2 报告与模型 | 注入损坏、重复或额外键、伪引文、拒答、截断、超时；不得覆盖旧报告 | 真实 OpenAI Java 4.112.0 客户端向受控服务请求，每种故障只发一次；不合规输出拒绝并保留可用状态。[客户端](candidate-3cdfc4b/junit/TEST-team.segroup11.review.model.ModelGatewayTest.xml) · [状态](candidate-3cdfc4b/junit/TEST-team.segroup11.review.state.ReviewStateTest.xml) | 通过，受控故障注入 |
| N1a 状态与测试 | 独立进程关闭与重开，保留最后字符、空行/尾空格、主动清空、页签和报告；不得隐式重发或分析 | 项目存储及原生 Tasks 另跑在真实宿主；重开时模型计数由回环服务记录，GitHub 流量只按已插桩的场景报告。[状态](candidate-3cdfc4b/junit/TEST-team.segroup11.review.state.ReviewStateTest.xml) · [双进程原始输出](candidate-3cdfc4b/state-restart) | 通过，四个正常进程重开及独立双进程观察 |
| N1b 状态与测试 | 在旧任务快照存在时编辑新稿；比较仅加评论和改正文；仅正文变化可标旧 | 冷任务首次 A/B 及已有任务恢复分别核对；0.2.4 正常 PyCharm 在同一报告 UUID 上核实仅评论变化仍当前、改正文标旧、还原后当前，最新草稿保持。[本包原生 Tasks](candidate-3cdfc4b/task-activation) · [真实正文前后](candidate-3cdfc4b/gui/pycharm.md) | 通过，当前包真私仓原文往返 |
| N2a 状态与测试 | A 的 10 秒模型请求，重复点击，滚动、编辑与切 A/B；首帧 ≤500 ms，一次请求只更新 A | 本包受控成功 12,915 ms，只发 1 次。等待中 A/B 可独立编辑，完成时不跳页。正常产品的首个实际绘制帧和完整耗时另列。[慢请求回归](candidate-3cdfc4b/junit/TEST-team.segroup11.review.SlowWorkflowTest.xml) | 通过，四款正常窗口 |
| N2b 状态与测试 | 已有旧报告，10 秒后 HTTP 503；保留旧报告与新稿，用户再点才重试 | 受控失败 10,057 ms，只发 1 次。成功和失败的请求归属、编辑与旧报告分别断言；正常产品的失败和重试独立计时。[慢失败回归](candidate-3cdfc4b/junit/TEST-team.segroup11.review.SlowWorkflowTest.xml) | 通过，四款正常窗口 |
| C1 窗口与 Tasks | 预览与 Tab 不发布；手工改写后一次显式发送；核对作者、编号、逐字正文、在途新稿 | 真实 Swing Tab 处理与锁定发送目标/修订有自动覆盖。各款正常 IDE 发评后由独立账号只读 GitHub API 核对两张专用 Issue 与逐字正文。[编辑器与确认](candidate-3cdfc4b/junit/TEST-team.segroup11.review.SlowWorkflowTest.xml) · [本轮真实回执](candidate-3cdfc4b/github) | 通过，四款真实账号单发 |
| C2 状态与测试 | 确定拒绝后手动重试；另注入服务端已收、响应丢失；重开后先 GET 原 Issue，无自动重发 | 独立仓库记账器直接加载同包 JAR，分别统计 POST 尝试、实际入账、重复读取及另一 Issue；原始输出附在专项目录。[丢回执观察器](candidate-3cdfc4b/comment-recovery) · [状态机](candidate-3cdfc4b/junit/TEST-team.segroup11.review.state.ReviewStateTest.xml) | 通过，受控已写入后断线及恢复 |
| N3 四组 | 四指定构建，同一 SHA，兼容性、从磁盘安装、正常私仓、慢响应、Tasks 与正常重启、显式发评 | 官方 Verifier、隔离运行时与正常用户配置分别见各自记录。正常用户配置按下表逐产品核验。[Verifier](candidate-3cdfc4b/verifier) · [隔离运行时](candidate-3cdfc4b/runtime) · [正常产品](candidate-3cdfc4b/gui) | 通过，四款同包五组操作 |

## 同包实机结果

| 产品与实际 build | A 慢成功 首帧/完成 | A 慢 503 首帧/完成 | 显式重试 首帧/完成 | 主账号真实评论 |
| --- | ---: | ---: | ---: | --- |
| IntelliJ IDEA IU-262.10315.125 | 25 / 10792 ms | 63 / 10042 ms | 7 / 10014 ms | [#17 · 7127681925](https://github.com/SeGroup11/scrum-workspace/issues/17#issuecomment-7127681925) |
| WebStorm WS-262.10315.144 | 5 / 10010 ms | 10 / 10042 ms | 6 / 10012 ms | [#17 · 7127850398](https://github.com/SeGroup11/scrum-workspace/issues/17#issuecomment-7127850398) |
| Rider RD-262.9437.287 | 8 / 10011 ms | 5 / 10074 ms | 15 / 10012 ms | [#17 · 7127880975](https://github.com/SeGroup11/scrum-workspace/issues/17#issuecomment-7127880975) |
| PyCharm PY-262.9437.214 | 8 / 10513 ms | 9 / 10045 ms | 16 / 10014 ms | [#16 · 7127797042](https://github.com/SeGroup11/scrum-workspace/issues/16#issuecomment-7127797042) |

以上时间以产品按钮第一次实际绘制与点击间隔、模型在 IDE 中的完整耗时分别计。A/B 均来自真实私仓，模型服务在本机固定延迟。独立 QA 对两张 Issue 搜索每份最终正文，四款恰好各有一条且属于目标。真实最终条数 A=3、B=6；基线分别为 2、3。[四份独立回执 Check](https://github.com/SeGroup11/scrum-workspace/runs/138359601812) · [逐请求回执](candidate-3cdfc4b/github)

本版 PyCharm 的 A 报告 UUID `c0868c1b-4a2a-4d9d-9017-859bd5811266` 在仅新增评论后仍为当前报告。在专用 #16 临时修改真实正文时，它显示“基于较早版本”；按原始字节还原后，旧标消失，报告及发送期间写下的新草稿仍在。更改、还原和独立只读查询都保留了当时的三条评论。[正常窗口与截图](candidate-3cdfc4b/gui/pycharm.md) · [原文哈希复核](candidate-3cdfc4b/github/body-qa.json) · [独立 Check](https://github.com/SeGroup11/scrum-workspace/runs/138359625231)


## 四产品的执行方式与判定

四个正常用户配置都从磁盘安装同一 ZIP，启动日志列出 0.2.4，已安装主 JAR 与 ZIP 条目 SHA-256 相同。四份官方判定均为 Compatible，API 不兼容与必需依赖缺失均为 0；每个 build 另列 5 处弃用和 6 处实验 API。独立 CLI 4 分 56 秒，Gradle verifyPlugin 5 分 38 秒，两次使用同一安装包。[依赖与逐款原始判定](candidate-3cdfc4b/verifier) · [Gradle 记录](candidate-3cdfc4b/verifier/gradle-verifyPlugin.txt)

正常 WebStorm 和 PyCharm 用标题栏的原生 Tasks 切换；当前安装的 IDEA、Rider 含平台 Tasks API，使用插件按钮调用相同的原生 TaskManager 完成激活。四款均使用正常项目和 GitHub 主账号。另开的空白配置中，IDEA、PyCharm 各完成两种模型模式；WebStorm、Rider 的独立无界面配置因许可限制未执行该探针。正常窗口中的私仓、模型和评论按本页各产品记录判定。[IDEA](candidate-3cdfc4b/gui/idea.md) · [WebStorm](candidate-3cdfc4b/gui/webstorm.md) · [Rider](candidate-3cdfc4b/gui/rider.md) · [PyCharm](candidate-3cdfc4b/gui/pycharm.md) · [隔离许可与模型结果](candidate-3cdfc4b/runtime/result.json)

在 A/B 操作后把 B 主动清空，保留 A 的空行、末字及尾空格，在正常退出后重开同一工程。JBR 从各个新进程启动时仅跟踪插件的连接、详情、模型与评论四个入口。读回已清空的 B，再切回 A 核对其完整草稿，随后停止计数。实际四个新进程为 IDEA 16024、WebStorm 28680、Rider 12244、PyCharm 4556。每个均为 1 次连接、2 次详情读取、0 次模型入口、0 次评论入口。12 个真实窗口的首帧介于 5–63 ms。项目状态与远端评论另有独立核对。[四产品五组操作 QA](https://github.com/SeGroup11/scrum-workspace/runs/138367248948)[计数方法、正对照与各进程原始数据](candidate-3cdfc4b/restart-calls)


## 缺陷前后对照

0.2.2 首次绑定原生任务时详情被清掉。0.2.3 的四步冷任务复测通过，正常冷启动也可恢复私仓，但其生产窗口在双仓手选 B 后重新检测时右侧为空。迟到的 A 详情/评论没有覆盖 B，也没有跨账号发评；正常关闭重开可恢复。0.2.4 在明确点选新行时取消之前的待恢复定位并显示当前连接。相同输入继续用原生窗口观察器复核。[0.2.2 冷任务实测](candidate-267063f/task-activation/result.txt) · [0.2.3 重检失败](candidate-3f9c974/window-race/result.txt) · [0.2.4 源码修复](https://github.com/SeGroup11/scrum-workspace/commit/3cdfc4b5ccf22dd3792d6ade3c8952e9b32b0835)
