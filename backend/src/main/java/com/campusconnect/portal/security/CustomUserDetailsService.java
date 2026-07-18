package com.campusconnect.portal.security;

import com.campusconnect.portal.config.props.CacheProperties;
import com.campusconnect.portal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Loads users for authentication by email, or by id for the JWT filter.
 *
 * <p>Resolved principals are cached in {@link CacheProperties#USER_SESSIONS} to spare the DB a
 * round-trip on repeat lookups. Entries carry email- and id-scoped key prefixes so the two lookup
 * paths never collide, and are evicted at account state-change points (see {@code SessionCacheEvictor}).
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheProperties.USER_SESSIONS, key = "'email:' + #email.toLowerCase()")
    public UserDetails loadUserByUsername(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .map(UserPrincipal::from)
                .orElseThrow(() -> new UsernameNotFoundException("No user found with email " + email));
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheProperties.USER_SESSIONS, key = "'id:' + #id")
    public UserDetails loadUserById(UUID id) {
        return userRepository.findWithRolesById(id)
                .map(UserPrincipal::from)
                .orElseThrow(() -> new UsernameNotFoundException("No user found with id " + id));
    }
}
