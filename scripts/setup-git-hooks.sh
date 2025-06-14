#!/bin/bash
#
# 安装Git hooks脚本
# 用于设置提交信息格式检查
#

echo "正在安装Git hooks..."

# 创建.git/hooks目录（如果不存在）
mkdir -p .git/hooks

# 复制commit-msg hook
cp .githooks/commit-msg .git/hooks/commit-msg

# 设置执行权限
chmod +x .git/hooks/commit-msg

echo "Git hooks安装完成！"
echo "现在所有提交都会自动检查格式是否符合规范。"
echo "详细规范请查看 COMMIT_CONVENTION.md" 