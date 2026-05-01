package com.dukaanpe.customerloyalty.repository;

import com.dukaanpe.customerloyalty.entity.LoyaltySettings;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoyaltySettingsRepository extends JpaRepository<LoyaltySettings, Long> {

    Optional<LoyaltySettings> findByStoreId(Long storeId);
}

