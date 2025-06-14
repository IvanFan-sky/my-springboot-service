package com.spark.demo.common.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * IP工具类
 * 用于获取客户端真实IP地址
 * 
 * @author spark
 */
@Slf4j
public class IpUtil {

    private static final String UNKNOWN = "unknown";
    private static final String LOCALHOST_IPV4 = "127.0.0.1";
    private static final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";
    private static final int IP_MAX_LENGTH = 15;

    /**
     * 获取客户端真实IP地址
     * 
     * @param request HTTP请求
     * @return 客户端IP地址
     */
    public static String getClientIP(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }

        String ip = null;
        
        // 1. 检查X-Forwarded-For头（代理服务器传递的原始客户端IP）
        ip = request.getHeader("X-Forwarded-For");
        if (isValidIP(ip)) {
            // X-Forwarded-For可能包含多个IP，取第一个
            int index = ip.indexOf(',');
            if (index != -1) {
                ip = ip.substring(0, index);
            }
            return ip.trim();
        }

        // 2. 检查X-Real-IP头（Nginx等反向代理设置的真实IP）
        ip = request.getHeader("X-Real-IP");
        if (isValidIP(ip)) {
            return ip;
        }

        // 3. 检查Proxy-Client-IP头（Apache服务器）
        ip = request.getHeader("Proxy-Client-IP");
        if (isValidIP(ip)) {
            return ip;
        }

        // 4. 检查WL-Proxy-Client-IP头（WebLogic服务器）
        ip = request.getHeader("WL-Proxy-Client-IP");
        if (isValidIP(ip)) {
            return ip;
        }

        // 5. 检查HTTP_CLIENT_IP头
        ip = request.getHeader("HTTP_CLIENT_IP");
        if (isValidIP(ip)) {
            return ip;
        }

        // 6. 检查HTTP_X_FORWARDED_FOR头
        ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        if (isValidIP(ip)) {
            return ip;
        }

        // 7. 最后使用request.getRemoteAddr()
        ip = request.getRemoteAddr();
        
        // 处理本地回环地址
        if (LOCALHOST_IPV6.equals(ip)) {
            ip = LOCALHOST_IPV4;
        }

        log.debug("获取到客户端IP: {}", ip);
        return ip;
    }

    /**
     * 验证IP地址是否有效
     * 
     * @param ip IP地址
     * @return 是否有效
     */
    private static boolean isValidIP(String ip) {
        return StringUtils.hasText(ip) 
                && !UNKNOWN.equalsIgnoreCase(ip) 
                && ip.length() <= IP_MAX_LENGTH;
    }

    /**
     * 判断是否为内网IP
     * 
     * @param ip IP地址
     * @return 是否为内网IP
     */
    public static boolean isInternalIP(String ip) {
        if (!StringUtils.hasText(ip)) {
            return false;
        }

        try {
            String[] parts = ip.split("\\.");
            if (parts.length != 4) {
                return false;
            }

            int firstOctet = Integer.parseInt(parts[0]);
            int secondOctet = Integer.parseInt(parts[1]);

            // 10.0.0.0 - 10.255.255.255
            if (firstOctet == 10) {
                return true;
            }

            // 172.16.0.0 - 172.31.255.255
            if (firstOctet == 172 && secondOctet >= 16 && secondOctet <= 31) {
                return true;
            }

            // 192.168.0.0 - 192.168.255.255
            if (firstOctet == 192 && secondOctet == 168) {
                return true;
            }

            // 127.0.0.0 - 127.255.255.255 (回环地址)
            if (firstOctet == 127) {
                return true;
            }

            return false;
        } catch (NumberFormatException e) {
            log.warn("解析IP地址失败: {}", ip, e);
            return false;
        }
    }

    /**
     * 获取IP地址的地理位置信息（占位方法）
     * 
     * @param ip IP地址
     * @return 地理位置信息
     */
    public static String getIPLocation(String ip) {
        // 这里可以集成第三方IP地址库，如GeoIP2等
        if (isInternalIP(ip)) {
            return "内网IP";
        }
        return "未知位置";
    }
} 