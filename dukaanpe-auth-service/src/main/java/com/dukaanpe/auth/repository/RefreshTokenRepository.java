package com.dukaanpe.auth.repository;

import com.dukaanpe.auth.entity.RefreshToken;
import com.dukaanpe.auth.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByTokenAndIsRevokedFalse(String token);

    List<RefreshToken> findByUserAndIsRevokedFalseOrderByLastActiveAtDescCreatedAtDesc(User user);

    Optional<RefreshToken> findByUserAndSessionIdAndIsRevokedFalse(User user, String sessionId);

    void deleteByToken(String token);

    void deleteByUser(User user);

    void deleteByUserAndSessionId(User user, String sessionId);

    void deleteByUserAndDeviceId(User user, String deviceId);
}

