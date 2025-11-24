package com.trip.web.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trip.common.result.Result;
import com.trip.common.result.ResultCodeEnum;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class UnauthorizedEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Logger log = LoggerFactory.getLogger(UnauthorizedEntryPoint.class);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");

        // 检查是否有 JWT 解析错误
        Object jwtError = request.getAttribute("jwt.error");
        if (jwtError instanceof Exception e) {
            log.error("JWT 解析失败：{}", e.getMessage(), e);
        }

        Result<String> body = Result.fail(ResultCodeEnum.APP_LOGIN_AUTH.getCode(), "Token 无效或已过期，请重新登录");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
