package com.dukaanpe.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "upi_qr_codes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpiQrCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "merchant_upi_id", nullable = false)
    private String merchantUpiId;

    @Column(name = "merchant_name")
    private String merchantName;

    @Column(name = "qr_code_image_base64", length = 4000)
    private String qrCodeImageBase64;

    @Column(name = "is_default")
    private Boolean isDefault;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        if (isDefault == null) {
            isDefault = false;
        }
    }
}

