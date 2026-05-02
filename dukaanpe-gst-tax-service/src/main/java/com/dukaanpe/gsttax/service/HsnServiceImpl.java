package com.dukaanpe.gsttax.service;

import com.dukaanpe.gsttax.dto.CreateHsnRequest;
import com.dukaanpe.gsttax.dto.HsnResponse;
import com.dukaanpe.gsttax.dto.PagedResponse;
import com.dukaanpe.gsttax.dto.UpdateHsnRequest;
import com.dukaanpe.gsttax.entity.HsnMaster;
import com.dukaanpe.gsttax.exception.InvalidTaxOperationException;
import com.dukaanpe.gsttax.exception.ResourceNotFoundException;
import com.dukaanpe.gsttax.repository.HsnMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class HsnServiceImpl implements HsnService {

    private final HsnMasterRepository hsnMasterRepository;

    @Override
    public HsnResponse create(CreateHsnRequest request) {
        String normalizedCode = request.getHsnCode().trim();
        if (hsnMasterRepository.existsByHsnCode(normalizedCode)) {
            throw new InvalidTaxOperationException("HSN already exists: " + normalizedCode);
        }

        HsnMaster entity = HsnMaster.builder()
            .hsnCode(normalizedCode)
            .description(request.getDescription().trim())
            .gstRate(scale(request.getGstRate()))
            .cessRate(scale(defaultZero(request.getCessRate())))
            .isActive(true)
            .build();

        return toResponse(hsnMasterRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public HsnResponse getById(Long id) {
        return toResponse(loadHsn(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<HsnResponse> list(String hsnCode, String description, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "hsnCode"));
        Page<HsnMaster> records = hsnMasterRepository.findByHsnCodeContainingIgnoreCaseAndDescriptionContainingIgnoreCase(
            emptySafe(hsnCode),
            emptySafe(description),
            pageable
        );

        return PagedResponse.<HsnResponse>builder()
            .content(records.getContent().stream().map(this::toResponse).toList())
            .pageNumber(records.getNumber())
            .pageSize(records.getSize())
            .totalElements(records.getTotalElements())
            .totalPages(records.getTotalPages())
            .last(records.isLast())
            .build();
    }

    @Override
    public HsnResponse update(Long id, UpdateHsnRequest request) {
        HsnMaster entity = loadHsn(id);
        entity.setDescription(request.getDescription().trim());
        entity.setGstRate(scale(request.getGstRate()));
        entity.setCessRate(scale(defaultZero(request.getCessRate())));
        entity.setIsActive(request.getActive());
        return toResponse(hsnMasterRepository.save(entity));
    }

    @Override
    public void delete(Long id) {
        HsnMaster entity = loadHsn(id);
        entity.setIsActive(false);
        hsnMasterRepository.save(entity);
    }

    private HsnMaster loadHsn(Long id) {
        return hsnMasterRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("HSN not found: " + id));
    }

    private HsnResponse toResponse(HsnMaster entity) {
        return HsnResponse.builder()
            .id(entity.getId())
            .hsnCode(entity.getHsnCode())
            .description(entity.getDescription())
            .gstRate(entity.getGstRate())
            .cessRate(entity.getCessRate())
            .active(entity.getIsActive())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

    private String emptySafe(String value) {
        return value == null ? "" : value.trim();
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(2, java.math.RoundingMode.HALF_UP);
    }
}

