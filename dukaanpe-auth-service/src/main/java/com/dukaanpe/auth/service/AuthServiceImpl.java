package com.dukaanpe.auth.service;

import com.dukaanpe.auth.config.JwtConfig;
import com.dukaanpe.auth.dto.AuthResponse;
import com.dukaanpe.auth.dto.ChangePasswordRequest;
import com.dukaanpe.auth.dto.UpdateProfileRequest;
import com.dukaanpe.auth.dto.UpdateRoleRequest;
import com.dukaanpe.auth.dto.UserDeviceResponse;
import com.dukaanpe.auth.dto.UserProfileResponse;
import com.dukaanpe.auth.dto.UserSessionResponse;
import com.dukaanpe.auth.dto.VerifyOtpRequest;
import com.dukaanpe.auth.entity.RefreshToken;
import com.dukaanpe.auth.entity.User;
import com.dukaanpe.auth.entity.UserRole;
import com.dukaanpe.auth.exception.InvalidCredentialsException;
import com.dukaanpe.auth.exception.UserNotFoundException;
import com.dukaanpe.auth.repository.UserRepository;
import com.dukaanpe.auth.security.JwtTokenProvider;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final OtpService otpService;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final JwtConfig jwtConfig;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void sendOtp(String phoneNumber) {
        otpService.sendOtp(phoneNumber);
    }

    @Override
    @Transactional
    public AuthResponse verifyOtp(VerifyOtpRequest request, String userAgent, String ipAddress, String deviceName) {
        otpService.verifyOtp(request.getPhoneNumber(), request.getOtp());

        User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
            .map(existing -> {
                if (request.getFullName() != null && !request.getFullName().isBlank() && existing.getFullName() == null) {
                    existing.setFullName(request.getFullName().trim());
                }
                return existing;
            })
            .orElseGet(() -> userRepository.save(User.builder()
                .phoneNumber(request.getPhoneNumber())
                .fullName(request.getFullName() != null && !request.getFullName().isBlank()
                    ? request.getFullName().trim() : "DukaanPe User")
                .role(UserRole.OWNER)
                .isActive(true)
                .build()));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new UserNotFoundException("User is deactivated. Contact owner for access.");
        }

        String accessToken = jwtTokenProvider.generateToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user, userAgent, ipAddress, deviceName);

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(String refreshToken, String userAgent, String ipAddress) {
        RefreshToken tokenEntity = refreshTokenService.verifyRefreshToken(refreshToken);
        refreshTokenService.touchRefreshToken(refreshToken, userAgent, ipAddress);
        User user = tokenEntity.getUser();
        String accessToken = jwtTokenProvider.generateToken(user);
        return buildAuthResponse(user, accessToken, refreshToken);
    }

    @Override
    public void logout(String refreshToken) {
        refreshTokenService.deleteByToken(refreshToken);
    }

    @Override
    @Transactional
    public Map<String, String> changePassword(String phoneNumber, ChangePasswordRequest request) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
            .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.getPasswordHash() != null && !user.getPasswordHash().isBlank()) {
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
                throw new InvalidCredentialsException("Current password is incorrect");
            }
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return Map.of("status", "ok");
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSessionResponse> listSessions(String phoneNumber) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
            .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<RefreshToken> activeTokens = refreshTokenService.listActiveByUser(user);
        String currentSessionId = activeTokens.isEmpty() ? null : activeTokens.get(0).getSessionId();

        return activeTokens.stream()
            .map(token -> UserSessionResponse.builder()
                .id(token.getSessionId())
                .deviceName(resolveDeviceName(token))
                .ipAddress(token.getIpAddress())
                .userAgent(token.getUserAgent())
                .lastActiveAt(token.getLastActiveAt())
                .current(token.getSessionId() != null && token.getSessionId().equals(currentSessionId))
                .build())
            .toList();
    }

    @Override
    @Transactional
    public void revokeSession(String phoneNumber, String sessionId) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
            .orElseThrow(() -> new UserNotFoundException("User not found"));
        refreshTokenService.revokeBySessionId(user, sessionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDeviceResponse> listDevices(String phoneNumber) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
            .orElseThrow(() -> new UserNotFoundException("User not found"));

        Map<String, UserDeviceResponse> devicesById = new HashMap<>();
        for (RefreshToken token : refreshTokenService.listActiveByUser(user)) {
            String deviceId = token.getDeviceId();
            UserDeviceResponse existing = devicesById.get(deviceId);
            if (existing == null || (existing.getLastSeenAt() != null && token.getLastActiveAt() != null && token.getLastActiveAt().isAfter(existing.getLastSeenAt()))) {
                devicesById.put(deviceId, UserDeviceResponse.builder()
                    .id(deviceId)
                    .deviceName(resolveDeviceName(token))
                    .platform(derivePlatform(token.getUserAgent()))
                    .appVersion("web")
                    .lastSeenAt(token.getLastActiveAt())
                    .trusted(true)
                    .build());
            }
        }

        return devicesById.values().stream()
            .sorted((left, right) -> {
                if (left.getLastSeenAt() == null && right.getLastSeenAt() == null) {
                    return 0;
                }
                if (left.getLastSeenAt() == null) {
                    return 1;
                }
                if (right.getLastSeenAt() == null) {
                    return -1;
                }
                return right.getLastSeenAt().compareTo(left.getLastSeenAt());
            })
            .toList();
    }

    @Override
    @Transactional
    public void revokeDevice(String phoneNumber, String deviceId) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
            .orElseThrow(() -> new UserNotFoundException("User not found"));
        refreshTokenService.revokeByDeviceId(user, deviceId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile(String phoneNumber) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
            .orElseThrow(() -> new UserNotFoundException("User not found"));
        return toProfile(user);
    }

    @Override
    @Transactional
    public UserProfileResponse updateCurrentUserProfile(String phoneNumber, UpdateProfileRequest request) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
            .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setFullName(request.getFullName().trim());
        user.setEmail(request.getEmail());
        return toProfile(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserProfileResponse> listUsers() {
        return userRepository.findAll().stream()
            .map(this::toProfile)
            .toList();
    }

    @Override
    @Transactional
    public UserProfileResponse updateUserRole(Long userId, UpdateRoleRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        user.setRole(request.getRole());
        return toProfile(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        user.setIsActive(false);
        userRepository.save(user);
        refreshTokenService.deleteByUser(user);
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(jwtConfig.getExpiration())
            .userId(user.getId())
            .phoneNumber(user.getPhoneNumber())
            .fullName(user.getFullName())
            .email(user.getEmail())
            .role(user.getRole())
            .build();
    }

    private UserProfileResponse toProfile(User user) {
        return UserProfileResponse.builder()
            .id(user.getId())
            .phoneNumber(user.getPhoneNumber())
            .fullName(user.getFullName())
            .email(user.getEmail())
            .role(user.getRole())
            .isActive(user.getIsActive())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
    }

    private String resolveDeviceName(RefreshToken token) {
        if (token.getDeviceName() != null && !token.getDeviceName().isBlank()) {
            return token.getDeviceName();
        }
        String platform = derivePlatform(token.getUserAgent());
        if (platform != null) {
            return platform + " browser";
        }
        return "Unknown device";
    }

    private String derivePlatform(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return "Web";
        }
        String normalized = userAgent.toLowerCase();
        if (normalized.contains("android")) {
            return "Android";
        }
        if (normalized.contains("iphone") || normalized.contains("ios")) {
            return "iOS";
        }
        if (normalized.contains("windows")) {
            return "Windows";
        }
        if (normalized.contains("mac")) {
            return "macOS";
        }
        if (normalized.contains("linux")) {
            return "Linux";
        }
        return "Web";
    }
}
