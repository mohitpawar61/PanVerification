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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public String register(RegisterRequest request)
    {
        User user = new User();

        user.setFullName(request.FullName());
        user.setEmail(request.email());
        user.setPassword(
                passwordEncoder.encode(
                        request.password()));

        user.setRole(Role.USER);
        userRepository.save(user);

        return "User Registered Successfully";
    }

    public ApiResponse<LoginResponse> login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isValid = passwordEncoder.matches(request.password(), user.getPassword());

        if (!isValid) {
            throw new RuntimeException("Invalid Password");
        }

        String token = jwtService.generateToken(user.getEmail(),user.getRole().name());

        LoginResponse loginResponse = new LoginResponse(token, user.getRole().name());

        return new ApiResponse<>(true, "Login Successful", loginResponse);
    }

}
