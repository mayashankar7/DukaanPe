package com.dukaanpe.auth.service;

import com.dukaanpe.auth.config.JwtConfig;
import com.dukaanpe.auth.entity.OtpRecord;
import com.dukaanpe.auth.exception.InvalidOtpException;
import com.dukaanpe.auth.exception.OtpExpiredException;
import com.dukaanpe.auth.repository.OtpRecordRepository;
import java.time.LocalDateTime;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpServiceImpl implements OtpService {

    private final OtpRecordRepository otpRecordRepository;
    private final JwtConfig jwtConfig;

    @Override
    @Transactional
    public void sendOtp(String phoneNumber) {
        String generatedOtp = String.format("%06d", new Random().nextInt(1_000_000));
        OtpRecord record = OtpRecord.builder()
            .phoneNumber(phoneNumber)
            .otpCode(generatedOtp)
            .expiryTime(LocalDateTime.now().plusMinutes(5))
            .isUsed(false)
            .build();
        otpRecordRepository.save(record);

        // Keep OTP values out of non-dev logs.
        if (Boolean.TRUE.equals(jwtConfig.getAllowFixedOtp())) {
            log.info("OTP for phone {} is {}", phoneNumber, generatedOtp);
        } else {
            log.info("OTP issued for phone {}", phoneNumber);
        }
    }

    @Override
    @Transactional
    public void verifyOtp(String phoneNumber, String otp) {
        if (Boolean.TRUE.equals(jwtConfig.getAllowFixedOtp()) && "123456".equals(otp)) {
            return;
        }

        OtpRecord latestOtp = otpRecordRepository.findTopByPhoneNumberAndIsUsedFalseOrderByCreatedAtDesc(phoneNumber)
            .orElseThrow(() -> new InvalidOtpException("No active OTP found for this phone number"));

        if (latestOtp.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new OtpExpiredException("OTP has expired. Please request a new OTP.");
        }

        if (!latestOtp.getOtpCode().equals(otp)) {
            throw new InvalidOtpException("Invalid OTP provided");
        }

        latestOtp.setIsUsed(true);
        otpRecordRepository.save(latestOtp);
    }
}

