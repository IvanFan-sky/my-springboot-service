package com.spark.demo.service.impl;

/**
 * @author spark
 * @date 2024-07-27
 */

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.spark.demo.common.context.UserContext;
import com.spark.demo.common.exception.BusinessException;
import com.spark.demo.common.result.ResultCode;
import com.spark.demo.converter.UserConverter;
import com.spark.demo.dto.LoginDTO;
import com.spark.demo.dto.PasswordLoginDTO;
import com.spark.demo.dto.SmsLoginDTO;
import com.spark.demo.dto.UserDTO;
import com.spark.demo.entity.User;
import com.spark.demo.mapper.UserMapper;
import com.spark.demo.service.SmsService;
import com.spark.demo.service.UserService;
import com.spark.demo.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 用户服务实现类
 * @author spark
 * @date 2025-05-29
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper,User> implements UserService{

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private SmsService smsService;

    private final UserConverter userConverter = UserConverter.INSTANCE;
    
    // 手机号正则
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    // 邮箱正则
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    // 用户名正则（字母、数字、下划线，3-20位）
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");

    @Override
    public void register(UserDTO userDTO) {
        log.info("用户注册开始，用户名: {}", userDTO.getUsername());
        
        try {
            // 输入参数验证
            validateUserDTO(userDTO, true);
            
            // 检查用户名是否已存在
            if (isUsernameExists(userDTO.getUsername())) {
                log.warn("用户注册失败，用户名已存在: {}", userDTO.getUsername());
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "用户名已存在");
            }
            
            // 检查手机号是否已存在
            if (StringUtils.hasText(userDTO.getPhone()) && isPhoneExists(userDTO.getPhone())) {
                log.warn("用户注册失败，手机号已存在: {}", maskPhone(userDTO.getPhone()));
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "手机号已存在");
            }
            
            // 检查邮箱是否已存在
            if (StringUtils.hasText(userDTO.getEmail()) && isEmailExists(userDTO.getEmail())) {
                log.warn("用户注册失败，邮箱已存在: {}", maskEmail(userDTO.getEmail()));
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "邮箱已存在");
            }

            User user = userConverter.dtoToEntity(userDTO);
            // 生成UUID
            user.generateUuid();
            // 加密密码
            user.setPassword(encryptPassword(userDTO.getPassword()));
            // 设置默认值
            if (!StringUtils.hasText(user.getRole())) {
                user.setRole("user"); // 默认角色
            }
            if (user.getStatus() == null) {
                user.setStatus(1); // 默认状态：正常
            }
            
            userMapper.insert(user);
            log.info("用户注册成功，UUID: {}, 用户名: {}", user.getUuid(), userDTO.getUsername());
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("用户注册失败，用户名: {}", userDTO.getUsername(), e);
            throw new BusinessException(ResultCode.FAIL, "注册失败，请稍后重试");
        }
    }

    @Override
    public String login(LoginDTO loginDTO) {
        String clientIp = getClientIpAddress();
        log.info("用户登录尝试，用户标识: {}, IP: {}", loginDTO.getUsername(), clientIp);
        
        try {
            // 输入验证
            if (!StringUtils.hasText(loginDTO.getUsername())) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "用户名不能为空");
            }
            
            if (!StringUtils.hasText(loginDTO.getPassword()) && !StringUtils.hasText(loginDTO.getCode())) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "密码或验证码不能为空");
            }
            
            // 查找用户（支持用户名/手机号登录）
            User user = findUserByUsernameOrPhone(loginDTO.getUsername());
            if (user == null) {
                log.warn("用户登录失败，用户不存在: {}, IP: {}", loginDTO.getUsername(), clientIp);
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "用户名或密码错误");
            }
            
            // 检查用户状态
            if (user.getStatus() == 0) {
                log.warn("用户登录失败，账号已被禁用: {}, IP: {}", user.getUuid(), clientIp);
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "账号已被禁用");
            }
            
            // 验证密码或验证码
            boolean authSuccess = false;
            if (StringUtils.hasText(loginDTO.getPassword())) {
                // 密码登录
                authSuccess = verifyPassword(loginDTO.getPassword(), user.getPassword());
                if (!authSuccess) {
                    log.warn("用户登录失败，密码错误: {}, IP: {}", user.getUuid(), clientIp);
                }
            } else if (StringUtils.hasText(loginDTO.getCode()) && StringUtils.hasText(user.getPhone())) {
                // 验证码登录
                authSuccess = smsService.verifyCode(user.getPhone(), loginDTO.getCode());
                if (!authSuccess) {
                    log.warn("用户登录失败，验证码错误: {}, IP: {}", user.getUuid(), clientIp);
                }
            }
            
            if (!authSuccess) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "用户名或密码错误");
            }
            
            // 设置session，使用UUID而不是数据库ID
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpSession session = attributes.getRequest().getSession();
                session.setAttribute("userUuid", user.getUuid());
                session.setAttribute("userId", user.getId()); // 内部使用
                session.setAttribute("username", user.getUsername());
                session.setAttribute("role", user.getRole());
                
                // 立即设置用户上下文，使得登录后立即可以获取当前用户信息
                UserContext.setCurrentUser(user);
                
                log.info("用户登录成功，UUID: {}, 用户名: {}, SessionId: {}, IP: {}", 
                        user.getUuid(), user.getUsername(), session.getId(), clientIp);
                return session.getId();
            }
            
            throw new BusinessException(ResultCode.FAIL, "登录失败");
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("用户登录异常，用户标识: {}, IP: {}", loginDTO.getUsername(), clientIp, e);
            throw new BusinessException(ResultCode.FAIL, "登录失败，请稍后重试");
        }
    }

    @Override
    public String passwordLogin(PasswordLoginDTO passwordLoginDTO) {
        String clientIp = getClientIpAddress();
        log.info("用户密码登录尝试，用户标识: {}, IP: {}", passwordLoginDTO.getUsername(), clientIp);
        
        try {
            // 查找用户（支持用户名/手机号登录）
            User user = findUserByUsernameOrPhone(passwordLoginDTO.getUsername());
            if (user == null) {
                log.warn("用户密码登录失败，用户不存在: {}, IP: {}", passwordLoginDTO.getUsername(), clientIp);
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "用户名或密码错误");
            }
            
            // 检查用户状态
            if (user.getStatus() == 0) {
                log.warn("用户密码登录失败，账号已被禁用: {}, IP: {}", user.getUuid(), clientIp);
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "账号已被禁用");
            }
            
            // 验证密码
            boolean authSuccess = verifyPassword(passwordLoginDTO.getPassword(), user.getPassword());
            if (!authSuccess) {
                log.warn("用户密码登录失败，密码错误: {}, IP: {}", user.getUuid(), clientIp);
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "用户名或密码错误");
            }
            
            // 设置session并返回
            return createUserSession(user, clientIp);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("用户密码登录异常，用户标识: {}, IP: {}", passwordLoginDTO.getUsername(), clientIp, e);
            throw new BusinessException(ResultCode.FAIL, "登录失败，请稍后重试");
        }
    }

    @Override
    public String smsLogin(SmsLoginDTO smsLoginDTO) {
        String clientIp = getClientIpAddress();
        log.info("用户短信登录尝试，手机号: {}, IP: {}", maskPhone(smsLoginDTO.getPhone()), clientIp);
        
        try {
            // 验证短信验证码
            boolean codeValid = smsService.verifyCode(smsLoginDTO.getPhone(), smsLoginDTO.getCode());
            if (!codeValid) {
                log.warn("用户短信登录失败，验证码错误: {}, IP: {}", maskPhone(smsLoginDTO.getPhone()), clientIp);
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "验证码错误或已过期");
            }
            
            // 查找用户（按手机号查找）
            User user = findUserByUsernameOrPhone(smsLoginDTO.getPhone());
            if (user == null) {
                log.warn("用户短信登录失败，用户不存在: {}, IP: {}", maskPhone(smsLoginDTO.getPhone()), clientIp);
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "该手机号未注册");
            }
            
            // 检查用户状态
            if (user.getStatus() == 0) {
                log.warn("用户短信登录失败，账号已被禁用: {}, IP: {}", user.getUuid(), clientIp);
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "账号已被禁用");
            }
            
            // 设置session并返回
            return createUserSession(user, clientIp);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("用户短信登录异常，手机号: {}, IP: {}", maskPhone(smsLoginDTO.getPhone()), clientIp, e);
            throw new BusinessException(ResultCode.FAIL, "登录失败，请稍后重试");
        }
    }

    @Override
    public UserVO getCurrentUserInfo(Long userId) {
        try {
            User user = userMapper.selectById(userId);
            if (user == null || user.getDeletedTime() != null) {
                throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
            }
            return userConverter.entityToVo(user);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取当前用户信息失败，用户ID: {}", userId, e);
            throw new BusinessException(ResultCode.FAIL, "获取用户信息失败");
        }
    }

    @Override
    public UserVO getUserByUuid(String uuid) {
        log.debug("根据UUID获取用户信息: {}", uuid);
        
        try {
            if (!StringUtils.hasText(uuid)) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "用户UUID不能为空");
            }
            
            User user = findByUuid(uuid);
            if (user == null) {
                throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
            }
            
            return userConverter.entityToVo(user);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("根据UUID获取用户信息失败: {}", uuid, e);
            throw new BusinessException(ResultCode.FAIL, "获取用户信息失败");
        }
    }

    @Override
    public UserVO addUser(UserDTO userDTO) {
        log.info("管理员新增用户，用户名: {}", userDTO.getUsername());
        
        try {
            validateUserDTO(userDTO, true);
            
            // 检查重复
            if (isUsernameExists(userDTO.getUsername())) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "用户名已存在");
            }
            if (StringUtils.hasText(userDTO.getPhone()) && isPhoneExists(userDTO.getPhone())) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "手机号已存在");
            }
            if (StringUtils.hasText(userDTO.getEmail()) && isEmailExists(userDTO.getEmail())) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "邮箱已存在");
            }
            
            User user = userConverter.dtoToEntity(userDTO);
            user.generateUuid();
            user.setPassword(encryptPassword(userDTO.getPassword()));
            
            if (!StringUtils.hasText(user.getRole())) {
                user.setRole("user");
            }
            if (user.getStatus() == null) {
                user.setStatus(1);
            }
            
            userMapper.insert(user);
            log.info("管理员新增用户成功，UUID: {}, 用户名: {}", user.getUuid(), userDTO.getUsername());
            return userConverter.entityToVo(user);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("管理员新增用户失败，用户名: {}", userDTO.getUsername(), e);
            throw new BusinessException(ResultCode.FAIL, "新增用户失败");
        }
    }

    @Override
    public void deleteUserByUuid(String uuid) {
        log.info("删除用户，UUID: {}", uuid);
        
        try {
            if (!StringUtils.hasText(uuid)) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "用户UUID不能为空");
            }
            
            User user = findByUuid(uuid);
            if (user == null) {
                throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
            }
            
            userMapper.deleteById(user.getId()); // MyBatis-Plus自动处理逻辑删除
            log.info("删除用户成功，UUID: {}", uuid);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除用户失败，UUID: {}", uuid, e);
            throw new BusinessException(ResultCode.FAIL, "删除用户失败");
        }
    }

    @Override
    public void deleteUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null || user.getDeletedTime() != null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在或已被删除");
        }
        userMapper.deleteById(id);
        log.info("删除用户成功，用户ID: {}", id);
    }

    @Override
    public UserVO updateUserByUuid(String uuid, UserDTO userDTO) {
        log.info("更新用户信息，UUID: {}", uuid);
        
        try {
            if (!StringUtils.hasText(uuid)) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "用户UUID不能为空");
            }
            
            User user = findByUuid(uuid);
            if (user == null) {
                throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
            }
            
            // 验证更新数据
            validateUserDTO(userDTO, false);
            
            // 检查重复（排除自己）
            if (StringUtils.hasText(userDTO.getPhone()) && !userDTO.getPhone().equals(user.getPhone()) 
                    && isPhoneExists(userDTO.getPhone())) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "手机号已存在");
            }
            if (StringUtils.hasText(userDTO.getEmail()) && !userDTO.getEmail().equals(user.getEmail()) 
                    && isEmailExists(userDTO.getEmail())) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "邮箱已存在");
            }
            
            // 更新允许的字段
            updateUserFields(user, userDTO);
            
            userMapper.updateById(user);
            log.info("更新用户信息成功，UUID: {}", uuid);
            
            return userConverter.entityToVo(userMapper.selectById(user.getId()));
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新用户信息失败，UUID: {}", uuid, e);
            throw new BusinessException(ResultCode.FAIL, "更新用户信息失败");
        }
    }

    @Override
    public UserVO updateUser(Long id, UserDTO userDTO) {
        User user = userMapper.selectById(id);
        if (user == null || user.getDeletedTime() != null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }

        // 更新字段
        updateUserFields(user, userDTO);

        userMapper.updateById(user);
        log.info("更新用户成功，用户ID: {}", id);
        return userConverter.entityToVo(userMapper.selectById(id));
    }

    @Override
    public UserVO getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null || user.getDeletedTime() != null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        return userConverter.entityToVo(user);
    }

    @Override
    public Page<UserVO> listUsers(Page<User> pageRequest, UserDTO userFilter) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        // 查询条件构建
        if (userFilter != null) {
            if (userFilter.getUsername() != null && !userFilter.getUsername().isEmpty()) {
                wrapper.like(User::getUsername, userFilter.getUsername());
            }
            if (userFilter.getPhone() != null && !userFilter.getPhone().isEmpty()) {
                wrapper.like(User::getPhone, userFilter.getPhone());
            }
            if (userFilter.getEmail() != null && !userFilter.getEmail().isEmpty()) {
                wrapper.like(User::getEmail, userFilter.getEmail());
            }
            if (userFilter.getStatus() != null) {
                wrapper.eq(User::getStatus, userFilter.getStatus());
            }
            if (userFilter.getRole() != null && !userFilter.getRole().isEmpty()) {
                wrapper.eq(User::getRole, userFilter.getRole());
            }
        }
        wrapper.orderByAsc(User::getId); // 按ID升序

        Page<User> userPage = userMapper.selectPage(pageRequest, wrapper);

        // 在转换前打印 User 实体的 ID
        if (userPage.getRecords() != null && !userPage.getRecords().isEmpty()) {
            log.info("原始 User 实体列表 (共 {} 条):", userPage.getRecords().size());
            userPage.getRecords().forEach(user -> {
                if (user == null) {
                    log.warn("  - User 实体为 null");
                } else {
                    log.info("  - Username: {}, ID: {}, UUID: {}", user.getUsername(), user.getId(), user.getUuid());
                    if (user.getId() == null) {
                        log.warn("    WARN: User 实体 ID 为 null! Username: {}", user.getUsername());
                    }
                }
            });
        } else {
            log.info("原始 User 实体列表为空或为null。");
        }

        List<UserVO> userVOList = userConverter.entityListToVoList(userPage.getRecords());

        // 在转换后打印 UserVO 的 ID
        if (userVOList != null && !userVOList.isEmpty()) {
            log.info("转换后的 UserVO 列表 (共 {} 条):", userVOList.size());
            userVOList.forEach(userVO -> {
                if (userVO == null) {
                    log.warn("  - UserVO 为 null");
                } else {
                    log.info("  - Username: {}, ID: {}, UUID: {}", userVO.getUsername(), userVO.getId(), userVO.getUuid());
                    if (userVO.getId() == null) {
                        log.warn("    WARN: UserVO ID 为 null! Username: {}", userVO.getUsername());
                    }
                }
            });
        } else {
            log.info("转换后的 UserVO 列表为空或为null。");
        }

        Page<UserVO> userVOPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        userVOPage.setRecords(userVOList);
        return userVOPage;
    }


    @Override
    public User findByUuid(String uuid) {
        if (!StringUtils.hasText(uuid)) {
            return null;
        }
        
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUuid, uuid)
                .isNull(User::getDeletedTime);
        
        return userMapper.selectOne(queryWrapper);
    }

    @Override
    public void changePassword(String uuid, String oldPassword, String newPassword) {
        log.info("用户修改密码，UUID: {}", uuid);
        
        try {
            if (!StringUtils.hasText(uuid) || !StringUtils.hasText(oldPassword) || !StringUtils.hasText(newPassword)) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "参数不能为空");
            }
            
            if (newPassword.length() < 6) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "新密码长度不能少于6位");
            }
            
            User user = findByUuid(uuid);
            if (user == null) {
                throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
            }
            
            // 验证旧密码
            if (!verifyPassword(oldPassword, user.getPassword())) {
                log.warn("用户修改密码失败，旧密码错误: {}", uuid);
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "旧密码错误");
            }
            
            // 更新密码
            user.setPassword(encryptPassword(newPassword));
            userMapper.updateById(user);
            
            log.info("用户修改密码成功，UUID: {}", uuid);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("用户修改密码失败，UUID: {}", uuid, e);
            throw new BusinessException(ResultCode.FAIL, "修改密码失败");
        }
    }
    
    @Override
    public UserVO updateUserStatus(String uuid, Integer status) {
        log.info("修改用户状态，UUID: {}, 新状态: {}", uuid, status);
        
        try {
            if (!StringUtils.hasText(uuid)) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "用户UUID不能为空");
            }
            
            if (status == null || (status != 0 && status != 1)) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "状态值无效，只能是0（禁用）或1（正常）");
            }
            
            User user = findByUuid(uuid);
            if (user == null) {
                throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
            }
            
            // 不能禁用自己
            Long currentUserId = UserContext.getCurrentUserId();
            if (user.getId().equals(currentUserId) && status == 0) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "不能禁用自己的账号");
            }
            
            // 更新状态
            user.setStatus(status);
            userMapper.updateById(user);
            
            // 转换为VO并返回
            UserVO userVO = userConverter.entityToVo(user);
            
            log.info("用户状态修改成功，UUID: {}, 新状态: {}", uuid, status);
            return userVO;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("修改用户状态失败，UUID: {}, 状态: {}", uuid, status, e);
            throw new BusinessException(ResultCode.FAIL, "修改用户状态失败");
        }
    }
    
    // ==================== 私有方法 ====================
    
    /**
     * 验证用户DTO
     */
    private void validateUserDTO(UserDTO userDTO, boolean isCreate) {
        if (userDTO == null) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "用户信息不能为空");
        }
        
        if (isCreate) {
            if (!StringUtils.hasText(userDTO.getUsername())) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "用户名不能为空");
            }
            if (!StringUtils.hasText(userDTO.getPassword())) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "密码不能为空");
            }
        }
        
        // 用户名格式验证
        if (StringUtils.hasText(userDTO.getUsername()) && !USERNAME_PATTERN.matcher(userDTO.getUsername()).matches()) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "用户名格式不正确，只能包含字母、数字、下划线，长度3-20位");
        }
        
        // 密码强度验证
        if (StringUtils.hasText(userDTO.getPassword()) && userDTO.getPassword().length() < 6) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "密码长度不能少于6位");
        }
        
        // 手机号格式验证
        if (StringUtils.hasText(userDTO.getPhone()) && !PHONE_PATTERN.matcher(userDTO.getPhone()).matches()) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "手机号格式不正确");
        }
        
        // 邮箱格式验证
        if (StringUtils.hasText(userDTO.getEmail()) && !EMAIL_PATTERN.matcher(userDTO.getEmail()).matches()) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "邮箱格式不正确");
        }
    }
    
    /**
     * 检查用户名是否存在
     */
    private boolean isUsernameExists(String username) {
        if (!StringUtils.hasText(username)) {
            return false;
        }
        
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username)
                .isNull(User::getDeletedTime);
        
        return userMapper.selectCount(queryWrapper) > 0;
    }
    
    /**
     * 检查手机号是否存在
     */
    private boolean isPhoneExists(String phone) {
        if (!StringUtils.hasText(phone)) {
            return false;
        }
        
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPhone, phone)
                .isNull(User::getDeletedTime);
        
        return userMapper.selectCount(queryWrapper) > 0;
    }
    
    /**
     * 检查邮箱是否存在
     */
    private boolean isEmailExists(String email) {
        if (!StringUtils.hasText(email)) {
            return false;
        }
        
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getEmail, email)
                .isNull(User::getDeletedTime);
        
        return userMapper.selectCount(queryWrapper) > 0;
    }
    
    /**
     * 根据用户名或手机号查找用户
     */
    private User findUserByUsernameOrPhone(String usernameOrPhone) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(wrapper -> wrapper
                .eq(User::getUsername, usernameOrPhone)
                .or()
                .eq(User::getPhone, usernameOrPhone))
                .isNull(User::getDeletedTime);
        
        return userMapper.selectOne(queryWrapper);
    }
    
    /**
     * 更新用户字段
     */
    private void updateUserFields(User user, UserDTO userDTO) {
        // 不允许通过此接口直接修改密码，密码修改有专门接口
        if (StringUtils.hasText(userDTO.getPhone())) user.setPhone(userDTO.getPhone());
        if (StringUtils.hasText(userDTO.getEmail())) user.setEmail(userDTO.getEmail());
        if (StringUtils.hasText(userDTO.getNickname())) user.setNickname(userDTO.getNickname());
        if (StringUtils.hasText(userDTO.getAvatar())) user.setAvatar(userDTO.getAvatar());
        if (userDTO.getGender() != null) user.setGender(userDTO.getGender());
        if (userDTO.getBirthday() != null) user.setBirthday(userDTO.getBirthday());
        if (StringUtils.hasText(userDTO.getRole())) user.setRole(userDTO.getRole());
        if (userDTO.getStatus() != null) user.setStatus(userDTO.getStatus());
        if (StringUtils.hasText(userDTO.getWechatId())) user.setWechatId(userDTO.getWechatId());
        if (StringUtils.hasText(userDTO.getAlipayId())) user.setAlipayId(userDTO.getAlipayId());
    }
    
    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (StringUtils.hasText(xForwardedFor) && !"unknown".equalsIgnoreCase(xForwardedFor)) {
                    return xForwardedFor.split(",")[0].trim();
                }
                
                String xRealIp = request.getHeader("X-Real-IP");
                if (StringUtils.hasText(xRealIp) && !"unknown".equalsIgnoreCase(xRealIp)) {
                    return xRealIp;
                }
                
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.warn("获取客户端IP地址失败", e);
        }
        return "unknown";
    }
    
    /**
     * 手机号脱敏
     */
    private String maskPhone(String phone) {
        if (!StringUtils.hasText(phone) || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
    
    /**
     * 邮箱脱敏
     */
    private String maskEmail(String email) {
        if (!StringUtils.hasText(email) || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        String username = parts[0];
        if (username.length() <= 2) {
            return email;
        }
        return username.substring(0, 2) + "***@" + parts[1];
    }
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /**
     * 加密密码 - 使用BCrypt加密
     */
    private String encryptPassword(String plainPassword) {
        return passwordEncoder.encode(plainPassword);
    }
    
    /**
     * 验证密码 - 使用BCrypt验证
     */
    private boolean verifyPassword(String plainPassword, String encryptedPassword) {
        return passwordEncoder.matches(plainPassword, encryptedPassword);
    }

    private String createUserSession(User user, String clientIp) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpSession session = attributes.getRequest().getSession();
            session.setAttribute("userUuid", user.getUuid());
            session.setAttribute("userId", user.getId()); // 内部使用
            session.setAttribute("username", user.getUsername());
            session.setAttribute("role", user.getRole());
            
            // 立即设置用户上下文，使得登录后立即可以获取当前用户信息
            UserContext.setCurrentUser(user);
            
            log.info("用户登录成功，UUID: {}, 用户名: {}, SessionId: {}, IP: {}", 
                    user.getUuid(), user.getUsername(), session.getId(), clientIp);
            return session.getId();
        }
        throw new BusinessException(ResultCode.FAIL, "登录失败");
    }
}