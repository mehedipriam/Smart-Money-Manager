package com.smartmoneymanager.backend.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartmoneymanager.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/** Used by {@code AuthenticationManager} during login, looking users up by email. */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .map(UserPrincipal::of)
                .orElseThrow(() -> new UsernameNotFoundException("No account found for email: " + email));
    }
}
