package com.dukaanpe.auth.service;

import com.dukaanpe.auth.entity.RefreshToken;
import com.dukaanpe.auth.entity.User;
import java.util.List;

public interface RefreshTokenService {

    String createRefreshToken(User user, String userAgent, String ipAddress, String deviceName);

    RefreshToken verifyRefreshToken(String token);

    void touchRefreshToken(String token, String userAgent, String ipAddress);

    List<RefreshToken> listActiveByUser(User user);

    void deleteByToken(String token);

    void deleteByUser(User user);

    void revokeBySessionId(User user, String sessionId);

    void revokeByDeviceId(User user, String deviceId);
}

