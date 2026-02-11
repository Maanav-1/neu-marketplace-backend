package com.neumarket.security;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.*;

/**
 * Custom annotation to inject the currently authenticated user into controller methods.
 *
 * Usage:
 * @GetMapping("/me")
 * public UserResponse getCurrentUser(@CurrentUser UserPrincipal user) {
 *     return userService.getUserById(user.getId());
 * }
 */
@Target({ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AuthenticationPrincipal
public @interface CurrentUser {
}