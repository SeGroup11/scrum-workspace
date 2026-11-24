# 正常 IDE 重开期间的插件调用

每个产品先从原生菜单正常保存并退出测试项目。一次性启动脚本以同一项目、同一账户配置重开，只对这次测试进程启用 JBR 自带的 Flight Recorder。PyCharm 使用同一隔离测试 profile 的 properties 与 vmoptions。待恢复的 A/B 原文、空草稿和另一份新稿可读后停止记录。只读核对列表和详情可以检验记录器工作正常。脚本输出四个指定方法的时间与名称、进程启动时间和原始记录 SHA-256，不采集方法参数、草稿、凭据或调用栈。[JDK 25 事件过滤语法](https://docs.oracle.com/en/java/javase/25/docs/api/jdk.jfr/jdk/jfr/package-summary.html)

产品的评论 UI 进入 `GitHubGateway.postComment` 后才可能构造 GitHub 创建评论请求。模型分析从 `ModelGateway.evaluate` 进入。恢复窗口内有连接与详情的正记录、这两个入口同时为零，才能据此核定插件未触发发送或分析。具体网络写入结果用 GitHub 回执、回环 HTTP 日志独立对照。含挂起的 Kotlin 方法可能因重新进入而有多条追踪事件，有重入时不将正事件数量解释为 HTTP 请求数。[唯一评论调用点](../../../../src/main/kotlin/team/segroup11/review/ReviewEditor.kt) · [模型入口](../../../../src/main/kotlin/team/segroup11/review/state/ReviewStore.kt)

对照运行直接加载同一安装包中的网关，进行了三个尝试：一次明确拒绝、一次显式重试、一次服务器写入后断线。Flight Recorder 实收一次连接和三次 `postComment`，与独立服务器记录的三个 POST 尝试一致。[方法记录](ledger-positive-control.json) · [请求记录](../comment-recovery/result.txt)

正常 IntelliJ IDEA 的另一个显式操作窗口捕获 2 条详情调用和 1 条模型入口调用，验证分析方法过滤在实际 IDE 类加载器中有效。[正常进程的阳性对照](idea-analysis-positive-control.json)

```powershell
.\Start-RestartTrace.ps1 -Product idea -Executable $ideaExe -ProjectPath $testProject
# 在正常工程内等待恢复并核对 A/B，然后使用脚本输出的进程号
.\Complete-RestartTrace.ps1 -Product idea -ProductProcessId $idePid -Jbr $jbr -Output $newOutputDirectory
```
