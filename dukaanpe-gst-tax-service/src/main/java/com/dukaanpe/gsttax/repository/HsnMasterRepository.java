package com.dukaanpe.gsttax.repository;

import com.dukaanpe.gsttax.entity.HsnMaster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HsnMasterRepository extends JpaRepository<HsnMaster, Long> {

    Optional<HsnMaster> findByHsnCode(String hsnCode);

    boolean existsByHsnCode(String hsnCode);

    Page<HsnMaster> findByHsnCodeContainingIgnoreCaseAndDescriptionContainingIgnoreCase(
        String hsnCode,
        String description,
        Pageable pageable
    );
}

