package com.example.reKyc.Config;

import com.example.reKyc.Model.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalException {
        @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
            CommonResponse commonResponse=new CommonResponse();
            commonResponse.setMsg("One o more field required");
            commonResponse.setCode("400");
        return new ResponseEntity<>(commonResponse, HttpStatus.BAD_REQUEST);
    }
}
