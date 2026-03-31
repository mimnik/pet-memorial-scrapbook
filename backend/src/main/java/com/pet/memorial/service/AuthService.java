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
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("用户名已存在");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("邮箱已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setDisplayName(request.getUsername().trim());
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
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("账户不存在"));

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

    public AuthResponse me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalArgumentException("未登录");
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
