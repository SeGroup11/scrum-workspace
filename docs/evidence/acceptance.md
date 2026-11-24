# 0.2.2 候选验收矩阵

候选 [267063f8d7fc2d321f03d386b2ce9c64553c916c](https://github.com/SeGroup11/scrum-workspace/commit/267063f8d7fc2d321f03d386b2ce9c64553c916c)，插件版本 0.2.2，安装包 github-issue-review-0.2.2.zip 共 75,390,776 字节，SHA-256 为 b2eed1cd59d1f9f6f2231c55bb6c0d97ab82c5a6da27963c2a934053739bedf0。2026-11-24 在 Windows NT 10.0.26300.0、JBR 25、Gradle 9.8.1、OpenAI Java 4.112.0 上从干净源码归档执行 test buildPlugin，19 个任务实际执行。五个套件共 23 项通过，错误、失败和跳过均为 0。[构建与 JUnit XML](candidate-267063f/junit) · [QA Check](https://github.com/SeGroup11/scrum-workspace/runs/138315005604)

固定私仓为 SeGroup11/scrum-workspace，A 是 [#16 导出文件为空](https://github.com/SeGroup11/scrum-workspace/issues/16)，B 是 [#17 导出搜索结果](https://github.com/SeGroup11/scrum-workspace/issues/17)。本轮开始时 A 有 0 条评论，B 有 1 条来自 0.2.1 的真实测试评论。下表均以本次源码、ZIP 及同名证据目录为准；跨版本的比较单独标出。[本轮基线](candidate-267063f/github/before.json) · [0.2.1 原始矩阵](acceptance-0.2.1.md) · [0.2.0 原始矩阵](acceptance-0.2.0.md)

| 用例与主责 | 输入与预期 | 0.2.2 可复查的实测分项 | 尚待单独核验 |
| --- | --- | --- | --- |
| G1 · 平台与 GitHub | 选择并恢复仓库与 IDE 身份；处理权限、限流及账号变化 | 回放验证双身份、remote、服务器数字 ID、身份与仓库拒绝的分段提示，以及确认或取消重认证。[网关测试](candidate-267063f/junit/TEST-team.segroup11.review.github.GitHubGatewayTest.xml) | 真双账号、多 remote 及撤销权限后的恢复 |
| I1 · 平台与 GitHub | 过滤整页 PR 后仍能查 Issue，列表评论 GET=0，迟到响应不影响 B | 使用 SDK 解码两页与 Link，区分评论 GET。真实 #17 的用户、创建及更新时间有断言。[网关测试](candidate-267063f/junit/TEST-team.segroup11.review.github.GitHubGatewayTest.xml) | 真私仓列表字段、快速切换与刷新所选行 |
| I2 · 窗口与 Tasks | 空正文与未加载可辨，评论翻页失败保留正文，按 ID 去重 | 受控缺作者、空正文、评论读取失败与重试有断言。实际发布过的评论 GET 和 POST 均保留作者和日期，原包类加载器复现与新包对照见下。[双类加载器复测](candidate-267063f/github/isolated-loader-result.txt) | PyCharm 实机已核对新作者/日期和计数 0→1；真实分页讨论仍另验 |
| 设置专项 · 报告与模型 | 两项目共享，Apply 生效，按服务地址隔离密钥，缺配置可浏览和编辑 | 两个真实 PyCharm 项目读取同一表单设置。PasswordSafe 核对地址 A 能取回原值，地址 B 无密钥，切回 A 恢复原值。[真实设置](candidate-267063f/settings/README.md)。测试两种响应模式、错误输入和无配置调用。[模型网关](candidate-267063f/junit/TEST-team.segroup11.review.model.ModelGatewayTest.xml) | 私仓窗口缺配置时仍可浏览和编辑、修改后下一次分析即时生效 |
| R1 · 报告与模型 | 六项四态，逐字引文，“导出要快”追问规模与耗时 | 同一包在真实 A/B 原文上复跑固定模型，两份均解析成功，每份六项。B 正确引用原话并追问；A 已有复现步骤却被判缺失。[实际结果](candidate-267063f/model/semantic-replay.json) · [解析测试](candidate-267063f/junit/TEST-team.segroup11.review.model.ReportCodecTest.xml) | 真实供应商能力、真人标注与语义分歧 |
| R2 · 报告与模型 | 畸形、伪引文、拒答、截断、超时不覆盖旧报告或草稿 | 结构负例、单次 HTTP 请求、失败保留旧报告已通过。[SDK](candidate-267063f/junit/TEST-team.segroup11.review.model.ModelGatewayTest.xml) · [状态](candidate-267063f/junit/TEST-team.segroup11.review.state.ReviewStateTest.xml) | 正常窗口失败、显式重试与真实供应商差异 |
| N1a · 状态与测试 | 末字、空白、主动清空、报告与页签在重启时保持；恢复不发送 | 两个独立 PyCharm PID 正常退出、重开，A 末字/空白、页签和报告 ID 一致，B 的主动清空保留。模型恢复请求 0。[重启证据](candidate-267063f/state-restart/README.md)。XML/JDOM 往返和跨账号同号隔离通过。[状态测试](candidate-267063f/junit/TEST-team.segroup11.review.state.ReviewStateTest.xml) | 真实窗口正常重启、焦点、恢复期 GitHub GET/POST |
| N1b · 状态与测试 | 旧 Tasks 快照只恢复指针；仅正文变化使旧报告过时 | 同包在已注册的 Tasks provider 加载旧定位时 A/B 记录未被覆盖。[真实 Tasks](candidate-267063f/state-restart/README.md)。不同修订、正文 hash 及评论独立的状态断言通过。[状态测试](candidate-267063f/junit/TEST-team.segroup11.review.state.ReviewStateTest.xml) | 正常 Tasks 菜单往返能恢复，首次激活却清空右窗；改正文、仅加评论分别回查 |
| N2a · 状态与测试 | 延迟成功时首个绘制帧 ≤500 ms，每次分析仅一请求，等待期可操作 | 受控模型成功耗时 11,476 ms，调用一次。EDT 等待时编辑、切换，结果归 A。[慢响应回归](candidate-267063f/junit/TEST-team.segroup11.review.SlowWorkflowTest.xml) | 本次 PyCharm 真窗口首帧 9 ms、完成 10,622 ms；更多产品的完整交互另验 |
| N2b · 状态与测试 | 延迟失败保留旧报告和新稿，可由用户重试 | 受控 HTTP 503 耗时 10,041 ms，一次调用；旧报告和新编辑保留。[慢响应回归](candidate-267063f/junit/TEST-team.segroup11.review.SlowWorkflowTest.xml) | 真窗口失败首帧、交互与显式重试 |
| C1 · 窗口与 Tasks | 预览和 Tab 不发送；显式发布锁定目标和点击时文字 | 真实 Swing 编辑器键绑定、预览采纳、选区保留及提交后新修订均有受控断言。显示条数覆盖未加载、分页、收新回执和空列表。[UI 回归](candidate-267063f/junit/TEST-team.segroup11.review.SlowWorkflowTest.xml) · [状态](candidate-267063f/junit/TEST-team.segroup11.review.state.ReviewStateTest.xml) | PyCharm #16 的 ID、作者、逐字正文、0→1 及发送后新编辑均已核对，其他 IDE 各自留档 |
| C2 · 状态与测试 | 已收但回执未知时先读取；用户决定是否重发 | 模拟丢回执、确定拒绝、人工确认与重启无自动重发均通过。[网关](candidate-267063f/junit/TEST-team.segroup11.review.github.GitHubGatewayTest.xml) · [状态](candidate-267063f/junit/TEST-team.segroup11.review.state.ReviewStateTest.xml) | 真实发布对账与用户界面的未知结果核对 |
| N3 · 四组 | 同一包通过四款 ABI 与依赖检查，并逐款执行五组实操 | 官方 CLI 及 Gradle verifyPlugin 对同一 ZIP 均得到四份 Compatible，每款 5 处弃用、6 处实验 API，未报告缺失必需依赖。[原始报告](candidate-267063f/verifier/result.json) · [Gradle 复核](candidate-267063f/verifier/gradle-verifyPlugin.txt) | 四款完整五组操作按原条件验收 |

## 失败、修复与复测

授权后的 WebStorm 0.2.1 成功发表 #17 的第 1 条评论。GitHub API 回读作者 material4002 与创建时间；IDE 的讨论区却显示未知作者/日期、新评论数量仍为 0。旧安装包在两个独立类加载器下用原始 JSON 重现了字段丢失。0.2.2 直接映射 SDK 的 JSON 字段，同时让已加载评论集合按 ID 去重后驱动显示条数。对新包产品 JAR 运行同一个驱动，读回了 #17 的作者、创建时间，以及评论 7125780145 的作者和时间。[旧包回执](candidate-e66ed31/github/webstorm-receipt.json) · [旧包失败](https://github.com/SeGroup11/scrum-workspace/runs/138308671600) · [新包驱动结果](candidate-267063f/github/isolated-loader-result.txt)

## 四款 IDE 的实际覆盖

同包已在独立测试配置中完成四款二进制检验，另在实际 IDE 内分别观察 SDK 是否运行。IU/PY 两款各完成 JSON 对象与严格 Schema 两次回环请求，隔离 WS/RD 在观察器前被许可证检查阻止。正常 Windows 用户配置中的安装与操作单独记录。[二进制 Check](https://github.com/SeGroup11/scrum-workspace/runs/138316956854) · [运行时原始记录](candidate-267063f/runtime/result.json) · [设置/重启 Check](https://github.com/SeGroup11/scrum-workspace/runs/138317025421)

四个目标是 IntelliJ IDEA IU-262.10315.125、WebStorm WS-262.10315.144、Rider RD-262.9437.287 和 PyCharm PY-262.9437.214。真实 IDE、真实私仓、受控模型、本地状态与平台兼容各有独立的可判定结果。0.2.1 的真实 PyCharm 三次首帧为 6、5、19 ms，分别对应成功、HTTP 503 和重试；此组历史数据只归该版本。[历史操作与原始日志](candidate-e66ed31/gui/pycharm-live/README.md)

### PyCharm 真私仓与原生任务

同一 0.2.2 包从磁盘安装并正常重启，已装主 JAR 与交付 ZIP 内主 JAR 的散列逐字相同。生产按钮首帧 9 ms，慢成功耗时 10,622 ms。主账号在 #16 点击发送，等待回执时写入另一份含两个尾空格的新稿。GitHub 独立核实评论 7126271956、作者 material4002、逐字正文和时间。A 的条数 0→1，B 仍为 1，窗口立即显示 1 条并保留后写的文字。已修复 0.2.1 丢作者/日期与旧计数的问题。[实机归档](candidate-267063f/gui/pycharm.md) · [GitHub 评论](https://github.com/SeGroup11/scrum-workspace/issues/16#issuecomment-7126271956) · [QA 逐字读回](candidate-267063f/github/pycharm-receipt.json) · [Check](https://github.com/SeGroup11/scrum-workspace/runs/138322151486)

第一次从插件将 A 或 B 设为原生 IDE 任务时，Tasks 已切换，右窗却变为空选择；启动阶段也曾需要手动重新检测远程仓库。返回已绑定的原生任务时，A/B 的页签、独立草稿、A 评论回执均恢复。原生 tasks.zip 及 workspace.xml 证实数据仍在。首次激活与启动恢复仍列为失败，后续修复按新版本独立取证。[原生任务与工作区](candidate-267063f/gui/pycharm-live/native-task-snapshots.json) · [缺陷 Check](https://github.com/SeGroup11/scrum-workspace/runs/138322188361)

Rider RD-262.9437.287 已正常从磁盘安装同包并成功动态加载，已安装 JAR 散列与 ZIP 内文件一致。[Rider 安装证据](candidate-267063f/gui/rider.md)
