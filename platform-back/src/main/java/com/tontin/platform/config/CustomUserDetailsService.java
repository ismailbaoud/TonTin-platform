package com.tontin.platform.config;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.tontin.platform.domain.User;
import com.tontin.platform.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email).
        orElseThrow();

        return new CustomUserDetails(
            user.getId(),
            user.getEmail(),
            user.getPassword(),
            user, List.of(new SimpleGrantedAuthority("ROLE_"+user.getRole().name()))
        );
    }    
}
