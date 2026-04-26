package com.example.admissions_management.application.service;

import com.example.admissions_management.application.dto.request.UserAccountRequest;
import com.example.admissions_management.application.dto.response.UserAccountResponse;
import com.example.admissions_management.domain.model.UserRole;
import com.example.admissions_management.infrastructure.persistence.entity.UserAccount;
import com.example.admissions_management.infrastructure.persistence.repository.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserAccountService {

    private final UserAccountRepository repository;

    public UserAccountService(UserAccountRepository repository) {
        this.repository = repository;
    }

    public List<UserAccountResponse> getAllUsers() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Optional<UserAccountResponse> getUserById(Long id) {
        return repository.findById(id).map(this::toResponse);
    }

    public UserAccountResponse save(UserAccountRequest request) {
        UserAccount entity = request.getId() != null ? repository.findById(request.getId()).orElse(new UserAccount()) : new UserAccount();

        if (entity.getId() == null) {
            entity.setUsername(request.getUsername());
        }
        if (request.getFullName() != null) {
            entity.setFullName(request.getFullName());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            entity.setPassword(request.getPassword());
        }
        entity.setRole(request.getRole() != null ? request.getRole() : UserRole.USER);
        entity.setEnabled(request.getEnabled() != null ? request.getEnabled() : Boolean.TRUE);

        entity = repository.save(entity);
        return toResponse(entity);
    }

    public UserAccountResponse changePassword(Long id, String newPassword) {
        UserAccount entity = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại."));
        entity.setPassword(newPassword);
        entity = repository.save(entity);
        return toResponse(entity);
    }

    public UserAccountResponse setRole(Long id, UserRole role) {
        UserAccount entity = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại."));
        entity.setRole(role);
        entity = repository.save(entity);
        return toResponse(entity);
    }

    public UserAccountResponse setEnabled(Long id, boolean enabled) {
        UserAccount entity = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại."));
        entity.setEnabled(enabled);
        entity = repository.save(entity);
        return toResponse(entity);
    }

    private UserAccountResponse toResponse(UserAccount account) {
        return new UserAccountResponse(
                account.getId(),
                account.getUsername(),
                account.getFullName(),
                account.getRole(),
                account.getEnabled()
        );
    }
}
