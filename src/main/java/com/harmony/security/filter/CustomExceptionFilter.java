package com.harmony.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harmony.exception.ExpiredTokenException;
import com.harmony.exception.InvalidTokenException;
import com.harmony.global.response.code.ErrorCode;
import com.harmony.global.response.structure.ErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class CustomExceptionFilter extends OncePerRequestFilter {
    private final ObjectMapper objectMapper;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try{
            log.info("custom exception filter에 진입했습니다.");
            filterChain.doFilter(request, response);
        }catch(InvalidTokenException e){
            log.error("Exception Filter: 토큰이 이상하다는데요?");
            setErrorResponse(response, ErrorCode.AUTH_INVALID_TOKEN);
        }catch(ExpiredTokenException e){
            log.error("Exception Filter: 토큰이 만료되었다는데요??");
            setErrorResponse(response, ErrorCode.AUTH_EXPIRED_TOKEN);
        }
    }
    private void setErrorResponse(HttpServletResponse response, ErrorCode errorCode){
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        ErrorResponse errorResponse = ErrorResponse.of(errorCode);
        try {
            objectMapper.writeValue(response.getWriter(), errorResponse);
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
