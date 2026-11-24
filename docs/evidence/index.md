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

三条产品分支在同一日实际执行。仓库分别保留主责组、交叉评审角色、Git 作者和 QA 身份。八名成员的姓名、独立成员评审、会议、真实用户反馈及其他组复现仅在实际发生后补入。
