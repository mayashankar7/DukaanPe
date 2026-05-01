package com.dukaanpe.customerloyalty.repository;

import com.dukaanpe.customerloyalty.entity.Campaign;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    Page<Campaign> findByStoreIdAndIsActiveTrue(Long storeId, Pageable pageable);

    List<Campaign> findByStoreIdAndIsActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
        Long storeId,
        LocalDate from,
        LocalDate to
    );
}

