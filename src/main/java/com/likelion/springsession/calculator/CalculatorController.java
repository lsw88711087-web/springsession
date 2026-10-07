package com.likelion.springsession.calculator;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CalculatorController {

    @GetMapping("/divide")
    public int divide(@RequestParam int a, @RequestParam int b) {
        return a / b; // 컨트롤러는 정상 흐름만 신경 쓴다
    }

    @ExceptionHandler(ArithmeticException.class)
    public ResponseEntity<String> handleArithmeticException(ArithmeticException e) {
        return ResponseEntity.badRequest().body("0으로 나눌 수 없습니다");
    }
}