# PowerShell脚本：安装Git hooks
# 用于设置提交信息格式检查

Write-Host "正在安装Git hooks..." -ForegroundColor Green

# 检查是否在Git仓库中
if (-not (Test-Path ".git")) {
    Write-Host "错误：当前目录不是Git仓库！" -ForegroundColor Red
    exit 1
}

# 创建.git/hooks目录（如果不存在）
$hooksDir = ".git/hooks"
if (-not (Test-Path $hooksDir)) {
    New-Item -ItemType Directory -Path $hooksDir -Force | Out-Null
}

# 复制commit-msg hook
$sourceHook = ".githooks/commit-msg"
$targetHook = "$hooksDir/commit-msg"

if (Test-Path $sourceHook) {
    Copy-Item $sourceHook $targetHook -Force
    Write-Host "✓ commit-msg hook已安装" -ForegroundColor Green
} else {
    Write-Host "错误：找不到源文件 $sourceHook" -ForegroundColor Red
    exit 1
}

# 在Windows上，Git会自动处理脚本执行权限
Write-Host "✓ Git hooks安装完成！" -ForegroundColor Green
Write-Host ""
Write-Host "现在所有提交都会自动检查格式是否符合规范。" -ForegroundColor Yellow
Write-Host "详细规范请查看 COMMIT_CONVENTION.md" -ForegroundColor Yellow
Write-Host ""
Write-Host "测试提交格式检查：" -ForegroundColor Cyan
Write-Host "  正确格式: feat(auth): 添加用户登录功能" -ForegroundColor Green
Write-Host "  错误格式: 添加用户登录功能" -ForegroundColor Red 