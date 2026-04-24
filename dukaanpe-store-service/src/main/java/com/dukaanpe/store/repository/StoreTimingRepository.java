package com.dukaanpe.store.repository;

import com.dukaanpe.store.entity.Store;
import com.dukaanpe.store.entity.StoreTiming;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreTimingRepository extends JpaRepository<StoreTiming, Long> {

    List<StoreTiming> findByStoreOrderByDayOfWeekAsc(Store store);

    void deleteByStore(Store store);
}

