package com.campus.auth.service;

import com.campus.auth.dto.CaptchaResponse;
import com.campus.common.cache.CacheKeys;
import com.campus.common.exception.BusinessException;
import com.campus.common.result.AuthErrorCode;
import com.campus.config.AuthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CaptchaService {

    private static final Duration CAPTCHA_TTL = Duration.ofMinutes(2);

    private final StringRedisTemplate stringRedisTemplate;
    private final AuthProperties authProperties;

    public CaptchaResponse generate() {
        if (!authProperties.isCaptchaEnabled()) {
            return new CaptchaResponse(null, null, false);
        }
        SecureRandom random = new SecureRandom();
        int a = 1 + random.nextInt(9);
        int b = 1 + random.nextInt(9);
        boolean plus = random.nextBoolean();
        if (!plus && a < b) {
            int temp = a;
            a = b;
            b = temp;
        }
        int answer = plus ? a + b : a - b;
        String expression = a + (plus ? " + " : " - ") + b + " = ?";

        BufferedImage image = new BufferedImage(120, 40, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(new Color(245, 247, 250));
        graphics.fillRect(0, 0, 120, 40);
        graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        graphics.setColor(new Color(30, 41, 59));
        graphics.drawString(expression, 10, 29);
        graphics.setStroke(new BasicStroke(1.2f));
        for (int i = 0; i < 6; i++) {
            graphics.setColor(new Color(120 + random.nextInt(100),
                    120 + random.nextInt(100), 120 + random.nextInt(100)));
            graphics.drawLine(random.nextInt(120), random.nextInt(40),
                    random.nextInt(120), random.nextInt(40));
        }
        graphics.dispose();

        String captchaId = UUID.randomUUID().toString().replace("-", "");
        stringRedisTemplate.opsForValue()
                .set(CacheKeys.captcha(captchaId), String.valueOf(answer), CAPTCHA_TTL);

        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", output);
            String base64 = Base64.getEncoder().encodeToString(output.toByteArray());
            return new CaptchaResponse(captchaId, base64, true);
        } catch (Exception ex) {
            throw new BusinessException(AuthErrorCode.CAPTCHA_INVALID, "验证码生成失败");
        }
    }

    public void validateIfEnabled(String captchaId, String captchaCode) {
        if (!authProperties.isCaptchaEnabled()) {
            return;
        }
        if (captchaId == null || captchaId.isBlank() || captchaCode == null || captchaCode.isBlank()) {
            throw new BusinessException(AuthErrorCode.CAPTCHA_INVALID);
        }
        String answer = stringRedisTemplate.opsForValue().getAndDelete(CacheKeys.captcha(captchaId));
        if (answer == null || !answer.equals(captchaCode.trim())) {
            throw new BusinessException(AuthErrorCode.CAPTCHA_INVALID);
        }
    }
}
