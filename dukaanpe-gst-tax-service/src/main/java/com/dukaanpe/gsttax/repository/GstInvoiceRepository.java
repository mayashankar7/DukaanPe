package com.dukaanpe.gsttax.repository;

import com.dukaanpe.gsttax.entity.GstInvoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface GstInvoiceRepository extends JpaRepository<GstInvoice, Long> {

    boolean existsByStoreIdAndInvoiceNumber(Long storeId, String invoiceNumber);

    List<GstInvoice> findByStoreIdAndInvoiceDateBetween(Long storeId, LocalDate fromDate, LocalDate toDate);

    Page<GstInvoice> findByStoreIdAndInvoiceDateBetween(Long storeId, LocalDate fromDate, LocalDate toDate, Pageable pageable);
}

