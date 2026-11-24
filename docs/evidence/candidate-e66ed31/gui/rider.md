# Rider 正常配置实机观察

2026-11-24 16:03 启动了正常用户配置中的 Rider 2026.2.1，启动日志确认进程为 RD-262.9437.287，JBR 为 25.0.3。[启动日志](rider-startup.log)

Rider 显示「导入设置」首次配置页，提供从现有 IDE 导入和「跳过导入」。[实机截图](rider-first-import.png)

用户完成 GitHub 授权后继续 Rider 首次配置，跳过导入和可选插件，保留默认 Rider 主题和 Visual Studio 键位。正常配置进入欢迎页，从「配置 → 插件」能打开管理器，齿轮菜单提供「从磁盘安装插件」。本轮没有在 Rider 安装旧候选 `e66ed31`；下一候选将从此入口安装。[Rider 欢迎页](rider-welcome.png)
