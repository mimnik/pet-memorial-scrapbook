package com.pet.memorial.config;

import com.pet.memorial.entity.User;
import com.pet.memorial.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(1)
public class AdminAccountInitializer implements ApplicationRunner {

    private static final String ADMIN_USERNAME = "0001";
    private static final String ADMIN_PASSWORD = "123456";
    private static final String ADMIN_EMAIL = "0001@pet-memorial.local";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminAccountInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        User admin = userRepository.findByUsername(ADMIN_USERNAME).orElseGet(User::new);

        admin.setUsername(ADMIN_USERNAME);
        if (admin.getEmail() == null || admin.getEmail().trim().isEmpty()) {
            admin.setEmail(resolveAdminEmail());
        }
        admin.setDisplayName("系统管理员");
        admin.setRole("ROLE_ADMIN");
        admin.setAccountFrozen(Boolean.FALSE);
        admin.setPostingRestricted(Boolean.FALSE);

        Integer warningCount = admin.getWarningCount();
        admin.setWarningCount(warningCount == null ? 0 : warningCount);

        if (admin.getPassword() == null || !passwordEncoder.matches(ADMIN_PASSWORD, admin.getPassword())) {
            admin.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
        }

        userRepository.save(admin);
    }

    private String resolveAdminEmail() {
        if (!userRepository.existsByEmail(ADMIN_EMAIL)) {
            return ADMIN_EMAIL;
        }
        return "0001-admin@pet-memorial.local";
    }
}
