package com.harmony.global.annotation;

import com.harmony.security.impl.UserDetailsImpl;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class CurrentUserIdArgumentResolver implements HandlerMethodArgumentResolver {
  @Override
  public boolean supportsParameter(MethodParameter parameter){
    return parameter.hasParameterAnnotation(CurrentUserId.class);
  }

  @Override
  public Long resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest, WebDataBinderFactory binderFactory){
    Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
    if(authentication==null){
      return null;
    }

    UserDetailsImpl principal=(UserDetailsImpl)authentication.getPrincipal();

    return principal.getUserId();
  }
}
