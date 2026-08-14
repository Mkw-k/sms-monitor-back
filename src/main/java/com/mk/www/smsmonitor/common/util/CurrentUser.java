package com.mk.www.smsmonitor.common.util;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * packageName    : com.mk.www.smsmonitor.common.util
 * fileName       : CurrentUser
 * author         : rhaud
 * date           : 2026-03-23
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-23        rhaud       최초 생성
 */
@Target(ElementType.PARAMETER) // 파라미터에만 붙일 수 있게 설정
@Retention(RetentionPolicy.RUNTIME) // 실행 중에도 정보를 유지
@AuthenticationPrincipal(expression = "#this instanceof T(com.mk.www.smsmonitor.user.application.CustomUserDetails) ? user : null")
public @interface CurrentUser {
}