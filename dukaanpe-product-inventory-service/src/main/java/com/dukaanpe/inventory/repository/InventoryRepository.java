package com.dukaanpe.inventory.repository;

import com.dukaanpe.inventory.entity.Inventory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    List<Inventory> findByStoreId(Long storeId);

    Optional<Inventory> findByProductId(Long productId);

    List<Inventory> findByStoreIdAndCurrentStockLessThanEqual(Long storeId, Double threshold);
}

