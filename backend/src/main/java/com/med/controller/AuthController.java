package com.med.controller;

import com.med.common.Result;
import com.med.config.TokenStore;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    @Value("${auth.username}")
    private String username;

    @Value("${auth.password}")
    private String password;

    private final TokenStore tokenStore;

    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody LoginRequest request) {
        if (username.equals(request.getUsername()) && password.equals(request.getPassword())) {
            Map<String, String> data = new HashMap<>();
            // 生成token并存储，默认48小时过期
            String token = tokenStore.generateToken(request.getUsername());
            data.put("token", token);
            data.put("username", request.getUsername());
            return Result.success(data);
        } else {
            return Result.error(401, "用户名或密码错误");
        }
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            tokenStore.removeToken(token);
        }
        return Result.success();
    }

    @Data
    static class LoginRequest {
        private String username;
        private String password;
    }
}

