package com.pet.memorial.security;

import com.pet.memorial.entity.User;
import com.pet.memorial.repository.UserRepository;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final String GUEST_USERNAME = "guest";

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (GUEST_USERNAME.equals(username)) {
            return new org.springframework.security.core.userdetails.User(
                GUEST_USERNAME,
                "",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_GUEST"))
            );
        }

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));

        if (Boolean.TRUE.equals(user.getAccountFrozen())) {
            throw new LockedException("账号已被冻结");
        }

        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            Collections.singletonList(new SimpleGrantedAuthority(user.getRole()))
        );
    }
}
