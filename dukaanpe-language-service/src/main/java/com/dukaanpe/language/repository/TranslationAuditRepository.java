package com.dukaanpe.language.repository;

import com.dukaanpe.language.entity.TranslationAuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TranslationAuditRepository extends JpaRepository<TranslationAuditEntity, Long> {
}

