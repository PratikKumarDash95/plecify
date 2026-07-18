package com.campusconnect.portal.repository;

import com.campusconnect.portal.common.enums.RoleType;
import com.campusconnect.portal.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(RoleType name);
    boolean existsByName(RoleType name);
}
