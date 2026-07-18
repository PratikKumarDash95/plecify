package com.campusconnect.portal.repository;

import com.campusconnect.portal.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    @Query("select rt from RefreshToken rt join fetch rt.user where rt.tokenHash = :hash")
    Optional<RefreshToken> findByTokenHash(@Param("hash") String tokenHash);

    /** Revoke all active tokens for a user (logout everywhere / password change). */
    @Modifying
    @Query("""
            update RefreshToken rt set rt.revoked = true, rt.revokedAt = :now
            where rt.user.id = :userId and rt.revoked = false
            """)
    int revokeAllForUser(@Param("userId") UUID userId, @Param("now") Instant now);

    /** Housekeeping: purge tokens expired before the cutoff. */
    @Modifying
    @Query("delete from RefreshToken rt where rt.expiresAt < :cutoff")
    int deleteAllExpiredBefore(@Param("cutoff") Instant cutoff);
}
