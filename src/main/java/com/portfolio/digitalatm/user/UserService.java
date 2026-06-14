package com.portfolio.digitalatm.user;

import com.portfolio.digitalatm.audit.AuditService;
import com.portfolio.digitalatm.common.exception.ConflictException;
import com.portfolio.digitalatm.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public UserResponse me(String username) {
        return UserMapper.toResponse(findByUsername(username));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream().map(UserMapper::toResponse).toList();
    }

    @Transactional
    public UserResponse updateMe(String username, UpdateUserRequest request) {
        AppUser user = findByUsername(username);
        userRepository.findByEmail(request.email())
                .filter(found -> !found.getId().equals(user.getId()))
                .ifPresent(found -> { throw new ConflictException("El email ya está registrado"); });
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        auditService.record(username, "USER_PROFILE_UPDATED", "SUCCESS", null);
        return UserMapper.toResponse(userRepository.save(user));
    }

    public AppUser findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
    }
}
