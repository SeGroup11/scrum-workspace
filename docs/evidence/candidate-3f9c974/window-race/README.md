# 两个账号、两个仓库、两个晚到回包

PyCharm PY-262.9437.214 的独立无界面进程载入原始 0.2.3 ZIP 和一个独立测试插件。测试项目没有 IDE 账号与 Git remote。观察器实例化候选的生产 ReviewWindow，仅在该实例的 GitHubGateway 中设置受控 ConnectionAccess。合成身份为 one/101、two/202，合成仓库为 qa/alpha 和 qa/beta。请求只进入内存 transport，JSON 与分页 Link 头经过原装 GitHub 插件的请求解码器。控件在 IDE 的 EDT 上操作，网络等待在另一个线程。此项观测未测量可见绘制时间。[观察器](WindowProbe.java) · [IDE 加载记录](ide-lifecycle.log)

首次运行时观察器自身在 EDT 等待，导致已排队的 UI 回调无法运行。修正观察器的 ApplicationStarter 调度后，测试先等 A 的详情和评论分别进入闸门，再从窗口手选另一账号和另一仓库。B 的详情、评论均已返回且实际编辑器保存 B 的新稿，再释放 A 的两项迟到响应。标题、正文、评论、计数、回复目标与草稿均保持 B。两个仓的第一页均只有 PR，控件自动取第二页且浏览列表没有拉取评论。CLOSED 筛选走两页，完整编号 #17 能命中，#4 不能误匹配。[逐请求与控件记录](result.txt)

0.2.3 的额外重检出现一项失败。选择 B 之后，窗口仍留着旧 A 的待恢复提示；点击“重新检测”会正确选择账号 two 和仓库 Beta，但右窗变成“请选择 Issue”。此时项目的最新位置仍是 Beta #17。测试正常保存、关闭项目再重开，自动恢复 two / Beta #17、详情、评论以及逐字相同的 B 草稿。输出分别记录每一步的连接提示、标题、回复目标、编辑器文字、待恢复位置与项目最新位置。它没有在 Beta 请求中使用 Alpha 的身份，也没有向任一 Issue 发评论。[同窗重检与正常重开的对照](result.txt)

复跑时提供原始 ZIP、对应 SHA-256 和本机安装目录。每轮单独创建项目及 IDE 配置；任何一项判定失败均使脚本以非零退出。

```powershell
./run.ps1 -Zip $env:CANDIDATE_ZIP -Sha256 $env:CANDIDATE_SHA256 -PyCharm $env:PYCHARM_HOME -Output ./build/window-race
```
