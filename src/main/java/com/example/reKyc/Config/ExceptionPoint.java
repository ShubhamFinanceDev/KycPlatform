package com.example.reKyc.Config;

import com.example.reKyc.Model.CommonResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

@Component
public class ExceptionPoint  implements AuthenticationEntryPoint {
        @Override
        public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException, IOException {
            ObjectMapper objectMapper = new ObjectMapper();
            HashMap<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", "401");
            errorResponse.put("msg", "authentication required");

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));

//            writer.println("Access Denied !! " + authException.getMessage());
        }
    }

