package com.victor.auth_service.controller;

import com.victor.auth_service.dto.RegisterDto;
import com.victor.auth_service.dto.TokenDto;
import com.victor.auth_service.request.LoginRequest;
import com.victor.auth_service.request.RegisterRequest;
import com.victor.auth_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<TokenDto> login(@RequestBody LoginRequest request) {
        log.info("Inside AuthController.login with request: {}", request);
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterDto> register(@RequestBody RegisterRequest request) {
        log.info("Inside AuthController.register with request: {}", request);
        return ResponseEntity.ok(authService.register(request));
    }
}
