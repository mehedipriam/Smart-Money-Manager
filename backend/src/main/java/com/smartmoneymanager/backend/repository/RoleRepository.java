package com.smartmoneymanager.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartmoneymanager.backend.entity.Role;
import com.smartmoneymanager.backend.entity.enums.RoleName;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleName name);
}
