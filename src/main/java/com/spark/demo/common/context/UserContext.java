package com.spark.demo.common.context;

import com.spark.demo.entity.User;

/**
 * 用户上下文管理器
 * @author spark
 * @date 2025-05-29
 */
public class UserContext {
    private static final ThreadLocal<User> USER_HOLDER = new ThreadLocal<>();
    
    /**
     * 设置当前用户
     */
    public static void setCurrentUser(User user) {
        USER_HOLDER.set(user);
    }
    
    /**
     * 获取当前用户
     */
    public static User getCurrentUser() {
        return USER_HOLDER.get();
    }
    
    /**
     * 获取当前用户ID
     */
    public static Long getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId() : null;
    }
    
    /**
     * 获取当前用户角色
     */
    public static String getCurrentUserRole() {
        User user = getCurrentUser();
        return user != null ? user.getRole() : null;
    }
    
    /**
     * 检查当前用户是否有指定角色
     */
    public static boolean hasRole(String role) {
        String currentRole = getCurrentUserRole();
        return currentRole != null && currentRole.equals(role);
    }
    
    /**
     * 检查当前用户是否有任意指定角色
     */
    public static boolean hasAnyRole(String... roles) {
        String currentRole = getCurrentUserRole();
        if (currentRole == null) {
            return false;
        }
        for (String role : roles) {
            if (currentRole.equals(role)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 清除当前用户
     */
    public static void clear() {
        USER_HOLDER.remove();
    }
} 