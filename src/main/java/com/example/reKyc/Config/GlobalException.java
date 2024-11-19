package com.example.reKyc.Config;

import com.example.reKyc.Model.CommonResponse;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.io.PrintWriter;

@RestControllerAdvice
public class GlobalException implements AuthenticationEntryPoint{
    private Logger logger = LoggerFactory.getLogger(AuthenticationEntryPoint.class);

    CommonResponse commonResponse = new CommonResponse();

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse> handleGlobalException(Exception ex) {
        logger.error(ex.getMessage());
        commonResponse.setCode("500");
        commonResponse.setMsg(ex.getMessage());

        return new ResponseEntity<>(commonResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        commonResponse.setCode(String.valueOf(HttpServletResponse.SC_UNAUTHORIZED));
        commonResponse.setMsg("Access denied.");
        Gson gson = new Gson();
        PrintWriter writer=response.getWriter();
        writer.print(gson.toJson(commonResponse));
        writer.flush();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
            CommonResponse commonResponse=new CommonResponse();
            commonResponse.setMsg("One or more field required");
            commonResponse.setCode("400");
        return new ResponseEntity<>(commonResponse, HttpStatus.BAD_REQUEST);
    }

}
