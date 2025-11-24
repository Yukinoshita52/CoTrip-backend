package com.trip.web.security;

import com.trip.common.exception.GlobalExceptionHandler;
import com.trip.common.login.LoginUser;
import com.trip.common.login.LoginUserHolder;
import com.trip.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String header = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
                String token = header.substring(7);

                try {
                    Claims claims = JwtUtil.parseToken(token);
                    Long userId = claims.get("userId", Long.class);
                    String username = claims.get("username", String.class);

                    LoginUser loginUser = new LoginUser(userId, username);

                    // 同时设置到SecurityContext和ThreadLocal中
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    // 设置到ThreadLocal，供Controller使用
                    LoginUserHolder.setLoginUser(loginUser);

                } catch (Exception e) {
                    // 捕获所有JWT解析异常，并记录到request中
                    log.error("JWT解析失败：{}", e.getMessage(), e);
                    request.setAttribute("jwt.error", e);
                }
            }

            filterChain.doFilter(request, response);
        } finally {
            // 请求完成后清理ThreadLocal，防止内存泄漏
            LoginUserHolder.clear();
        }
    }
}
