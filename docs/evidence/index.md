# 产品故事与实测索引

2026 年 11 月 24 日，本次工作从 `main@2e60d2f` 开始。#1、#2、#3 和 scrum-smoke 发布草稿用于身份演练。产品测试仓库为 SeGroup11/scrum-workspace，A 为 [#16 导出文件为空](https://github.com/SeGroup11/scrum-workspace/issues/16)，B 为 [#17 导出搜索结果](https://github.com/SeGroup11/scrum-workspace/issues/17)。[组织看板](https://github.com/orgs/SeGroup11/projects/1)

| 故事 | 使用场景 | 思源设计 | PR 与评审 | 实现提交 | 验收用例 |
| --- | --- | --- | --- | --- | --- |
| [1.1 · #4](https://github.com/SeGroup11/scrum-workspace/issues/4) | 在 IDE 启动最小插件并读取一条私有测试 Issue | [章节](siyuan://blocks/20261124134925-vgw00re) | [#18](https://github.com/SeGroup11/scrum-workspace/pull/18) | [4bc2b85](https://github.com/SeGroup11/scrum-workspace/commit/4bc2b85) | G1/I1/N3 |
| [1.2 · #5](https://github.com/SeGroup11/scrum-workspace/issues/5) | 选择并恢复仓库与 IDE 账号 | [章节](siyuan://blocks/20261123160030-ys7o1n2) | [#18](https://github.com/SeGroup11/scrum-workspace/pull/18) | [862b0c6](https://github.com/SeGroup11/scrum-workspace/commit/862b0c6) | G1 |
| [1.3 · #6](https://github.com/SeGroup11/scrum-workspace/issues/6) | 按页浏览 Issue 并按需打开讨论 | [章节](siyuan://blocks/20261123160143-c1m4z62) | [#18](https://github.com/SeGroup11/scrum-workspace/pull/18) | [ae3e8ee](https://github.com/SeGroup11/scrum-workspace/commit/ae3e8ee) | I1/I2 |
| [1.4 · #7](https://github.com/SeGroup11/scrum-workspace/issues/7) | 在工具窗口查看原文并使用可重放的评审夹具 | [章节](siyuan://blocks/20261121235754-hmmmmc9) | [#18](https://github.com/SeGroup11/scrum-workspace/pull/18) | [ae3e8ee](https://github.com/SeGroup11/scrum-workspace/commit/ae3e8ee) | I1/I2/R1 |
| [2.1 · #8](https://github.com/SeGroup11/scrum-workspace/issues/8) | 在原生设置配置模型服务与凭据 | [章节](siyuan://blocks/20261123160030-0qu8aot) | [#19](https://github.com/SeGroup11/scrum-workspace/pull/19) | [58e218f](https://github.com/SeGroup11/scrum-workspace/commit/58e218f) | 设置专项 |
| [2.2 · #9](https://github.com/SeGroup11/scrum-workspace/issues/9) | 按六项协议检查 Issue 并核对原文 | [章节](siyuan://blocks/20261123160324-z6udvja) | [#19](https://github.com/SeGroup11/scrum-workspace/pull/19) | [58e218f](https://github.com/SeGroup11/scrum-workspace/commit/58e218f) | R1/R2 |
| [2.3 · #10](https://github.com/SeGroup11/scrum-workspace/issues/10) | 后台分析与任务切换后继续编辑 | [章节](siyuan://blocks/20261123160446-51hhsm7) | [#19](https://github.com/SeGroup11/scrum-workspace/pull/19) | [b0b9e74](https://github.com/SeGroup11/scrum-workspace/commit/b0b9e74) | N1a/N1b/N2a/N2b |
| [2.4 · #11](https://github.com/SeGroup11/scrum-workspace/issues/11) | 预览、采纳并显式发布最终草稿 | [章节](siyuan://blocks/20261123160446-dt9u3q7) | [#19](https://github.com/SeGroup11/scrum-workspace/pull/19) | [0457462](https://github.com/SeGroup11/scrum-workspace/commit/0457462) | C1/C2 |
| [3.1 · #12](https://github.com/SeGroup11/scrum-workspace/issues/12) | 从干净检出复跑自动回归 | [章节](siyuan://blocks/20261123160601-jzkx80e) | [#21](https://github.com/SeGroup11/scrum-workspace/pull/21) | [f8e1c4a](https://github.com/SeGroup11/scrum-workspace/commit/f8e1c4a) | 12 项及设置 |
| [3.2 · #13](https://github.com/SeGroup11/scrum-workspace/issues/13) | 核对旧快照和丢失回执的恢复 | [章节](siyuan://blocks/20261123160446-51hhsm7) | [#21](https://github.com/SeGroup11/scrum-workspace/pull/21) | [b0b9e74](https://github.com/SeGroup11/scrum-workspace/commit/b0b9e74) | N1b/C2 |
| [3.3 · #14](https://github.com/SeGroup11/scrum-workspace/issues/14) | 在四款 IDE 验收同一候选 ZIP | [章节](siyuan://blocks/20261124134925-ktkq6x0) | [#21](https://github.com/SeGroup11/scrum-workspace/pull/21) | [f8e1c4a](https://github.com/SeGroup11/scrum-workspace/commit/f8e1c4a) | N3 |
| [3.4 · #15](https://github.com/SeGroup11/scrum-workspace/issues/15) | 复现一次修复并交付课程材料 | [章节](siyuan://blocks/20261124134925-3vkvrlb) | [#21](https://github.com/SeGroup11/scrum-workspace/pull/21) | [4548253](https://github.com/SeGroup11/scrum-workspace/commit/4548253) | 课程材料 |

## 第一轮的失败与复测

- 在 `ae3e8ee` 上，真实 SDK 解码回放的 4 个网关测试通过。该提交的首包因插件显示名含非法字符被 Verifier 拒绝，进入验证的 IDE 数为 0。[测试](https://github.com/SeGroup11/scrum-workspace/runs/138261486179) · [失败](https://github.com/SeGroup11/scrum-workspace/runs/138262252679)
- `4bc2b85` 将描述符显示名改为英文。Gradle 从隔离检出运行 4 个测试，全部通过。同一 ZIP `650a34355f4c4c81f637f723b3a4462390ea90a7987dca57787b9fa578d942e4` 在 IU、WS、RD、PY 都获得 Compatible，每份报告另列 4 处过时和 6 处实验 API。[复测](https://github.com/SeGroup11/scrum-workspace/runs/138265147006) · [流程跟进 #20](https://github.com/SeGroup11/scrum-workspace/issues/20)
- 源码审查发现了仓库大小写、默认账号选择、刷新失败丢行和 SDK 可空字段问题。修复、变更与评审均可从 [PR #18](https://github.com/SeGroup11/scrum-workspace/pull/18) 定位。

## 过程证据的范围

| 轮次 | 实际交付与问题 | 可追溯记录 |
| --- | --- | --- |
| Sprint 1 | 实现 IDE 账号、私仓读取入口与 Task 映射。Verifier 拒绝首包后更正显示名并重验四款。 | [PR #18](https://github.com/SeGroup11/scrum-workspace/pull/18) · [首轮复测](https://github.com/SeGroup11/scrum-workspace/runs/138265147006) · [回顾 #20](https://github.com/SeGroup11/scrum-workspace/issues/20) |
| Sprint 2 | 增加模型、逐字引文、草稿、上下文和显式发送。独立检出 PR 提交 0457462 运行 17 项受控测试。 | [PR #19](https://github.com/SeGroup11/scrum-workspace/pull/19) · [17 项回归](https://github.com/SeGroup11/scrum-workspace/runs/138267977370) |
| Sprint 3 | 在候选 2974097 重做干净构建、慢响应及四款 Verifier；修复报告分类说明和展开。 | [PR #21](https://github.com/SeGroup11/scrum-workspace/pull/21) · [逐项验收矩阵](acceptance.md) · [流程回查 #22](https://github.com/SeGroup11/scrum-workspace/issues/22) · [Release 草稿](https://github.com/SeGroup11/scrum-workspace/releases/tag/untagged-ee81e4b2606f38839f92) |

## 0.2.1 主账号实测与修复

在故事 1.2 的 WebStorm 私仓实测中，旧包在主账号重新登录后仍给出笼统的身份验证失败提示。新包为所选账号增加原生重新认证入口，并区分身份验证、仓库读取及后续操作的失败。QA 复跑 21 个独立测试，记录了账号阶段错误、取消重认证和确认重认证的结果。[故事 #5](https://github.com/SeGroup11/scrum-workspace/issues/5) · [候选 e66ed31](https://github.com/SeGroup11/scrum-workspace/commit/e66ed311cf986d876814f7737f897b526322236a) · [QA Check](https://github.com/SeGroup11/scrum-workspace/runs/138292777291) · [0.2.1 验收矩阵](acceptance-0.2.1.md) · [0.2.0 历史矩阵](acceptance-0.2.0.md)

看板有三个 Sprint，每个 Sprint 四张故事。各卡的实际状态可按序号回读。0.2.1 的锁定产物、课程材料及测试范围汇在新的发布草稿。[看板快照](candidate-e66ed31/github/project.json) · [S1 修复评审](https://github.com/SeGroup11/scrum-workspace/pull/18#pullrequestreview-6035485040) · [S3 候选评审](https://github.com/SeGroup11/scrum-workspace/pull/21#pullrequestreview-6035485566) · [发布列表中的 v0.2.1-rc.1 草稿（ID 456437638）](https://github.com/SeGroup11/scrum-workspace/releases)

三条产品分支在同一日实际执行。仓库分别保留主责组、交叉评审角色、Git 作者和 QA 身份。八名成员的姓名、独立成员评审、会议、真实用户反馈及其他组复现仅在实际发生后补入。

## 私仓实发暴露的字段与计数缺陷

组织授权后，WebStorm 和 PyCharm 都读到了私仓 A/B 的原文。WebStorm 使用 material4002 对 #17 发送一次，独立 QA 验证评论 ID 7125780145、逐字正文和 0→1；#16 仍为 0。0.2.1 把评论作者及日期显示为未知，讨论区仍显示 0 条。旧安装包的独立类加载器复现了字段丢失。同一候选的 PyCharm 对 10 秒成功、503、重试分别测得首帧 6/5/19 ms。[评论与回执](candidate-e66ed31/github/webstorm-receipt.json) · [缺陷 Check](https://github.com/SeGroup11/scrum-workspace/runs/138308671600) · [真实首帧与交互](https://github.com/SeGroup11/scrum-workspace/runs/138309854800)

故事 1.3 和 2.4 的修复将作者与时间改为直接映射 API 字段，并让界面从当前去重后的评论集合计算条数。新候选源码为 267063f。新包从干净归档执行 23 项回归，双类加载器读取了实际作者和日期，四个精确构建均为 Compatible。故事 1.1 的首轮可安装、可读私仓路径已按原条件关闭，其余卡按各自验收条件追踪。[23 项 Check](https://github.com/SeGroup11/scrum-workspace/runs/138315005604) · [0.2.2 评审](https://github.com/SeGroup11/scrum-workspace/pull/21#pullrequestreview-6035965374) · [看板快照](candidate-267063f/github/project.json) · [最新验收矩阵](acceptance.md)。[真实字段输入](candidate-e66ed31/github/field-regression-input.json) · [修复提交](https://github.com/SeGroup11/scrum-workspace/commit/267063f8d7fc2d321f03d386b2ce9c64553c916c) · [0.2.1 验收矩阵](acceptance-0.2.1.md)

## 0.2.3 冷启动回归与复核

0.2.2 的原生 Tasks 首次绑定 A 和 B 都会清掉右侧详情；同版的两个正常启动进程还曾在 Git 就绪前完成仓库扫描，需要手动重新检测。0.2.3 在首次任务切换时保留目标定位，并在 Git 发布仓库映射时重新尝试发现。使用相同观察器，0.2.2 的两次首次绑定失败，0.2.3 的首次 A、首次 B、切回 A、切回 B 四次均通过。PyCharm 的正常用户配置还复测了冷启动自动恢复私仓、账号和 #17，在此前从未绑定的私仓 #20 上验证原生任务标题与插件详情稳定一致。[修复提交](https://github.com/SeGroup11/scrum-workspace/commit/3f9c974b84a716ffbf8aa6c04d732601c0e3b4e0) · [0.2.2 原始失败](candidate-267063f/task-activation/result.txt) · [0.2.3 原生复测](candidate-3f9c974/task-activation/result.txt) · [真实 PyCharm](candidate-3f9c974/gui/pycharm.md) · [回顾行动 #23](https://github.com/SeGroup11/scrum-workspace/issues/23)

最终安装包为 `github-issue-review-0.2.3.zip`，SHA-256 `d0e5c1af07918baa377a0a81151dabaace0ade7f5d7c5617a4b447315a24000f`。26 项自动测试通过，Gradle verifyPlugin 与独立 Verifier 在四款精确 build 上均报告 Compatible。真实两进程启动和关闭保留了逐字符草稿和报告身份；模型在重开时收到零次请求。两个项目的设置和按地址隔离的凭据也通过原生服务复测。[逐项矩阵](acceptance.md) · [实现评审](https://github.com/SeGroup11/scrum-workspace/pull/21#pullrequestreview-6036360001) · [构建和自动测试](https://github.com/SeGroup11/scrum-workspace/runs/138328199869) · [四款兼容性](https://github.com/SeGroup11/scrum-workspace/runs/138332736163) · [Tasks 和重启](https://github.com/SeGroup11/scrum-workspace/runs/138332764452) · [设置与 PasswordSafe](https://github.com/SeGroup11/scrum-workspace/runs/138332795842)

真实桌面发送通过账号 material4002 操作，GitHub 的独立 GET 记录编号、原文、作者及当时 A/B 条数。模型测试使用本机可回放服务。R1 的两份响应均满足协议，但固定响应错误地将 A 的两步复现判为缺失，各例保留原始输入和模型 JSON。[远端回执](candidate-3f9c974/github) · [语义回放与结果](candidate-3f9c974/model) · [课程报告](../course/report.zh-CN.md) · [可下载的私仓发布草稿](https://github.com/SeGroup11/scrum-workspace/releases)
