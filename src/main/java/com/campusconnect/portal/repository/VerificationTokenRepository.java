package com.campusconnect.portal.repository;

import com.campusconnect.portal.common.enums.TokenType;
import com.campusconnect.portal.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {

    @Query("select vt from VerificationToken vt join fetch vt.user where vt.tokenHash = :hash")
    Optional<VerificationToken> findByTokenHash(@Param("hash") String tokenHash);

    /** Invalidate any outstanding tokens of a type before issuing a fresh one. */
    @Modifying
    @Query("""
            update VerificationToken vt set vt.usedAt = :now
            where vt.user.id = :userId and vt.type = :type and vt.usedAt is null
            """)
    int invalidateOutstanding(@Param("userId") UUID userId,
                              @Param("type") TokenType type,
                              @Param("now") Instant now);

    @Modifying
    @Query("delete from VerificationToken vt where vt.expiresAt < :cutoff")
    int deleteAllExpiredBefore(@Param("cutoff") Instant cutoff);

    /** Removes every token of a type for a user (used to clear stale login OTPs before issuing a new one). */
    @Modifying
    @Query("delete from VerificationToken vt where vt.user.id = :userId and vt.type = :type")
    int deleteByUserIdAndType(@Param("userId") UUID userId, @Param("type") TokenType type);
}
