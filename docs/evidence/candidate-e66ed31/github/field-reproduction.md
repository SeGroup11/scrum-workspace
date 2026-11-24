# 复现已安装插件的作者与时间丢失

WebStorm 0.2.1 在读到真实 Issue 和评论时丢失作者与日期。GitHub 独立读回确认源字段存在。[真实字段摘要](field-regression-input.json) · [实机观察](../gui/webstorm.md)

单元测试中的 SDK 与插件共用一个类加载环境。实际打包中，GitHub 插件自带 Jackson 注解，课程插件的分发包也包含独立的 `jackson-annotations-2.18.9.jar`。旧适配器继承 SDK 数据类，在另一个字段上再以插件的 `@JsonProperty("user")` 等注解标出相同 JSON 属性。

驱动用父加载器提供本机 GitHub SDK，用子加载器加载课程插件 DTO 与它随包带的注解，然后调用 GitHub SDK 的 `GithubApiContentHelper.fromJson`。运行前会确认两个加载器各自得到不同的 `JsonProperty` 类型。以真实形态的 Issue 和评论 JSON 作为同一输入，旧候选两者的作者与日期均为 null。[可复跑驱动](FieldLoaderProbe.java) · [0.2.1 运行输出](isolated-loader-reproduction.txt)

在 Windows 上，将候选 ZIP 解压到临时目录后执行以下 PowerShell 命令。JDK 路径可替换为本机能编译 Java 17 或更新版本的 JDK。此步骤不连接 GitHub，不处理 OAuth 凭据。

```powershell
$testIde = 'C:\Users\33583\AppData\Local\Programs\PyCharm'
$testJdk = 'C:\Users\33583\.jdks\openjdk-24.0.1'
$evidenceDir = 'docs\evidence\candidate-e66ed31\github'
$probeOut = 'build\sdk-signatures'
New-Item -ItemType Directory -Force $probeOut | Out-Null
& "$testJdk\bin\javac.exe" -d $probeOut "$evidenceDir\FieldLoaderProbe.java"
$probeClassPath = "$probeOut;$testIde\lib\*;$testIde\plugins\vcs-github\lib\*"
$pluginLib = 'C:\temp\candidate\github-issue-review\lib'
& "$testIde\jbr\bin\java.exe" -cp $probeClassPath FieldLoaderProbe "$pluginLib\github-issue-review-0.2.1.jar" "$pluginLib\jackson-annotations-2.18.9.jar" old
```

用修复候选的同名类重跑时，指向其插件 JAR 和随包注解 JAR，将末尾参数从 `old` 改为 `new`。任一用例与预期不符时进程以非零状态退出。
