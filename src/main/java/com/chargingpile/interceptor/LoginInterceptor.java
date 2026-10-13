package com.chargingpile.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("userId") != null) {
            return true;
        }
        // 未登录：API 返回 JSON，页面重定向到登录页
        if (request.getRequestURI().startsWith("/api/")) {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录，请先登录\"}");
            return false;
        }
        response.sendRedirect("/login.html");
        return false;
    }
}
