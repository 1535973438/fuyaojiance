package com.med.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final TokenStore tokenStore;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 允许OPTIONS请求（CORS预检）
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        // 允许登录接口访问
        String path = request.getRequestURI();
        if (path.startsWith("/api/auth/login")) {
            return true;
        }

        // 检查token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未授权，请先登录\"}");
            return false;
        }

        // 验证token是否有效（包括过期时间检查）
        String token = authHeader.substring(7);
        if (!tokenStore.isValidToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"token无效或已过期\"}");
            return false;
        }

        // token有效时，刷新过期时间
        tokenStore.refreshToken(token);

        return true;
    }
}

