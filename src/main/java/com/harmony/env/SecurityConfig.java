package com.harmony.env;

import com.harmony.security.filter.AuthenticationFilter;
import com.harmony.security.filter.CustomExceptionFilter;
import com.harmony.security.handler.JwtAccessDeniedHandler;
import com.harmony.security.handler.JwtAuthenticationEntryPoint;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
  private final String[] allowedUrls=
      {"/ws/**", "/v3/api-docs/**", "/swagger/**", "/swagger-ui/**", "/swagger-resources/**", "/user/register", "/auth/login", "/auth/reissue", "/error"};
  private final CustomExceptionFilter customExceptionFilter;
  private final AuthenticationFilter authenticationFilter;
  private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
  private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

  @Bean
  public PasswordEncoder passwordEncoder(){
    return new BCryptPasswordEncoder();
  }

  // http에 대해 인증, 인가 담당
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
    return http
        .csrf(AbstractHttpConfigurer::disable) // csrf 보호 비활성화
        .cors(cors->cors.configurationSource(corsConfigurationSource())) // cors 커스텀 설정 적용
        .authorizeHttpRequests(requests->
            requests.requestMatchers(allowedUrls).permitAll()
                .anyRequest().authenticated()) // 지정된 url 외 인증 없이 접근 불가능
        // UsernamePasswordAthenticationFilter 이전에 JwtAuthorizationFilter가 수행된다. UsernamePasswordAuthenticationFilter를 거치기 전에 이미 인증이 완료된다.
        .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class) // JWT 인증 추가(커스텀)
        .addFilterBefore(customExceptionFilter, AuthenticationFilter.class)
        .exceptionHandling(exceptionConfig->
            exceptionConfig.accessDeniedHandler(jwtAccessDeniedHandler)
                .authenticationEntryPoint(jwtAuthenticationEntryPoint))
        .sessionManagement(session->session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 미사용(JWT 사용)
        .build();
  }

  // cors 설정
  @Bean
  public CorsConfigurationSource corsConfigurationSource(){
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOriginPatterns(List.of("*"));
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source=new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration); // 모든 경로에 대해 이 설정 적용
    return source;
  }
}
