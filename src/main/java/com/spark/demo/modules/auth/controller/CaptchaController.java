package com.spark.demo.modules.auth.controller;

import com.spark.demo.common.result.Result;
import com.spark.demo.modules.auth.service.CaptchaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 滑动验证码控制器
 */
@Slf4j
@RestController
@RequestMapping("/v1/captcha")
@Tag(name = "滑动验证码", description = "滑动验证码相关接口")
public class CaptchaController {

    private final CaptchaService captchaService;

    public CaptchaController(CaptchaService captchaService) {
        this.captchaService = captchaService;
    }

    /**
     * 生成滑动验证码
     */
    @GetMapping("/generate")
    @Operation(summary = "生成滑动验证码", description = "生成滑动验证码图片和相关信息")
    public Result<Map<String, Object>> generateCaptcha() {
        log.info("请求生成滑动验证码");
        return captchaService.generateCaptcha();
    }

    /**
     * 验证滑动验证码
     */
    @PostMapping("/verify")
    @Operation(summary = "验证滑动验证码", description = "验证用户滑动的位置是否正确")
    public Result<Boolean> verifyCaptcha(
            @Parameter(description = "验证码ID") @RequestParam String captchaId,
            @Parameter(description = "用户滑动的X坐标") @RequestParam int sliderX) {
        log.info("验证滑动验证码，ID: {}, 滑动位置: {}", captchaId, sliderX);
        return captchaService.verifyCaptcha(captchaId, sliderX);
    }
} 