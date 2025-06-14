package com.spark.demo.modules.user.domain;

import com.spark.demo.entity.User;
import com.spark.demo.common.util.PasswordUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.UUID;

/**
 * 用户领域实体
 * 包含用户相关的业务逻辑
 * @author spark
 * @date 2025-06-14
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserDomain extends User {

    /**
     * 创建新用户
     * @param username 用户名
     * @param password 原始密码
     * @param phone 手机号
     * @param email 邮箱
     * @return 用户领域对象
     */
    public static UserDomain createUser(String username, String password, String phone, String email) {
        UserDomain userDomain = new UserDomain();
        userDomain.setUsername(username);
        userDomain.setPhone(phone);
        userDomain.setEmail(email);
        userDomain.setRole("user"); // 默认角色
        userDomain.setStatus(1); // 默认启用
        userDomain.generateUuid();
        userDomain.encryptPassword(password);
        return userDomain;
    }

    /**
     * 从数据库实体创建领域对象
     * @param user 数据库实体
     * @return 用户领域对象
     */
    public static UserDomain fromEntity(User user) {
        UserDomain userDomain = new UserDomain();
        userDomain.setId(user.getId());
        userDomain.setUuid(user.getUuid());
        userDomain.setUsername(user.getUsername());
        userDomain.setPassword(user.getPassword());
        userDomain.setPhone(user.getPhone());
        userDomain.setEmail(user.getEmail());
        userDomain.setNickname(user.getNickname());
        userDomain.setAvatar(user.getAvatar());
        userDomain.setGender(user.getGender());
        userDomain.setBirthday(user.getBirthday());
        userDomain.setRole(user.getRole());
        userDomain.setWechatId(user.getWechatId());
        userDomain.setAlipayId(user.getAlipayId());
        userDomain.setStatus(user.getStatus());
        userDomain.setCreatedTime(user.getCreatedTime());
        userDomain.setUpdatedTime(user.getUpdatedTime());
        userDomain.setDeletedTime(user.getDeletedTime());
        return userDomain;
    }

    /**
     * 转换为数据库实体
     * @return 数据库实体
     */
    public User toEntity() {
        User user = new User();
        user.setId(this.getId());
        user.setUuid(this.getUuid());
        user.setUsername(this.getUsername());
        user.setPassword(this.getPassword());
        user.setPhone(this.getPhone());
        user.setEmail(this.getEmail());
        user.setNickname(this.getNickname());
        user.setAvatar(this.getAvatar());
        user.setGender(this.getGender());
        user.setBirthday(this.getBirthday());
        user.setRole(this.getRole());
        user.setWechatId(this.getWechatId());
        user.setAlipayId(this.getAlipayId());
        user.setStatus(this.getStatus());
        user.setCreatedTime(this.getCreatedTime());
        user.setUpdatedTime(this.getUpdatedTime());
        user.setDeletedTime(this.getDeletedTime());
        return user;
    }

    /**
     * 生成UUID
     */
    @Override
    public void generateUuid() {
        if (this.getUuid() == null || this.getUuid().trim().isEmpty()) {
            this.setUuid(UUID.randomUUID().toString());
        }
    }

    /**
     * 加密密码
     * @param rawPassword 原始密码
     */
    public void encryptPassword(String rawPassword) {
        if (rawPassword != null && !rawPassword.trim().isEmpty()) {
            this.setPassword(PasswordUtil.encrypt(rawPassword));
        }
    }

    /**
     * 验证密码
     * @param rawPassword 原始密码
     * @return 是否匹配
     */
    public boolean verifyPassword(String rawPassword) {
        if (rawPassword == null || this.getPassword() == null) {
            return false;
        }
        return PasswordUtil.verify(rawPassword, this.getPassword());
    }

    /**
     * 修改密码
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否修改成功
     */
    public boolean changePassword(String oldPassword, String newPassword) {
        if (!verifyPassword(oldPassword)) {
            return false;
        }
        encryptPassword(newPassword);
        return true;
    }

    /**
     * 检查用户是否激活
     * @return 是否激活
     */
    public boolean isActive() {
        return this.getStatus() != null && this.getStatus() == 1;
    }

    /**
     * 激活用户
     */
    public void activate() {
        this.setStatus(1);
    }

    /**
     * 禁用用户
     */
    public void deactivate() {
        this.setStatus(0);
    }

    /**
     * 检查是否为管理员
     * @return 是否为管理员
     */
    public boolean isAdmin() {
        return "admin".equals(this.getRole());
    }

    /**
     * 检查是否已删除
     * @return 是否已删除
     */
    public boolean isDeleted() {
        return this.getDeletedTime() != null;
    }

    /**
     * 更新用户信息
     * @param username 用户名
     * @param phone 手机号
     * @param email 邮箱
     * @param nickname 昵称
     */
    public void updateBasicInfo(String username, String phone, String email, String nickname) {
        if (username != null && !username.trim().isEmpty()) {
            this.setUsername(username);
        }
        if (phone != null && !phone.trim().isEmpty()) {
            this.setPhone(phone);
        }
        if (email != null && !email.trim().isEmpty()) {
            this.setEmail(email);
        }
        if (nickname != null && !nickname.trim().isEmpty()) {
            this.setNickname(nickname);
        }
    }

    /**
     * 设置角色（仅管理员可操作）
     * @param role 角色
     * @param operatorRole 操作者角色
     * @return 是否设置成功
     */
    public boolean setRole(String role, String operatorRole) {
        if (!"admin".equals(operatorRole)) {
            return false;
        }
        this.setRole(role);
        return true;
    }

    /**
     * 验证用户数据完整性
     * @return 验证结果
     */
    public boolean validate() {
        return this.getUsername() != null && !this.getUsername().trim().isEmpty()
                && this.getPassword() != null && !this.getPassword().trim().isEmpty()
                && this.getUuid() != null && !this.getUuid().trim().isEmpty();
    }
} 