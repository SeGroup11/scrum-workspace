# PyCharm 对私仓的慢分析和草稿实测

2026-11-24，获得组织授权后，在 PyCharm PY-262.9437.214 的真实 Issue Review 工具窗口重新连接。候选 ZIP 为 0.2.1，SHA-256 为 `71c549a8203edb80060d37411ff344a32b10cec534982121163fe920c52b4cd4`。主 GitHub 账号读取 SeGroup11/scrum-workspace 的 18 张 Issue，包括 A #16 和 B #17。实际 GitHub 提供标题与正文，模型答案由本机受控 HTTP 服务提供。[私仓列表](2026-11-24T08-10-47-255Z-private-list.jpg) · [A 的原文](2026-11-24T08-11-14-565Z-a-before.jpg)

| 正常窗口真实点击 | 首帧 | IDE 请求耗时 | 受控模型服务 |
| --- | ---: | ---: | --- |
| 为 A 分析 slow-success | 6 ms | 10,910 ms | 1 次 HTTP 请求，服务耗时 10,000 ms |
| 改为 slow-error 后为 A 分析；等待中又点了一次灰色按钮 | 5 ms | 10,041 ms | 1 次 HTTP 请求，服务耗时 10,016 ms，返回 503 |
| 改回 slow-success，显式点击为 A 重试 | 19 ms | 10,014 ms | 1 次 HTTP 请求，服务耗时 10,000 ms |

首帧读自候选生产按钮的 paintComponent。它在“分析中…”已通过 Swing 绘出且 isShowing 为真时，用 System.nanoTime 计算与本次正常按钮点击的间隔；以上三次均低于 500 ms。IDE 日志时间为 UTC+8，JPEG 文件名使用 UTC。[生产插件原始日志](production-timings.log) · [模型服务原始计数](loopback-calls.log)

- 第一次等待时，实际滚动 A 的长正文、编辑 A 草稿并点左边列表切到 B。成功结果归 A；B 的报告页仍显示“尚无评估报告”。[等待反馈](2026-11-24T08-11-15-031Z-success-pending.jpg) · [正文滚动](2026-11-24T08-11-15-488Z-success-scroll.jpg) · [编辑 A](2026-11-24T08-11-16-341Z-success-edit-a.jpg) · [切 B](2026-11-24T08-11-16-787Z-success-switch-b.jpg) · [B 无报告](2026-11-24T08-11-49-108Z-success-b-no-report.jpg) · [A 的报告](2026-11-24T08-12-00-781Z-success-a-report.jpg)
- 失败请求等待中同样滚动、编辑 A、切换 B。返回 A 后，可见“模型服务返回 HTTP 503”、先前生成的 slow-success 报告及在失败请求期间追加的草稿。[失败等待](2026-11-24T08-12-34-158Z-error-pending.jpg) · [在途滚动](2026-11-24T08-12-35-112Z-error-scroll.jpg) · [失败后的旧报告和新草稿](2026-11-24T08-13-25-435Z-error-a-old-report-and-draft.jpg)
- 在 A 的旧报告中选择“复现步骤”，点击“用于回复”后查看建议预览，再在草稿中按 Tab，建议文本进入当前草稿。这一轮在 PyCharm 没有点击发送。[选择](2026-11-24T08-13-25-900Z-suggestion-selected.jpg) · [预览](2026-11-24T08-13-26-375Z-suggestion-preview.jpg) · [采纳](2026-11-24T08-13-26-818Z-suggestion-accepted.jpg)
- 失败后手动换回成功模型并重试。10 秒内真实滚动、切到 B 并改写独立草稿，持续停留在 B 至 A 完成。可按 UTC 文件名依次查看 retry-ready 至 retry-a-report 的时间序列。[重试](2026-11-24T08-14-38-506Z-retry-pending.jpg) · [滚动](2026-11-24T08-14-39-004Z-retry-scroll.jpg) · [B 实际编辑](2026-11-24T08-14-41-524Z-retry-b-edited.jpg) · [十秒后仍在 B](2026-11-24T08-14-48-450Z-retry-wait-5.jpg) · [A 重试报告](2026-11-24T08-14-57-949Z-retry-a-report.jpg)

0.2.1 有一个可见缺陷。两张真实 Issue 均渲染出正确标题和正文，但详情把作者、日期显示为未知；B 中可读到另一产品发布的评论内容和 ID，但评论作者也显示为未知。该现象保留作后续修复对照。[B 的真实讨论](2026-11-24T08-11-29-185Z-success-edit-b.jpg)
