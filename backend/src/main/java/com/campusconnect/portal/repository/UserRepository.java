package com.campusconnect.portal.repository;

import com.campusconnect.portal.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    /** Loads a user with roles eagerly for authentication (avoids a second round-trip). */
    @EntityGraph(attributePaths = "roles")
    Optional<User> findByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = "roles")
    Optional<User> findWithRolesById(UUID id);

    boolean existsByEmailIgnoreCase(String email);

    @Query("select count(u) from User u join u.roles r where r.name = :roleName")
    long countByRole(@Param("roleName") com.campusconnect.portal.common.enums.RoleType roleName);
}
