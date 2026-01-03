package com.med.config;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenStore {

    // 存储token和过期时间
    private final Map<String, TokenInfo> tokenMap = new ConcurrentHashMap<>();

    // 默认过期时间：48小时
    private static final int DEFAULT_EXPIRE_HOURS = 48;

    /**
     * 生成并存储token
     */
    public String generateToken(String username) {
        String token = "token_" + System.currentTimeMillis() + "_" + username.hashCode();
        LocalDateTime expireTime = LocalDateTime.now().plusHours(DEFAULT_EXPIRE_HOURS);
        
        TokenInfo tokenInfo = new TokenInfo();
        tokenInfo.setUsername(username);
        tokenInfo.setExpireTime(expireTime);
        
        tokenMap.put(token, tokenInfo);
        
        // 清理过期token
        cleanExpiredTokens();
        
        return token;
    }

    /**
     * 验证token是否有效
     */
    public boolean isValidToken(String token) {
        if (token == null || !token.startsWith("token_")) {
            return false;
        }
        
        TokenInfo tokenInfo = tokenMap.get(token);
        if (tokenInfo == null) {
            return false;
        }
        
        // 检查是否过期
        if (LocalDateTime.now().isAfter(tokenInfo.getExpireTime())) {
            tokenMap.remove(token);
            return false;
        }
        
        return true;
    }

    /**
     * 刷新token的过期时间
     */
    public void refreshToken(String token) {
        if (token == null || !token.startsWith("token_")) {
            return;
        }
        
        TokenInfo tokenInfo = tokenMap.get(token);
        if (tokenInfo != null) {
            // 重新设置过期时间为48小时后
            tokenInfo.setExpireTime(LocalDateTime.now().plusHours(DEFAULT_EXPIRE_HOURS));
        }
    }

    /**
     * 获取token对应的用户名
     */
    public String getUsername(String token) {
        TokenInfo tokenInfo = tokenMap.get(token);
        return tokenInfo != null ? tokenInfo.getUsername() : null;
    }

    /**
     * 删除token（登出时使用）
     */
    public void removeToken(String token) {
        tokenMap.remove(token);
    }

    /**
     * 清理过期的token
     */
    private void cleanExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        tokenMap.entrySet().removeIf(entry -> now.isAfter(entry.getValue().getExpireTime()));
    }

    @Data
    static class TokenInfo {
        private String username;
        private LocalDateTime expireTime;
    }
}

