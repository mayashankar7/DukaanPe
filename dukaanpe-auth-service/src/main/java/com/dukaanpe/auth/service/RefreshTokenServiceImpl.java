package com.dukaanpe.auth.service;

import com.dukaanpe.auth.config.JwtConfig;
import com.dukaanpe.auth.entity.RefreshToken;
import com.dukaanpe.auth.entity.User;
import com.dukaanpe.auth.exception.TokenRefreshException;
import com.dukaanpe.auth.repository.RefreshTokenRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtConfig jwtConfig;

    @Override
    @Transactional
    public String createRefreshToken(User user, String userAgent, String ipAddress, String deviceName) {
        String token = UUID.randomUUID().toString();
        RefreshToken refreshToken = RefreshToken.builder()
            .user(user)
            .token(token)
            .sessionId(UUID.randomUUID().toString())
            .deviceId(UUID.randomUUID().toString())
            .deviceName(deviceName)
            .userAgent(userAgent)
            .ipAddress(ipAddress)
            .expiryDate(LocalDateTime.now().plusSeconds(jwtConfig.getRefreshExpiration() / 1000))
            .isRevoked(false)
            .build();
        refreshTokenRepository.save(refreshToken);
        return token;
    }

    @Override
    @Transactional(readOnly = true)
    public RefreshToken verifyRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenAndIsRevokedFalse(token)
            .orElseThrow(() -> new TokenRefreshException("Refresh token is invalid"));

        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new TokenRefreshException("Refresh token expired. Please login again.");
        }

        return refreshToken;
    }

    @Override
    @Transactional
    public void touchRefreshToken(String token, String userAgent, String ipAddress) {
        refreshTokenRepository.findByTokenAndIsRevokedFalse(token).ifPresent(existing -> {
            existing.setLastActiveAt(LocalDateTime.now());
            if (userAgent != null && !userAgent.isBlank()) {
                existing.setUserAgent(userAgent.trim());
            }
            if (ipAddress != null && !ipAddress.isBlank()) {
                existing.setIpAddress(ipAddress.trim());
            }
            refreshTokenRepository.save(existing);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefreshToken> listActiveByUser(User user) {
        return refreshTokenRepository.findByUserAndIsRevokedFalseOrderByLastActiveAtDescCreatedAtDesc(user);
    }

    @Override
    @Transactional
    public void deleteByToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(existing -> {
            existing.setIsRevoked(true);
            existing.setLastActiveAt(LocalDateTime.now());
            refreshTokenRepository.save(existing);
        });
    }

    @Override
    @Transactional
    public void deleteByUser(User user) {
        List<RefreshToken> activeTokens = refreshTokenRepository.findByUserAndIsRevokedFalseOrderByLastActiveAtDescCreatedAtDesc(user);
        LocalDateTime now = LocalDateTime.now();
        activeTokens.forEach(token -> {
            token.setIsRevoked(true);
            token.setLastActiveAt(now);
        });
        refreshTokenRepository.saveAll(activeTokens);
    }

    @Override
    @Transactional
    public void revokeBySessionId(User user, String sessionId) {
        RefreshToken sessionToken = refreshTokenRepository.findByUserAndSessionIdAndIsRevokedFalse(user, sessionId)
            .orElseThrow(() -> new TokenRefreshException("Session not found"));
        sessionToken.setIsRevoked(true);
        sessionToken.setLastActiveAt(LocalDateTime.now());
        refreshTokenRepository.save(sessionToken);
    }

    @Override
    @Transactional
    public void revokeByDeviceId(User user, String deviceId) {
        List<RefreshToken> deviceTokens = refreshTokenRepository.findByUserAndIsRevokedFalseOrderByLastActiveAtDescCreatedAtDesc(user)
            .stream()
            .filter(token -> deviceId.equals(token.getDeviceId()))
            .toList();

        if (deviceTokens.isEmpty()) {
            throw new TokenRefreshException("Device not found");
        }

        LocalDateTime now = LocalDateTime.now();
        deviceTokens.forEach(token -> {
            token.setIsRevoked(true);
            token.setLastActiveAt(now);
        });
        refreshTokenRepository.saveAll(deviceTokens);
    }
}

