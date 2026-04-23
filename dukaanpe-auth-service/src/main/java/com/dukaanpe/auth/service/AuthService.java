package com.dukaanpe.auth.service;

import com.dukaanpe.auth.dto.AuthResponse;
import com.dukaanpe.auth.dto.ChangePasswordRequest;
import com.dukaanpe.auth.dto.UpdateProfileRequest;
import com.dukaanpe.auth.dto.UpdateRoleRequest;
import com.dukaanpe.auth.dto.UserDeviceResponse;
import com.dukaanpe.auth.dto.UserProfileResponse;
import com.dukaanpe.auth.dto.UserSessionResponse;
import com.dukaanpe.auth.dto.VerifyOtpRequest;
import java.util.List;
import java.util.Map;

public interface AuthService {

    void sendOtp(String phoneNumber);

    AuthResponse verifyOtp(VerifyOtpRequest request, String userAgent, String ipAddress, String deviceName);

    AuthResponse refreshToken(String refreshToken, String userAgent, String ipAddress);

    void logout(String refreshToken);

    Map<String, String> changePassword(String phoneNumber, ChangePasswordRequest request);

    List<UserSessionResponse> listSessions(String phoneNumber);

    void revokeSession(String phoneNumber, String sessionId);

    List<UserDeviceResponse> listDevices(String phoneNumber);

    void revokeDevice(String phoneNumber, String deviceId);

    UserProfileResponse getCurrentUserProfile(String phoneNumber);

    UserProfileResponse updateCurrentUserProfile(String phoneNumber, UpdateProfileRequest request);

    List<UserProfileResponse> listUsers();

    UserProfileResponse updateUserRole(Long userId, UpdateRoleRequest request);

    void deactivateUser(Long userId);
}

