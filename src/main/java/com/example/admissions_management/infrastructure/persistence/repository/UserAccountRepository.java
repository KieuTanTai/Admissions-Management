package com.example.admissions_management.infrastructure.persistence.repository;

import com.example.admissions_management.infrastructure.persistence.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByUsername(String username);
}
