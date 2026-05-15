package com.dukaanpe.syncoffline.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sync_event")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long storeId;
    private String entityType;
    private String entityId;
    private String operation;

    @Column(length = 4000)
    private String payload;

    private String dedupeKey;
    private boolean conflictDetected;
    private String conflictReason;
    private LocalDateTime createdAt;
}

