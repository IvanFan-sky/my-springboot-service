package com.spark.demo.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.spark.demo.dto.LoginDTO;
import com.spark.demo.dto.PasswordLoginDTO;
import com.spark.demo.dto.SmsLoginDTO;
import com.spark.demo.dto.UserDTO;
import com.spark.demo.entity.User;
import com.spark.demo.vo.UserVO;

/**
 * 用户服务接口
 * @author spark
 * @date 2025-05-29
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     * @param userDTO 用户注册信息
     */
    void register(UserDTO userDTO);

    /**
     * 用户登录（兼容原有方法）
     * @param loginDTO 登录信息
     * @return SessionId
     */
    String login(LoginDTO loginDTO);
    
    /**
     * 密码登录
     * @param passwordLoginDTO 密码登录信息
     * @return SessionId
     */
    String passwordLogin(PasswordLoginDTO passwordLoginDTO);
    
    /**
     * 短信验证码登录
     * @param smsLoginDTO 短信登录信息
     * @return SessionId
     */
    String smsLogin(SmsLoginDTO smsLoginDTO);

    /**
     * 获取当前登录用户信息
     * @param userId 用户ID（内部使用）
     * @return 当前登录用户信息
     */
    UserVO getCurrentUserInfo(Long userId);

    /**
     * 根据UUID获取用户信息
     * @param uuid 用户UUID
     * @return 用户信息
     */
    UserVO getUserByUuid(String uuid);

    /**
     * 新增用户 (管理员操作)
     * @param userDTO 用户信息
     * @return 新增用户信息
     */
    UserVO addUser(UserDTO userDTO);

    /**
     * 根据UUID删除用户 (逻辑删除，管理员操作)
     * @param uuid 用户UUID
     */
    void deleteUserByUuid(String uuid);

    /**
     * 根据ID删除用户 (逻辑删除，管理员操作) - 内部使用
     * @param id 用户ID
     */
    void deleteUserById(Long id);

    /**
     * 根据UUID更新用户信息 (管理员或用户自己操作)
     * @param uuid 用户UUID
     * @param userDTO 用户信息
     * @return 更新后的用户信息
     */
    UserVO updateUserByUuid(String uuid, UserDTO userDTO);

    /**
     * 更新用户信息 (管理员或用户自己操作) - 内部使用
     * @param id 用户ID
     * @param userDTO 用户信息
     * @return 更新后的用户信息
     */
    UserVO updateUser(Long id, UserDTO userDTO);

    /**
     * 根据ID查询用户 - 内部使用
     * @param id 用户ID
     * @return 用户信息
     */
    UserVO getUserById(Long id);

    /**
     * 分页查询用户列表
     * @param page 分页信息
     * @param userDTO 查询条件
     * @return 用户列表
     */
    Page<UserVO> listUsers(Page<User> page, UserDTO userDTO);

    /**
     * 根据UUID查找用户实体（内部使用）
     * @param uuid 用户UUID
     * @return 用户实体
     */
    User findByUuid(String uuid);

    /**
     * 修改密码
     * @param uuid 用户UUID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    void changePassword(String uuid, String oldPassword, String newPassword);

    /**
     * 修改用户状态
     * @param uuid 用户UUID
     * @param status 新状态 (0-禁用, 1-正常)
     * @return 更新后的用户信息
     */
    UserVO updateUserStatus(String uuid, Integer status);
}