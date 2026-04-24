package com.dukaanpe.store.repository;

import com.dukaanpe.store.entity.Store;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, Long> {

    List<Store> findByOwnerPhoneAndIsActiveTrue(String ownerPhone);
}

