package com.portfolio.digitalatm.auth;

import com.portfolio.digitalatm.audit.AuditService;
import com.portfolio.digitalatm.security.CustomUserDetailsService;
import com.portfolio.digitalatm.security.JwtService;
import com.portfolio.digitalatm.user.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private CustomUserDetailsService userDetailsService;
    @Mock private JwtService jwtService;
    @Mock private AuditService auditService;

    @InjectMocks private AuthService authService;

    @Test
    void registerCreatesUserAndReturnsToken() {
        RegisterRequest request = new RegisterRequest("Victor", "Diaz", "victor", "victor@test.com", "secret123");
        Role role = Role.builder().id(1L).name(RoleName.USER).build();
        AppUser saved = AppUser.builder()
                .id(10L)
                .firstName("Victor")
                .lastName("Diaz")
                .username("victor")
                .email("victor@test.com")
                .password("encoded")
                .enabled(true)
                .roles(Set.of(role))
                .build();
        UserDetails details = org.springframework.security.core.userdetails.User
                .withUsername("victor")
                .password("encoded")
                .roles("USER")
                .build();

        when(userRepository.existsByUsername("victor")).thenReturn(false);
        when(userRepository.existsByEmail("victor@test.com")).thenReturn(false);
        when(roleRepository.findByName(RoleName.USER)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("secret123")).thenReturn("encoded");
        when(userRepository.save(any(AppUser.class))).thenReturn(saved);
        when(userDetailsService.loadUserByUsername("victor")).thenReturn(details);
        when(jwtService.generateToken(details)).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.user().username()).isEqualTo("victor");
        verify(auditService).record("victor", "USER_REGISTERED", "SUCCESS", null);
    }

    @Test
    void loginReturnsToken() {
        LoginRequest request = new LoginRequest("victor", "secret123");
        Role role = Role.builder().id(1L).name(RoleName.USER).build();
        AppUser user = AppUser.builder()
                .id(10L)
                .firstName("Victor")
                .lastName("Diaz")
                .username("victor")
                .email("victor@test.com")
                .password("encoded")
                .enabled(true)
                .roles(Set.of(role))
                .build();
        UserDetails details = org.springframework.security.core.userdetails.User
                .withUsername("victor")
                .password("encoded")
                .roles("USER")
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken("victor", null));
        when(userDetailsService.loadUserByUsername("victor")).thenReturn(details);
        when(userRepository.findByUsername("victor")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(details)).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        verify(auditService).record("victor", "USER_LOGIN", "SUCCESS", null);
    }
}
