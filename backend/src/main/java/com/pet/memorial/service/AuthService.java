package com.pet.memorial.service;

import com.pet.memorial.dto.AuthResponse;
import com.pet.memorial.dto.LoginRequest;
import com.pet.memorial.dto.RegisterRequest;
import com.pet.memorial.entity.User;
import com.pet.memorial.repository.UserRepository;
import com.pet.memorial.security.JwtTokenProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final String BUILTIN_ADMIN_USERNAME = "0001";
    private static final String BUILTIN_ADMIN_PASSWORD = "123456";
    private static final String BUILTIN_ADMIN_EMAIL = "0001@pet-memorial.local";
    private static final String GUEST_USERNAME = "guest";
    private static final String GUEST_ROLE = "ROLE_GUEST";
    private static final String GUEST_DISPLAY_NAME = "游客";
    private static final String GUEST_EMAIL = "guest@pet-memorial.local";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public AuthResponse register(RegisterRequest request) {
        String username = request.getUsername() == null ? "" : request.getUsername().trim();
        if (BUILTIN_ADMIN_USERNAME.equals(username)) {
            throw new IllegalArgumentException("该用户名为系统管理员保留账号，请直接登录");
        }

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("用户名已存在");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("邮箱已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setDisplayName(username);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("ROLE_USER");
        User saved = userRepository.save(user);

        UserDetails details = new org.springframework.security.core.userdetails.User(
            saved.getUsername(),
            saved.getPassword(),
            java.util.Collections.singletonList(() -> saved.getRole())
        );
        String token = jwtTokenProvider.generateToken(details);
        return new AuthResponse(
            token,
            saved.getUsername(),
            saved.getEmail(),
            saved.getRole(),
            saved.getDisplayName(),
            saved.getAvatarUrl()
        );
    }

    public AuthResponse login(LoginRequest request) {
        String username = request.getUsername() == null ? "" : request.getUsername().trim();

        if (BUILTIN_ADMIN_USERNAME.equals(username)) {
            ensureBuiltinAdminAccount();
        }

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("账户不存在"));

        if (Boolean.TRUE.equals(user.getAccountFrozen())) {
            throw new IllegalArgumentException("账号已被冻结，请联系管理员处理");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("密码错误");
        }

        UserDetails principal = new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            java.util.Collections.singletonList(() -> user.getRole())
        );
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            principal,
            null,
            principal.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtTokenProvider.generateToken(principal);
        return new AuthResponse(
            token,
            user.getUsername(),
            user.getEmail(),
            user.getRole(),
            user.getDisplayName(),
            user.getAvatarUrl()
        );
    }

    public AuthResponse guestLogin() {
        UserDetails principal = new org.springframework.security.core.userdetails.User(
            GUEST_USERNAME,
            "",
            java.util.Collections.singletonList(() -> GUEST_ROLE)
        );

        Authentication authentication = new UsernamePasswordAuthenticationToken(
            principal,
            null,
            principal.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtTokenProvider.generateToken(principal);
        return new AuthResponse(
            token,
            GUEST_USERNAME,
            GUEST_EMAIL,
            GUEST_ROLE,
            GUEST_DISPLAY_NAME,
            null
        );
    }

    private void ensureBuiltinAdminAccount() {
        User admin = userRepository.findByUsername(BUILTIN_ADMIN_USERNAME).orElseGet(User::new);

        admin.setUsername(BUILTIN_ADMIN_USERNAME);
        if (admin.getEmail() == null || admin.getEmail().trim().isEmpty()) {
            admin.setEmail(resolveBuiltinAdminEmail());
        }
        admin.setDisplayName("系统管理员");
        admin.setRole("ROLE_ADMIN");
        admin.setAccountFrozen(Boolean.FALSE);
        admin.setPostingRestricted(Boolean.FALSE);
        admin.setWarningCount(0);
        admin.setPassword(passwordEncoder.encode(BUILTIN_ADMIN_PASSWORD));

        userRepository.save(admin);
    }

    private String resolveBuiltinAdminEmail() {
        if (!userRepository.existsByEmail(BUILTIN_ADMIN_EMAIL)) {
            return BUILTIN_ADMIN_EMAIL;
        }
        return "0001-admin@pet-memorial.local";
    }

    public AuthResponse me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalArgumentException("未登录");
        }

        if (GUEST_USERNAME.equals(authentication.getName())) {
            return new AuthResponse(
                null,
                GUEST_USERNAME,
                GUEST_EMAIL,
                GUEST_ROLE,
                GUEST_DISPLAY_NAME,
                null
            );
        }

        User user = userRepository.findByUsername(authentication.getName())
            .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        return new AuthResponse(
            null,
            user.getUsername(),
            user.getEmail(),
            user.getRole(),
            user.getDisplayName(),
            user.getAvatarUrl()
        );
    }
}
