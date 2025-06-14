package com.spark.demo.common.util;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * API文档工具类
 * 提供通用的API文档注解和示例
 * 
 * @author spark
 * @date 2025-01-27
 */
public class ApiDocUtil {

    /**
     * 标准成功响应注解
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "操作成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/Result"),
                examples = @ExampleObject(
                    name = "成功示例",
                    value = """
                        {
                          "code": 200,
                          "msg": "操作成功",
                          "data": null,
                          "timestamp": "2025-01-27T10:30:00"
                        }
                        """
                )
            )
        )
    })
    public @interface StandardSuccessResponse {}

    /**
     * 标准错误响应注解
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponses({
        @ApiResponse(
            responseCode = "400",
            description = "请求参数错误",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/Result"),
                examples = @ExampleObject(
                    name = "参数错误示例",
                    value = """
                        {
                          "code": 400,
                          "msg": "参数校验失败: username: 用户名不能为空",
                          "data": null,
                          "timestamp": "2025-01-27T10:30:00"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "未授权访问",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/Result"),
                examples = @ExampleObject(
                    name = "未授权示例",
                    value = """
                        {
                          "code": 401,
                          "msg": "用户未登录",
                          "data": null,
                          "timestamp": "2025-01-27T10:30:00"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "权限不足",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/Result"),
                examples = @ExampleObject(
                    name = "权限不足示例",
                    value = """
                        {
                          "code": 403,
                          "msg": "权限不足，需要admin角色",
                          "data": null,
                          "timestamp": "2025-01-27T10:30:00"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "429",
            description = "请求过于频繁",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/Result"),
                examples = @ExampleObject(
                    name = "限流示例",
                    value = """
                        {
                          "code": 429,
                          "msg": "访问过于频繁，请稍后再试",
                          "data": null,
                          "timestamp": "2025-01-27T10:30:00"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "服务器内部错误",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/Result"),
                examples = @ExampleObject(
                    name = "服务器错误示例",
                    value = """
                        {
                          "code": 500,
                          "msg": "系统内部错误，错误ID: A1B2C3D4",
                          "data": null,
                          "timestamp": "2025-01-27T10:30:00"
                        }
                        """
                )
            )
        )
    })
    public @interface StandardErrorResponses {}

    /**
     * 分页响应注解
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponse(
        responseCode = "200",
        description = "分页查询成功",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(ref = "#/components/schemas/PageResult"),
            examples = @ExampleObject(
                name = "分页查询示例",
                value = """
                    {
                      "code": 200,
                      "msg": "操作成功",
                      "data": {
                        "records": [
                          {
                            "id": 1,
                            "uuid": "user-uuid-1234",
                            "username": "testuser",
                            "phone": "138****8000",
                            "email": "test****@example.com",
                            "status": 1,
                            "role": "user",
                            "createdTime": "2025-01-27T10:30:00"
                          }
                        ],
                        "total": 100,
                        "size": 10,
                        "current": 1,
                        "pages": 10
                      },
                      "timestamp": "2025-01-27T10:30:00"
                    }
                    """
            )
        )
    )
    public @interface PageResponse {}

    /**
     * 登录响应注解
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponse(
        responseCode = "200",
        description = "登录成功",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(ref = "#/components/schemas/Result"),
            examples = @ExampleObject(
                name = "登录成功示例",
                value = """
                    {
                      "code": 200,
                      "msg": "登录成功",
                      "data": "12345678-1234-1234-1234-123456789012",
                      "timestamp": "2025-01-27T10:30:00"
                    }
                    """
            )
        )
    )
    public @interface LoginResponse {}

    /**
     * 用户信息响应注解
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponse(
        responseCode = "200",
        description = "获取用户信息成功",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(ref = "#/components/schemas/Result"),
            examples = @ExampleObject(
                name = "用户信息示例",
                value = """
                    {
                      "code": 200,
                      "msg": "操作成功",
                      "data": {
                        "uuid": "user-uuid-1234-5678-9012",
                        "username": "testuser",
                        "phone": "138****8000",
                        "email": "test****@example.com",
                        "nickname": "测试用户",
                        "avatar": "http://example.com/avatar.jpg",
                        "gender": 1,
                        "birthday": "2000-01-01",
                        "role": "user",
                        "status": 1,
                        "createdTime": "2025-01-27T10:30:00",
                        "updatedTime": "2025-01-27T10:30:00"
                      },
                      "timestamp": "2025-01-27T10:30:00"
                    }
                    """
            )
        )
    )
    public @interface UserInfoResponse {}

    /**
     * 完整的API响应注解（包含成功和错误）
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "操作成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/Result"),
                examples = @ExampleObject(
                    name = "成功示例",
                    value = """
                        {
                          "code": 200,
                          "msg": "操作成功",
                          "data": null,
                          "timestamp": "2025-01-27T10:30:00"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "请求参数错误",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/Result"),
                examples = @ExampleObject(
                    name = "参数错误示例",
                    value = """
                        {
                          "code": 400,
                          "msg": "参数校验失败: username: 用户名不能为空",
                          "data": null,
                          "timestamp": "2025-01-27T10:30:00"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "未授权访问",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/Result"),
                examples = @ExampleObject(
                    name = "未授权示例",
                    value = """
                        {
                          "code": 401,
                          "msg": "用户未登录",
                          "data": null,
                          "timestamp": "2025-01-27T10:30:00"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "权限不足",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/Result"),
                examples = @ExampleObject(
                    name = "权限不足示例",
                    value = """
                        {
                          "code": 403,
                          "msg": "权限不足，需要admin角色",
                          "data": null,
                          "timestamp": "2025-01-27T10:30:00"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "429",
            description = "请求过于频繁",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/Result"),
                examples = @ExampleObject(
                    name = "限流示例",
                    value = """
                        {
                          "code": 429,
                          "msg": "访问过于频繁，请稍后再试",
                          "data": null,
                          "timestamp": "2025-01-27T10:30:00"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "服务器内部错误",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/Result"),
                examples = @ExampleObject(
                    name = "服务器错误示例",
                    value = """
                        {
                          "code": 500,
                          "msg": "系统内部错误，错误ID: A1B2C3D4",
                          "data": null,
                          "timestamp": "2025-01-27T10:30:00"
                        }
                        """
                )
            )
        )
    })
    public @interface CompleteApiResponses {}

    /**
     * 认证相关API响应注解
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "登录成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/Result"),
                examples = @ExampleObject(
                    name = "登录成功示例",
                    value = """
                        {
                          "code": 200,
                          "msg": "登录成功",
                          "data": "12345678-1234-1234-1234-123456789012",
                          "timestamp": "2025-01-27T10:30:00"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "请求参数错误",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/Result"),
                examples = @ExampleObject(
                    name = "参数错误示例",
                    value = """
                        {
                          "code": 400,
                          "msg": "用户名或密码错误",
                          "data": null,
                          "timestamp": "2025-01-27T10:30:00"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "429",
            description = "请求过于频繁",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/Result"),
                examples = @ExampleObject(
                    name = "限流示例",
                    value = """
                        {
                          "code": 429,
                          "msg": "登录尝试过于频繁，请稍后再试",
                          "data": null,
                          "timestamp": "2025-01-27T10:30:00"
                        }
                        """
                )
            )
        )
    })
    public @interface AuthApiResponses {}

    /**
     * 用户管理API响应注解
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "获取用户信息成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/Result"),
                examples = @ExampleObject(
                    name = "用户信息示例",
                    value = """
                        {
                          "code": 200,
                          "msg": "操作成功",
                          "data": {
                            "uuid": "user-uuid-1234-5678-9012",
                            "username": "testuser",
                            "phone": "138****8000",
                            "email": "test****@example.com",
                            "nickname": "测试用户",
                            "avatar": "http://example.com/avatar.jpg",
                            "gender": 1,
                            "birthday": "2000-01-01",
                            "role": "user",
                            "status": 1,
                            "createdTime": "2025-01-27T10:30:00",
                            "updatedTime": "2025-01-27T10:30:00"
                          },
                          "timestamp": "2025-01-27T10:30:00"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "未授权访问",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/Result"),
                examples = @ExampleObject(
                    name = "未授权示例",
                    value = """
                        {
                          "code": 401,
                          "msg": "用户未登录",
                          "data": null,
                          "timestamp": "2025-01-27T10:30:00"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "权限不足",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/Result"),
                examples = @ExampleObject(
                    name = "权限不足示例",
                    value = """
                        {
                          "code": 403,
                          "msg": "权限不足，只能查看自己的信息",
                          "data": null,
                          "timestamp": "2025-01-27T10:30:00"
                        }
                        """
                )
            )
        )
    })
    public @interface UserApiResponses {}

    /**
     * 分页查询API响应注解
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "分页查询成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/PageResult"),
                examples = @ExampleObject(
                    name = "分页查询示例",
                    value = """
                        {
                          "code": 200,
                          "msg": "操作成功",
                          "data": {
                            "records": [
                              {
                                "id": 1,
                                "uuid": "user-uuid-1234",
                                "username": "testuser",
                                "phone": "138****8000",
                                "email": "test****@example.com",
                                "status": 1,
                                "role": "user",
                                "createdTime": "2025-01-27T10:30:00"
                              }
                            ],
                            "total": 100,
                            "size": 10,
                            "current": 1,
                            "pages": 10
                          },
                          "timestamp": "2025-01-27T10:30:00"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "未授权访问",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/Result"),
                examples = @ExampleObject(
                    name = "未授权示例",
                    value = """
                        {
                          "code": 401,
                          "msg": "用户未登录",
                          "data": null,
                          "timestamp": "2025-01-27T10:30:00"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "权限不足",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/Result"),
                examples = @ExampleObject(
                    name = "权限不足示例",
                    value = """
                        {
                          "code": 403,
                          "msg": "权限不足，需要admin角色",
                          "data": null,
                          "timestamp": "2025-01-27T10:30:00"
                        }
                        """
                )
            )
        )
    })
    public @interface PageApiResponses {}

    /**
     * 常用示例数据
     */
    public static class Examples {
        public static final String USER_UUID = "a1b2c3d4-e5f6-7890-1234-567890abcdef";
        public static final String SESSION_ID = "12345678-1234-1234-1234-123456789012";
        public static final String PHONE_NUMBER = "13800138000";
        public static final String EMAIL = "testuser@example.com";
        public static final String USERNAME = "testuser";
        public static final String PASSWORD = "123456";
        public static final String SMS_CODE = "123456";
        
        public static final String LOGIN_REQUEST = """
            {
              "username": "testuser",
              "password": "123456"
            }
            """;
            
        public static final String REGISTER_REQUEST = """
            {
              "username": "newuser",
              "password": "123456",
              "phone": "13800138001",
              "email": "newuser@example.com",
              "nickname": "新用户"
            }
            """;
            
        public static final String SMS_LOGIN_REQUEST = """
            {
              "phone": "13800138000",
              "code": "123456"
            }
            """;
    }
} 