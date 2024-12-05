package com.sunware.api_gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sunware.api_gateway.service.AuthService;
import com.sunware.api_gateway.util.JwtUtil;
import com.sunware.api_gateway.model.Permission; // Import Permission model if necessary

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    // Endpoint to request OTP
    @PostMapping("/generate-otp")
    public ResponseEntity<String> generateOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        try {
            authService.generateOtp(email);
            return ResponseEntity.ok("OTP sent to your email.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/validate-otp")
    public ResponseEntity<Map<String, String>> validateOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");

        try {
            // Validate OTP and retrieve JWT
            String jwt = authService.validateOtpAndGenerateToken(email, otp);

            // Prepare response with both message and JWT token
            Map<String, String> response = Map.of(
                "message", "OTP validated successfully",
                "token", jwt
            );
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(Map.of("message", e.getMessage()));
        }
    }
}
