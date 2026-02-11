package com.neumarket.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

//Technically, we could inline it in the filter:
//    java// In JwtAuthenticationFilter
//    @Override
//    protected void doFilterInternal(...) {
//      try {
//        // ... validation logic ...
//      } catch (Exception ex) {
//        // Handle error directly in filter   **********************************
//        response.setStatus(401);
//        response.setContentType("application/json");
//        response.getWriter().write("{\"error\": \"Unauthorized\"}");
//        return;  // Don't continue filter chain
//      }
//
//      filterChain.doFilter(request, response);
//    }
@Component
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

  @Override
  public void commence(HttpServletRequest request,
                       HttpServletResponse response,
                       AuthenticationException authException) throws IOException {

    log.error("Unauthorized error: {}", authException.getMessage());

    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

    Map<String, Object> body = new HashMap<>();
    body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
    body.put("message", "Unauthorized - Please login to access this resource");
    body.put("timestamp", LocalDateTime.now().toString());
    body.put("path", request.getServletPath());

    ObjectMapper mapper = new ObjectMapper();
    mapper.writeValue(response.getOutputStream(), body);
  }
}