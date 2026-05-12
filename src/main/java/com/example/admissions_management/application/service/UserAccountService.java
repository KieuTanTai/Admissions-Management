package com.example.admissions_management.application.service;

import com.example.admissions_management.application.dto.request.UserAccountRequest;
import com.example.admissions_management.application.dto.response.UserAccountResponse;
import com.example.admissions_management.domain.model.UserRole;
import com.example.admissions_management.infrastructure.persistence.entity.UserAccountEntity;
import com.example.admissions_management.infrastructure.persistence.repository.UserAccountRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserAccountService {

    private final UserAccountRepository userAccountRepository;

    public UserAccountService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    public List<UserAccountResponse> getAllUsers() {
        return userAccountRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Optional<UserAccountResponse> getUserById(Long id) {
        return userAccountRepository.findById(id).map(this::toResponse);
    }

    public UserAccountResponse save(UserAccountRequest request) {
        UserAccountEntity entity;

        if (request.getId() != null) {
            entity = userAccountRepository.findById(request.getId())
                    .orElse(new UserAccountEntity());
        } else {
            if (request.getUsername() == null || request.getUsername().isBlank()) {
                throw new IllegalArgumentException("Tên đăng nhập không được để trống.");
            }
            if (userAccountRepository.existsByUsername(request.getUsername())) {
                throw new IllegalArgumentException("Tên đăng nhập đã tồn tại.");
            }
            entity = new UserAccountEntity();
            entity.setCreatedAt(LocalDateTime.now());
        }

        entity.setUsername(request.getUsername());
        entity.setFullName(request.getFullName());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            entity.setPassword(request.getPassword());
        } else if (entity.getId() == null) {
            throw new IllegalArgumentException("Mật khẩu bắt buộc khi tạo mới người dùng.");
        }

        entity.setRole(request.getRole() == null ? UserRole.USER : request.getRole());
        entity.setEnabled(request.isEnabled());
        entity.setUpdatedAt(LocalDateTime.now());

        return toResponse(userAccountRepository.save(entity));
    }

    public void changePassword(Long id, String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("Mật khẩu mới không được để trống.");
        }
        UserAccountEntity entity = userAccountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại."));
        entity.setPassword(newPassword);
        entity.setUpdatedAt(LocalDateTime.now());
        userAccountRepository.save(entity);
    }

    public void setRole(Long id, UserRole role) {
        UserAccountEntity entity = userAccountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại."));
        entity.setRole(role);
        entity.setUpdatedAt(LocalDateTime.now());
        userAccountRepository.save(entity);
    }

    public void setEnabled(Long id, boolean enabled) {
        UserAccountEntity entity = userAccountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại."));
        entity.setEnabled(enabled);
        entity.setUpdatedAt(LocalDateTime.now());
        userAccountRepository.save(entity);
    }

    private UserAccountResponse toResponse(UserAccountEntity entity) {
        UserAccountResponse response = new UserAccountResponse();
        response.setId(entity.getId());
        response.setUsername(entity.getUsername());
        response.setFullName(entity.getFullName());
        response.setRole(entity.getRole() == null ? UserRole.USER : entity.getRole());
        response.setEnabled(entity.isEnabled());
        return response;
    }
}
