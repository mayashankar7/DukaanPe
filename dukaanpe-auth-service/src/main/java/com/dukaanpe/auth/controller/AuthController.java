package com.dukaanpe.auth.controller;

import com.dukaanpe.auth.dto.ApiResponse;
import com.dukaanpe.auth.dto.AuthResponse;
import com.dukaanpe.auth.dto.ChangePasswordRequest;
import com.dukaanpe.auth.dto.RefreshTokenRequest;
import com.dukaanpe.auth.dto.SendOtpRequest;
import com.dukaanpe.auth.dto.UpdateProfileRequest;
import com.dukaanpe.auth.dto.UpdateRoleRequest;
import com.dukaanpe.auth.dto.UserDeviceResponse;
import com.dukaanpe.auth.dto.UserProfileResponse;
import com.dukaanpe.auth.dto.UserSessionResponse;
import com.dukaanpe.auth.dto.VerifyOtpRequest;
import com.dukaanpe.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<Map<String, String>>> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        authService.sendOtp(request.getPhoneNumber());
        ApiResponse<Map<String, String>> response = ApiResponse.success(
            "OTP sent successfully",
            Map.of("phoneNumber", request.getPhoneNumber())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtp(
        @Valid @RequestBody VerifyOtpRequest request,
        HttpServletRequest httpServletRequest
    ) {
        AuthResponse authResponse = authService.verifyOtp(
            request,
            resolveUserAgent(httpServletRequest),
            resolveClientIp(httpServletRequest),
            resolveDeviceName(httpServletRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("OTP verified successfully", authResponse));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
        @Valid @RequestBody RefreshTokenRequest request,
        HttpServletRequest httpServletRequest
    ) {
        AuthResponse authResponse = authService.refreshToken(
            request.getRefreshToken(),
            resolveUserAgent(httpServletRequest),
            resolveClientIp(httpServletRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Token refreshed", authResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Map<String, String>>> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", Map.of("status", "ok")));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> me(Authentication authentication) {
        UserProfileResponse profile = authService.getCurrentUserProfile(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMe(
        Authentication authentication,
        @Valid @RequestBody UpdateProfileRequest request
    ) {
        UserProfileResponse profile = authService.updateCurrentUserProfile(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated", profile));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Map<String, String>>> changePassword(
        Authentication authentication,
        @Valid @RequestBody ChangePasswordRequest request
    ) {
        Map<String, String> response = authService.changePassword(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("Password updated", response));
    }

    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<List<UserSessionResponse>>> listSessions(Authentication authentication) {
        List<UserSessionResponse> sessions = authService.listSessions(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(sessions));
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<ApiResponse<Map<String, String>>> revokeSession(
        Authentication authentication,
        @PathVariable("sessionId") String sessionId
    ) {
        authService.revokeSession(authentication.getName(), sessionId);
        return ResponseEntity.ok(ApiResponse.success("Session revoked", Map.of("status", "ok")));
    }

    @GetMapping("/devices")
    public ResponseEntity<ApiResponse<List<UserDeviceResponse>>> listDevices(Authentication authentication) {
        List<UserDeviceResponse> devices = authService.listDevices(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(devices));
    }

    @DeleteMapping("/devices/{deviceId}")
    public ResponseEntity<ApiResponse<Map<String, String>>> revokeDevice(
        Authentication authentication,
        @PathVariable("deviceId") String deviceId
    ) {
        authService.revokeDevice(authentication.getName(), deviceId);
        return ResponseEntity.ok(ApiResponse.success("Device revoked", Map.of("status", "ok")));
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<List<UserProfileResponse>>> listUsers() {
        return ResponseEntity.ok(ApiResponse.success(authService.listUsers()));
    }

    @PutMapping("/users/{id}/role")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateRole(
        @PathVariable("id") Long id,
        @Valid @RequestBody UpdateRoleRequest request
    ) {
        UserProfileResponse response = authService.updateUserRole(id, request);
        return ResponseEntity.ok(ApiResponse.success("Role updated", response));
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> deactivateUser(@PathVariable("id") Long id) {
        authService.deactivateUser(id);
        return ResponseEntity.noContent().build();
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String resolveUserAgent(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return userAgent != null ? userAgent.trim() : null;
    }

    private String resolveDeviceName(HttpServletRequest request) {
        String deviceName = request.getHeader("X-Device-Name");
        return deviceName != null ? deviceName.trim() : null;
    }
}
