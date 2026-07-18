package com.campusconnect.portal.mapper;

import com.campusconnect.portal.dto.auth.UserResponse;
import com.campusconnect.portal.entity.Role;
import com.campusconnect.portal.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Maps {@link User} entities to their public {@link UserResponse} projection. Roles are
 * flattened to their plain names (no {@code ROLE_} prefix) for the API contract.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roles", source = "roles", qualifiedByName = "roleNames")
    UserResponse toResponse(User user);

    @Named("roleNames")
    default Set<String> roleNames(Set<Role> roles) {
        if (roles == null) {
            return Set.of();
        }
        return roles.stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toUnmodifiableSet());
    }
}
