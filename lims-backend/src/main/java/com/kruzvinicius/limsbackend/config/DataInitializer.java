package com.kruzvinicius.limsbackend.config;

import com.kruzvinicius.limsbackend.model.Role;
import com.kruzvinicius.limsbackend.model.User;
import com.kruzvinicius.limsbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initDatabase() {
        return args -> {
            log.info("Checking administrative accounts...");
            var adminOpt = userRepository.findByUsername("admin");
            if (adminOpt.isEmpty()) {
                log.info("No 'admin' account found. Provisioning default operator...");
                
                var adminUser = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin123"))
                        .role(Role.ADMIN)
                        .laboratoryUnit("HQ-SYSTEM")
                        .build();

                userRepository.save(adminUser);
                log.info("Default 'admin' operator synthesized successfully! Password: admin123");
            } else {
                log.info("Admin account exists. Forcing password reset to admin123 for safety...");
                User adminUser = adminOpt.get();
                adminUser.setPassword(passwordEncoder.encode("admin123"));
                userRepository.save(adminUser);
            }
        };
    }
}
