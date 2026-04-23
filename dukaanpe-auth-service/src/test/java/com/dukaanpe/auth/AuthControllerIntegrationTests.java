package com.dukaanpe.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.dukaanpe.auth.repository.OtpRecordRepository;
import com.dukaanpe.auth.repository.RefreshTokenRepository;
import com.dukaanpe.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthControllerIntegrationTests {

    private static final AtomicInteger PHONE_COUNTER = new AtomicInteger(12000000);

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private OtpRecordRepository otpRecordRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void resetAuthState() {
        refreshTokenRepository.deleteAll();
        otpRecordRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void sendOtpReturnsCreatedForValidPhone() {
        String phoneNumber = nextPhoneNumber();
        String url = "http://localhost:" + port + "/api/auth/send-otp";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>("{\"phoneNumber\":\"" + phoneNumber + "\"}", headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(true);
    }

    @Test
    void verifyOtpSupportsFixedDemoOtp() {
        String phoneNumber = nextPhoneNumber();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(
            "{\"phoneNumber\":\"" + phoneNumber + "\",\"otp\":\"123456\",\"fullName\":\"Rajesh Kumar\"}",
            headers
        );

        ResponseEntity<Map> response = exchangeWithRetry(
            "/api/auth/verify-otp",
            HttpMethod.POST,
            request,
            Map.class,
            Duration.ofSeconds(5)
        );

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(true);

        Map data = (Map) response.getBody().get("data");
        assertThat(data).isNotNull();
        assertThat(data.get("accessToken")).isNotNull();
        assertThat(data.get("refreshToken")).isNotNull();
        assertThat(data.get("phoneNumber")).isEqualTo(phoneNumber);
        assertThat(data.get("role")).isNotNull();
        assertThat(data.get("tokenType")).isEqualTo("Bearer");
    }

    @Test
    void refreshTokenAndMeProfileFlowWorksReliably() {
        String phoneNumber = nextPhoneNumber();

        HttpHeaders jsonHeaders = new HttpHeaders();
        jsonHeaders.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> verifyRequest = new HttpEntity<>(
            "{\"phoneNumber\":\"" + phoneNumber + "\",\"otp\":\"123456\",\"fullName\":\"Integration User\"}",
            jsonHeaders
        );

        ResponseEntity<Map> verifyResponse = exchangeWithRetry(
            "/api/auth/verify-otp",
            HttpMethod.POST,
            verifyRequest,
            Map.class,
            Duration.ofSeconds(5)
        );

        Map verifyData = assertAndExtractVerifyData(verifyResponse);
        String accessToken = (String) verifyData.get("accessToken");
        String refreshToken = (String) verifyData.get("refreshToken");

        assertThat(accessToken).isNotBlank();
        assertThat(refreshToken).isNotBlank();

        HttpEntity<String> refreshRequest = new HttpEntity<>(
            "{\"refreshToken\":\"" + refreshToken + "\"}",
            jsonHeaders
        );

        ResponseEntity<Map> refreshResponse = exchangeWithRetry(
            "/api/auth/refresh-token",
            HttpMethod.POST,
            refreshRequest,
            Map.class,
            Duration.ofSeconds(5)
        );

        assertThat(refreshResponse.getStatusCode().value()).isEqualTo(200);
        Map refreshData = (Map) refreshResponse.getBody().get("data");
        String refreshedAccessToken = (String) refreshData.get("accessToken");
        assertThat(refreshedAccessToken).isNotBlank();

        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setBearerAuth(refreshedAccessToken);

        ResponseEntity<Map> meResponse = exchangeWithRetry(
            "/api/auth/me",
            HttpMethod.GET,
            new HttpEntity<>(authHeaders),
            Map.class,
            Duration.ofSeconds(5)
        );

        assertThat(meResponse.getStatusCode().value()).isEqualTo(200);
        Map meData = (Map) meResponse.getBody().get("data");
        assertThat(meData.get("phoneNumber")).isEqualTo(phoneNumber);
        assertThat(meData.get("fullName")).isEqualTo("Integration User");
    }

    @Test
    void verifyOtpRejectsInvalidOtp() {
        String phoneNumber = nextPhoneNumber();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(
            "{\"phoneNumber\":\"" + phoneNumber + "\",\"otp\":\"000000\",\"fullName\":\"Wrong Otp\"}",
            headers
        );

        ResponseEntity<Map> response = exchangeWithRetry(
            "/api/auth/verify-otp",
            HttpMethod.POST,
            request,
            Map.class,
            Duration.ofSeconds(5)
        );

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(false);
    }

    @Test
    void logoutInvalidatesRefreshToken() {
        String phoneNumber = nextPhoneNumber();
        HttpHeaders jsonHeaders = new HttpHeaders();
        jsonHeaders.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> verifyRequest = new HttpEntity<>(
            "{\"phoneNumber\":\"" + phoneNumber + "\",\"otp\":\"123456\",\"fullName\":\"Logout User\"}",
            jsonHeaders
        );

        ResponseEntity<Map> verifyResponse = exchangeWithRetry(
            "/api/auth/verify-otp",
            HttpMethod.POST,
            verifyRequest,
            Map.class,
            Duration.ofSeconds(5)
        );

        Map verifyData = assertAndExtractVerifyData(verifyResponse);
        String accessToken = (String) verifyData.get("accessToken");
        String refreshToken = (String) verifyData.get("refreshToken");
        assertThat(refreshToken).isNotBlank();

        HttpHeaders logoutHeaders = new HttpHeaders();
        logoutHeaders.setContentType(MediaType.APPLICATION_JSON);
        logoutHeaders.setBearerAuth(accessToken);

        HttpEntity<String> logoutRequest = new HttpEntity<>(
            "{\"refreshToken\":\"" + refreshToken + "\"}",
            logoutHeaders
        );

        ResponseEntity<Map> logoutResponse = exchangeWithRetry(
            "/api/auth/logout",
            HttpMethod.POST,
            logoutRequest,
            Map.class,
            Duration.ofSeconds(5)
        );

        assertThat(logoutResponse.getStatusCode().value()).isEqualTo(200);

        ResponseEntity<Map> refreshResponse = exchangeWithRetry(
            "/api/auth/refresh-token",
            HttpMethod.POST,
            logoutRequest,
            Map.class,
            Duration.ofSeconds(5)
        );

        assertThat(refreshResponse.getStatusCode().value()).isEqualTo(400);
        assertThat(refreshResponse.getBody()).isNotNull();
        assertThat(refreshResponse.getBody().get("success")).isEqualTo(false);
    }

    @Test
    void usersEndpointRequiresAuthentication() {
        ResponseEntity<Map> response = exchangeWithRetry(
            "/api/auth/users",
            HttpMethod.GET,
            HttpEntity.EMPTY,
            Map.class,
            Duration.ofSeconds(5)
        );

        assertThat(response.getStatusCode().value()).isEqualTo(403);
    }

    @Test
    void changePasswordAndSessionsFlowWorks() {
        Map<String, String> tokens = loginAndGetTokens(nextPhoneNumber(), "Security User", "Kirana POS Web");
        String accessToken = tokens.get("accessToken");

        HttpHeaders authJsonHeaders = new HttpHeaders();
        authJsonHeaders.setContentType(MediaType.APPLICATION_JSON);
        authJsonHeaders.setBearerAuth(accessToken);

        HttpEntity<String> changePasswordRequest = new HttpEntity<>(
            "{\"currentPassword\":\"bootstrap\",\"newPassword\":\"new-password-123\"}",
            authJsonHeaders
        );

        ResponseEntity<Map> changePasswordResponse = exchangeWithRetry(
            "/api/auth/change-password",
            HttpMethod.POST,
            changePasswordRequest,
            Map.class,
            Duration.ofSeconds(5)
        );

        assertThat(changePasswordResponse.getStatusCode().value()).isEqualTo(200);
        assertThat(changePasswordResponse.getBody()).isNotNull();
        assertThat(changePasswordResponse.getBody().get("success")).isEqualTo(true);

        ResponseEntity<Map> sessionsResponse = exchangeWithRetry(
            "/api/auth/sessions",
            HttpMethod.GET,
            new HttpEntity<>(authJsonHeaders),
            Map.class,
            Duration.ofSeconds(5)
        );

        assertThat(sessionsResponse.getStatusCode().value()).isEqualTo(200);
        assertThat(sessionsResponse.getBody()).isNotNull();
        assertThat((java.util.List<?>) sessionsResponse.getBody().get("data")).isNotEmpty();
    }

    @Test
    void revokeSessionAndListDevicesWork() {
        Map<String, String> tokens = loginAndGetTokens(nextPhoneNumber(), "Device User", "Counter Tablet");
        String accessToken = tokens.get("accessToken");

        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setContentType(MediaType.APPLICATION_JSON);
        authHeaders.setBearerAuth(accessToken);

        ResponseEntity<Map> sessionsResponse = exchangeWithRetry(
            "/api/auth/sessions",
            HttpMethod.GET,
            new HttpEntity<>(authHeaders),
            Map.class,
            Duration.ofSeconds(5)
        );

        java.util.List<Map<String, Object>> sessions = (java.util.List<Map<String, Object>>) sessionsResponse.getBody().get("data");
        String sessionId = String.valueOf(sessions.get(0).get("id"));

        ResponseEntity<Map> revokeResponse = exchangeWithRetry(
            "/api/auth/sessions/" + sessionId,
            HttpMethod.DELETE,
            new HttpEntity<>(authHeaders),
            Map.class,
            Duration.ofSeconds(5)
        );

        assertThat(revokeResponse.getStatusCode().value()).isEqualTo(200);

        ResponseEntity<Map> devicesResponse = exchangeWithRetry(
            "/api/auth/devices",
            HttpMethod.GET,
            new HttpEntity<>(authHeaders),
            Map.class,
            Duration.ofSeconds(5)
        );

        assertThat(devicesResponse.getStatusCode().value()).isEqualTo(200);
        assertThat(devicesResponse.getBody()).isNotNull();
        assertThat(devicesResponse.getBody().get("data")).isNotNull();
    }

    private Map<String, String> loginAndGetTokens(String phoneNumber, String fullName, String deviceName) {
        HttpHeaders jsonHeaders = new HttpHeaders();
        jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
        jsonHeaders.set("X-Device-Name", deviceName);
        HttpEntity<String> verifyRequest = new HttpEntity<>(
            "{\"phoneNumber\":\"" + phoneNumber + "\",\"otp\":\"123456\",\"fullName\":\"" + fullName + "\"}",
            jsonHeaders
        );

        ResponseEntity<Map> verifyResponse = exchangeWithRetry(
            "/api/auth/verify-otp",
            HttpMethod.POST,
            verifyRequest,
            Map.class,
            Duration.ofSeconds(5)
        );

        Map<String, Object> verifyData = assertAndExtractVerifyData(verifyResponse);
        return Map.of(
            "accessToken", String.valueOf(verifyData.get("accessToken")),
            "refreshToken", String.valueOf(verifyData.get("refreshToken"))
        );
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> assertAndExtractVerifyData(ResponseEntity<Map> verifyResponse) {
        assertThat(verifyResponse.getStatusCode().value()).isEqualTo(200);
        assertThat(verifyResponse.getBody()).isNotNull();
        assertThat(verifyResponse.getBody().get("success")).isEqualTo(true);
        Map<String, Object> verifyData = (Map<String, Object>) verifyResponse.getBody().get("data");
        assertThat(verifyData).isNotNull();
        return verifyData;
    }

    private String nextPhoneNumber() {
        return "98" + PHONE_COUNTER.incrementAndGet();
    }

    private <T> ResponseEntity<T> exchangeWithRetry(
        String path,
        HttpMethod method,
        HttpEntity<?> request,
        Class<T> responseType,
        Duration timeout
    ) {
        Instant deadline = Instant.now().plus(timeout);
        RuntimeException lastError = null;

        while (Instant.now().isBefore(deadline)) {
            try {
                ResponseEntity<T> response = restTemplate.exchange(
                    "http://localhost:" + port + path,
                    method,
                    request,
                    responseType
                );

                if (response.getStatusCode().is5xxServerError()) {
                    lastError = new IllegalStateException("Transient 5xx response: " + response.getStatusCode());
                } else {
                    return response;
                }
            } catch (RuntimeException ex) {
                lastError = ex;
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        throw new IllegalStateException("HTTP exchange did not stabilize for path " + path, lastError);
    }
}
