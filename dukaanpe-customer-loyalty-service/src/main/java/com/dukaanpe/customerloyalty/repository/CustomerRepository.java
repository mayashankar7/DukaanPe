package com.dukaanpe.customerloyalty.repository;

import com.dukaanpe.customerloyalty.entity.Customer;
import com.dukaanpe.customerloyalty.entity.CustomerTier;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Page<Customer> findByStoreIdAndIsActiveTrue(Long storeId, Pageable pageable);

    Optional<Customer> findByIdAndIsActiveTrue(Long id);

    Optional<Customer> findByStoreIdAndPhoneAndIsActiveTrue(Long storeId, String phone);

    @Query("""
        select c from Customer c
        where c.storeId = :storeId
          and c.isActive = true
          and (
            :q is null
            or lower(c.customerName) like lower(concat('%', :q, '%'))
            or c.phone like concat('%', :q, '%')
          )
          and (:tier is null or c.customerTier = :tier)
          and (:minPurchases is null or c.totalPurchases >= :minPurchases)
          and (:lastVisitFrom is null or c.lastVisitDate >= :lastVisitFrom)
          and (:lastVisitTo is null or c.lastVisitDate <= :lastVisitTo)
        """)
    Page<Customer> searchByStoreAndFilters(
        @Param("storeId") Long storeId,
        @Param("q") String q,
        @Param("tier") CustomerTier tier,
        @Param("minPurchases") BigDecimal minPurchases,
        @Param("lastVisitFrom") LocalDate lastVisitFrom,
        @Param("lastVisitTo") LocalDate lastVisitTo,
        Pageable pageable
    );

    List<Customer> findByStoreIdAndIsActiveTrueOrderByTotalPurchasesDescIdAsc(Long storeId, Pageable pageable);

    @Query("""
        select c from Customer c
        where c.storeId = :storeId
          and c.isActive = true
          and c.customerTier in :tiers
          and (:minPurchases is null or c.totalPurchases >= :minPurchases)
          and (
            :birthdayCampaign = false
            or (
              c.dateOfBirth is not null
              and function('month', c.dateOfBirth) = :todayMonth
              and function('day', c.dateOfBirth) = :todayDay
            )
          )
        """)
    Page<Customer> findEligibleCustomers(
        @Param("storeId") Long storeId,
        @Param("tiers") List<CustomerTier> tiers,
        @Param("minPurchases") BigDecimal minPurchases,
        @Param("birthdayCampaign") boolean birthdayCampaign,
        @Param("todayMonth") int todayMonth,
        @Param("todayDay") int todayDay,
        Pageable pageable
    );

    @Query("""
        select count(c) from Customer c
        where c.storeId = :storeId
          and c.isActive = true
          and c.customerTier in :tiers
          and (:minPurchases is null or c.totalPurchases >= :minPurchases)
          and (
            :birthdayCampaign = false
            or (
              c.dateOfBirth is not null
              and function('month', c.dateOfBirth) = :todayMonth
              and function('day', c.dateOfBirth) = :todayDay
            )
          )
        """)
    long countEligibleCustomers(
        @Param("storeId") Long storeId,
        @Param("tiers") List<CustomerTier> tiers,
        @Param("minPurchases") BigDecimal minPurchases,
        @Param("birthdayCampaign") boolean birthdayCampaign,
        @Param("todayMonth") int todayMonth,
        @Param("todayDay") int todayDay
    );
}
