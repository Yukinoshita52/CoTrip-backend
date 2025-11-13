package com.trip.web.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trip.common.result.Result;
import com.trip.common.result.ResultCodeEnum;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class UnauthorizedEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");

        // 检查是否有JWT解析错误
        Object jwtError = request.getAttribute("jwt.error");
        String errorMessage = ResultCodeEnum.APP_LOGIN_AUTH.getMessage();
        
        if (jwtError instanceof Exception) {
            Exception e = (Exception) jwtError;
            errorMessage = "认证失败: " + e.getMessage();
        }

        Result<String> body = Result.fail(ResultCodeEnum.APP_LOGIN_AUTH.getCode(), errorMessage);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
