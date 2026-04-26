package com.dukaanpe.billing.repository;

import com.dukaanpe.billing.entity.BillItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillItemRepository extends JpaRepository<BillItem, Long> {

    List<BillItem> findByBillId(Long billId);
}

