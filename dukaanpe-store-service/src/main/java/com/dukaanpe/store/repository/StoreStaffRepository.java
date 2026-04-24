package com.dukaanpe.store.repository;

import com.dukaanpe.store.entity.Store;
import com.dukaanpe.store.entity.StoreStaff;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreStaffRepository extends JpaRepository<StoreStaff, Long> {

    List<StoreStaff> findByStoreAndIsActiveTrue(Store store);
}

