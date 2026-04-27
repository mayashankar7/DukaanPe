package com.dukaanpe.udhar.repository;

import com.dukaanpe.udhar.entity.KhataCustomer;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KhataCustomerRepository extends JpaRepository<KhataCustomer, Long> {

    Page<KhataCustomer> findByStoreIdAndIsActiveTrue(Long storeId, Pageable pageable);

    List<KhataCustomer> findByStoreIdAndIsActiveTrue(Long storeId);

    Page<KhataCustomer> findByStoreIdAndIsActiveTrueAndCustomerNameContainingIgnoreCaseOrStoreIdAndIsActiveTrueAndCustomerPhoneContainingIgnoreCase(
        Long storeIdName,
        String name,
        Long storeIdPhone,
        String phone,
        Pageable pageable
    );

    Optional<KhataCustomer> findByStoreIdAndCustomerPhoneAndIsActiveTrue(Long storeId, String customerPhone);

    List<KhataCustomer> findTop10ByStoreIdAndIsActiveTrueOrderByTotalOutstandingDesc(Long storeId);
}

