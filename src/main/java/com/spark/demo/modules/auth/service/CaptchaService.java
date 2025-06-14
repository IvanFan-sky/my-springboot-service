package com.spark.demo.modules.auth.service;

import com.spark.demo.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 滑动验证码服务
 */
@Slf4j
@Service
public class CaptchaService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SecureRandom random = new SecureRandom();
    
    // 验证码图片尺寸
    private static final int CAPTCHA_WIDTH = 300;
    private static final int CAPTCHA_HEIGHT = 150;
    private static final int SLIDER_WIDTH = 60;
    private static final int SLIDER_HEIGHT = 60;
    
    public CaptchaService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 生成滑动验证码
     */
    public Result<Map<String, Object>> generateCaptcha() {
        try {
            String captchaId = UUID.randomUUID().toString();
            
            // 生成随机滑块位置
            int sliderX = random.nextInt(CAPTCHA_WIDTH - SLIDER_WIDTH - 50) + 50;
            int sliderY = random.nextInt(CAPTCHA_HEIGHT - SLIDER_HEIGHT - 20) + 20;
            
            // 生成背景图片
            BufferedImage backgroundImage = generateBackgroundImage();
            
            // 生成滑块图片和带缺口的背景图片
            BufferedImage sliderImage = new BufferedImage(SLIDER_WIDTH, SLIDER_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            BufferedImage puzzleImage = new BufferedImage(CAPTCHA_WIDTH, CAPTCHA_HEIGHT, BufferedImage.TYPE_INT_RGB);
            
            Graphics2D puzzleG2d = puzzleImage.createGraphics();
            puzzleG2d.drawImage(backgroundImage, 0, 0, null);
            
            // 创建滑块形状（简单矩形）
            Graphics2D sliderG2d = sliderImage.createGraphics();
            sliderG2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // 从背景图片中提取滑块区域
            for (int x = 0; x < SLIDER_WIDTH; x++) {
                for (int y = 0; y < SLIDER_HEIGHT; y++) {
                    int rgb = backgroundImage.getRGB(sliderX + x, sliderY + y);
                    sliderImage.setRGB(x, y, rgb);
                }
            }
            
            // 在背景图片上创建缺口
            puzzleG2d.setColor(Color.GRAY);
            puzzleG2d.fillRect(sliderX, sliderY, SLIDER_WIDTH, SLIDER_HEIGHT);
            puzzleG2d.setColor(Color.DARK_GRAY);
            puzzleG2d.drawRect(sliderX, sliderY, SLIDER_WIDTH, SLIDER_HEIGHT);
            
            puzzleG2d.dispose();
            sliderG2d.dispose();
            
            // 转换为Base64
            String backgroundBase64 = imageToBase64(puzzleImage);
            String sliderBase64 = imageToBase64(sliderImage);
            
            // 保存验证码信息到Redis
            Map<String, Object> captchaInfo = new HashMap<>();
            captchaInfo.put("sliderX", sliderX);
            captchaInfo.put("sliderY", sliderY);
            captchaInfo.put("createTime", System.currentTimeMillis());
            
            redisTemplate.opsForValue().set("captcha:" + captchaId, captchaInfo, 5, TimeUnit.MINUTES);
            
            // 返回给前端的数据
            Map<String, Object> result = new HashMap<>();
            result.put("captchaId", captchaId);
            result.put("backgroundImage", "data:image/png;base64," + backgroundBase64);
            result.put("sliderImage", "data:image/png;base64," + sliderBase64);
            result.put("sliderY", sliderY);
            
            log.info("生成滑动验证码成功，ID: {}, 滑块位置: ({}, {})", captchaId, sliderX, sliderY);
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("生成滑动验证码失败", e);
            return Result.fail("生成验证码失败");
        }
    }

    /**
     * 验证滑动验证码
     */
    public Result<Boolean> verifyCaptcha(String captchaId, int userSliderX) {
        try {
            String key = "captcha:" + captchaId;
            Map<String, Object> captchaInfo = (Map<String, Object>) redisTemplate.opsForValue().get(key);
            
            if (captchaInfo == null) {
                return Result.fail("验证码已过期或不存在");
            }
            
            // 删除验证码（一次性使用）
            redisTemplate.delete(key);
            
            Integer correctSliderX = (Integer) captchaInfo.get("sliderX");
            Long createTime = (Long) captchaInfo.get("createTime");
            
            // 检查时间是否过期（5分钟）
            if (System.currentTimeMillis() - createTime > 5 * 60 * 1000) {
                return Result.fail("验证码已过期");
            }
            
            // 允许5像素的误差
            int tolerance = 5;
            boolean isValid = Math.abs(userSliderX - correctSliderX) <= tolerance;
            
            log.info("验证滑动验证码，ID: {}, 正确位置: {}, 用户位置: {}, 验证结果: {}", 
                    captchaId, correctSliderX, userSliderX, isValid);
            
            if (isValid) {
                return Result.success(true);
            } else {
                return Result.fail("验证失败，请重试");
            }
            
        } catch (Exception e) {
            log.error("验证滑动验证码失败", e);
            return Result.fail("验证失败");
        }
    }

    /**
     * 生成背景图片
     */
    private BufferedImage generateBackgroundImage() {
        BufferedImage image = new BufferedImage(CAPTCHA_WIDTH, CAPTCHA_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        
        // 设置渐变背景
        GradientPaint gradient = new GradientPaint(0, 0, new Color(135, 206, 250), 
                CAPTCHA_WIDTH, CAPTCHA_HEIGHT, new Color(70, 130, 180));
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, CAPTCHA_WIDTH, CAPTCHA_HEIGHT);
        
        // 添加一些干扰线
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2));
        for (int i = 0; i < 5; i++) {
            int x1 = random.nextInt(CAPTCHA_WIDTH);
            int y1 = random.nextInt(CAPTCHA_HEIGHT);
            int x2 = random.nextInt(CAPTCHA_WIDTH);
            int y2 = random.nextInt(CAPTCHA_HEIGHT);
            g2d.drawLine(x1, y1, x2, y2);
        }
        
        // 添加一些圆点
        for (int i = 0; i < 20; i++) {
            int x = random.nextInt(CAPTCHA_WIDTH);
            int y = random.nextInt(CAPTCHA_HEIGHT);
            int size = random.nextInt(5) + 2;
            g2d.fillOval(x, y, size, size);
        }
        
        g2d.dispose();
        return image;
    }

    /**
     * 将图片转换为Base64字符串
     */
    private String imageToBase64(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.getEncoder().encodeToString(imageBytes);
    }
} 