package com.verify.panverification.service;

import com.verify.panverification.dto.ApiResponse;
import com.verify.panverification.dto.LoginRequest;
import com.verify.panverification.dto.LoginResponse;
import com.verify.panverification.dto.RegisterRequest;
import com.verify.panverification.entity.Role;
import com.verify.panverification.entity.User;
import com.verify.panverification.repository.UserRepository;
import com.verify.panverification.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public String register(RegisterRequest request)
    {
        log.info("Registering new user with email: {}", request.email());
        User user = new User();

        user.setUsername(request.username());
        user.setFullName(request.FullName());
        user.setEmail(request.email());
        user.setPassword(
                passwordEncoder.encode(
                        request.password()));

        user.setRole(Role.USER);
        userRepository.save(user);

        log.info("User registered successfully: {}", request.email());

        return "User Registered Successfully";
    }

    public ApiResponse<LoginResponse> login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.email());

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.warn("Login failed - user not found: {}", request.email());
                    return new RuntimeException("User not found");
                });

        boolean isValid = passwordEncoder.matches(request.password(), user.getPassword());

        if (!isValid) {
            log.warn("Login failed - invalid password for: {}", request.email());
            throw new RuntimeException("Invalid Password");
        }

        String token = jwtService.generateToken(user.getEmail(),user.getRole().name());
        log.info("Login successful for: {}", request.email());

        LoginResponse loginResponse = new LoginResponse(token, user.getRole().name());

        return new ApiResponse<>(true, "Login Successful", loginResponse);
    }

}
