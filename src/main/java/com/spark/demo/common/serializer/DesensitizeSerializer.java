package com.spark.demo.common.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.spark.demo.common.annotation.Desensitize;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Objects;

/**
 * 数据脱敏序列化器
 * 根据注解配置对敏感数据进行脱敏处理
 * 
 * @author spark
 */
@NoArgsConstructor
@AllArgsConstructor
public class DesensitizeSerializer extends JsonSerializer<String> implements ContextualSerializer {

    private Desensitize.DesensitizeType type;
    private int startLen;
    private int endLen;
    private String replacement;

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (!StringUtils.hasText(value)) {
            gen.writeString(value);
            return;
        }

        String desensitizedValue = desensitize(value);
        gen.writeString(desensitizedValue);
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        if (property != null) {
            Desensitize desensitize = property.getAnnotation(Desensitize.class);
            if (desensitize != null) {
                return new DesensitizeSerializer(
                        desensitize.type(),
                        desensitize.startLen(),
                        desensitize.endLen(),
                        desensitize.replacement()
                );
            }
        }
        return this;
    }

    /**
     * 执行脱敏处理
     */
    private String desensitize(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }

        switch (type) {
            case PHONE:
                return desensitizePhone(value);
            case ID_CARD:
                return desensitizeIdCard(value);
            case EMAIL:
                return desensitizeEmail(value);
            case NAME:
                return desensitizeName(value);
            case ADDRESS:
                return desensitizeAddress(value);
            case BANK_CARD:
                return desensitizeBankCard(value);
            case PASSWORD:
                return desensitizePassword(value);
            case CUSTOM:
            default:
                return desensitizeCustom(value);
        }
    }

    /**
     * 手机号脱敏：138****1234
     */
    private String desensitizePhone(String phone) {
        if (phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    /**
     * 身份证脱敏：1234***********5678
     */
    private String desensitizeIdCard(String idCard) {
        if (idCard.length() < 8) {
            return idCard;
        }
        return idCard.substring(0, 4) + "***********" + idCard.substring(idCard.length() - 4);
    }

    /**
     * 邮箱脱敏：abc***@example.com
     */
    private String desensitizeEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            return email;
        }
        
        String username = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        
        if (username.length() <= 3) {
            return username.charAt(0) + "***" + domain;
        } else {
            return username.substring(0, 3) + "***" + domain;
        }
    }

    /**
     * 姓名脱敏：张*
     */
    private String desensitizeName(String name) {
        if (name.length() <= 1) {
            return name;
        }
        return name.charAt(0) + "*".repeat(name.length() - 1);
    }

    /**
     * 地址脱敏：北京市***
     */
    private String desensitizeAddress(String address) {
        if (address.length() <= 6) {
            return address.substring(0, 1) + "***";
        }
        return address.substring(0, 6) + "***";
    }

    /**
     * 银行卡脱敏：1234 **** **** 5678
     */
    private String desensitizeBankCard(String bankCard) {
        if (bankCard.length() < 8) {
            return bankCard;
        }
        
        String cleaned = bankCard.replaceAll("\\s", "");
        if (cleaned.length() < 8) {
            return bankCard;
        }
        
        String prefix = cleaned.substring(0, 4);
        String suffix = cleaned.substring(cleaned.length() - 4);
        int middleLen = cleaned.length() - 8;
        String middle = "*".repeat(Math.max(4, middleLen));
        
        return prefix + " " + middle + " " + suffix;
    }

    /**
     * 密码脱敏：******
     */
    private String desensitizePassword(String password) {
        return "******";
    }

    /**
     * 自定义脱敏
     */
    private String desensitizeCustom(String value) {
        int length = value.length();
        int start = Math.min(startLen, length);
        int end = Math.min(endLen, length - start);
        
        if (start + end >= length) {
            return value;
        }
        
        String prefix = value.substring(0, start);
        String suffix = value.substring(length - end);
        String middle = replacement.repeat(length - start - end);
        
        return prefix + middle + suffix;
    }
} 