package com.dukaanpe.supplierpurchase.repository;

import com.dukaanpe.supplierpurchase.entity.GoodsReceivedNote;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoodsReceivedNoteRepository extends JpaRepository<GoodsReceivedNote, Long> {

    List<GoodsReceivedNote> findByStoreIdOrderByReceivedDateDescIdDesc(Long storeId);

    long countByReceivedDate(LocalDate receivedDate);
}

