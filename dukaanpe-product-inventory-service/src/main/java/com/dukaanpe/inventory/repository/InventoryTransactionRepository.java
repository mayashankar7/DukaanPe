package com.dukaanpe.inventory.repository;

import com.dukaanpe.inventory.entity.InventoryTransaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {

    List<InventoryTransaction> findByProductIdOrderByCreatedAtDesc(Long productId);

    List<InventoryTransaction> findByStoreIdAndProductIdOrderByCreatedAtDesc(Long storeId, Long productId);
}

