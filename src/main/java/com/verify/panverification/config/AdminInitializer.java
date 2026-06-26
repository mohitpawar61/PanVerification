package com.verify.panverification.config;

import com.verify.panverification.entity.Role;
import com.verify.panverification.entity.User;
import com.verify.panverification.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer
        implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        log.info("Checking if default admin exists...");
        if (!userRepository.existsByEmail("admin@gmail.com")) {

            User admin = new User();

            admin.setFullName("System Admin");
            admin.setUsername("admin");
            admin.setEmail("admin@gmail.com");

            admin.setPassword(
                    passwordEncoder.encode("admin123")
            );

            admin.setRole(Role.ADMIN);

            userRepository.save(admin);

            log.info("Default admin created successfully with email: admin@gmail.com");
        }
        else{
            log.debug("Default admin already exists, skipping creation");
        }
    }
}
