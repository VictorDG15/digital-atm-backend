package com.portfolio.digitalatm.auth;

import com.portfolio.digitalatm.audit.AuditService;
import com.portfolio.digitalatm.common.exception.ConflictException;
import com.portfolio.digitalatm.common.exception.NotFoundException;
import com.portfolio.digitalatm.security.CustomUserDetailsService;
import com.portfolio.digitalatm.security.JwtService;
import com.portfolio.digitalatm.user.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final AuditService auditService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("El username ya está registrado");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("El email ya está registrado");
        }

        Role userRole = roleRepository.findByName(RoleName.USER)
                .orElseThrow(() -> new NotFoundException("Rol USER no configurado"));

        AppUser user = AppUser.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .enabled(true)
                .roles(Set.of(userRole))
                .build();

        AppUser saved = userRepository.save(user);
        auditService.record(saved.getUsername(), "USER_REGISTERED", "SUCCESS", null);
        UserDetails details = userDetailsService.loadUserByUsername(saved.getUsername());
        return new AuthResponse(jwtService.generateToken(details), "Bearer", UserMapper.toResponse(saved));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.usernameOrEmail(),
                request.password()
        ));
        UserDetails details = userDetailsService.loadUserByUsername(request.usernameOrEmail());
        AppUser user = userRepository.findByUsername(details.getUsername())
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        auditService.record(user.getUsername(), "USER_LOGIN", "SUCCESS", null);
        return new AuthResponse(jwtService.generateToken(details), "Bearer", UserMapper.toResponse(user));
    }
}
