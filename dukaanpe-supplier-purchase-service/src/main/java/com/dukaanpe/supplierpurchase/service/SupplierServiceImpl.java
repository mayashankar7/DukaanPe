package com.dukaanpe.supplierpurchase.service;

import com.dukaanpe.supplierpurchase.dto.SupplierRequest;
import com.dukaanpe.supplierpurchase.dto.SupplierResponse;
import com.dukaanpe.supplierpurchase.entity.Supplier;
import com.dukaanpe.supplierpurchase.exception.ResourceNotFoundException;
import com.dukaanpe.supplierpurchase.repository.SupplierRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;

    @Override
    @Transactional
    public SupplierResponse createSupplier(SupplierRequest request) {
        Supplier supplier = mapRequest(new Supplier(), request);
        return toResponse(supplierRepository.save(supplier));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierResponse> listSuppliers(Long storeId) {
        return supplierRepository.findByStoreIdAndIsActiveTrueOrderBySupplierNameAsc(storeId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierResponse getSupplier(Long id) {
        return toResponse(getActiveSupplier(id));
    }

    @Override
    @Transactional
    public SupplierResponse updateSupplier(Long id, SupplierRequest request) {
        Supplier supplier = getActiveSupplier(id);
        mapRequest(supplier, request);
        return toResponse(supplierRepository.save(supplier));
    }

    @Override
    @Transactional
    public void deactivateSupplier(Long id) {
        Supplier supplier = getActiveSupplier(id);
        supplier.setIsActive(false);
        supplierRepository.save(supplier);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierResponse> searchSuppliers(Long storeId, String q) {
        String query = q == null ? "" : q.trim();
        if (query.isEmpty()) {
            return listSuppliers(storeId);
        }
        return supplierRepository.findByStoreIdAndIsActiveTrueAndSupplierNameContainingIgnoreCaseOrderBySupplierNameAsc(storeId, query)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    private Supplier getActiveSupplier(Long id) {
        Supplier supplier = supplierRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
        if (!Boolean.TRUE.equals(supplier.getIsActive())) {
            throw new ResourceNotFoundException("Supplier is inactive with id: " + id);
        }
        return supplier;
    }

    private Supplier mapRequest(Supplier supplier, SupplierRequest request) {
        supplier.setStoreId(request.getStoreId());
        supplier.setSupplierName(request.getSupplierName());
        supplier.setContactPerson(request.getContactPerson());
        supplier.setPhone(request.getPhone());
        supplier.setAlternatePhone(request.getAlternatePhone());
        supplier.setEmail(request.getEmail());
        supplier.setAddress(request.getAddress());
        supplier.setCity(request.getCity());
        supplier.setState(request.getState());
        supplier.setPincode(request.getPincode());
        supplier.setGstin(request.getGstin());
        supplier.setCategoriesSupplied(request.getCategoriesSupplied());
        supplier.setPaymentTerms(request.getPaymentTerms());
        supplier.setBankAccountNumber(request.getBankAccountNumber());
        supplier.setBankIfsc(request.getBankIfsc());
        supplier.setBankName(request.getBankName());
        supplier.setRating(request.getRating());
        if (supplier.getIsActive() == null) {
            supplier.setIsActive(true);
        }
        return supplier;
    }

    private SupplierResponse toResponse(Supplier supplier) {
        return SupplierResponse.builder()
            .id(supplier.getId())
            .storeId(supplier.getStoreId())
            .supplierName(supplier.getSupplierName())
            .contactPerson(supplier.getContactPerson())
            .phone(supplier.getPhone())
            .alternatePhone(supplier.getAlternatePhone())
            .email(supplier.getEmail())
            .address(supplier.getAddress())
            .city(supplier.getCity())
            .state(supplier.getState())
            .pincode(supplier.getPincode())
            .gstin(supplier.getGstin())
            .categoriesSupplied(supplier.getCategoriesSupplied())
            .paymentTerms(supplier.getPaymentTerms())
            .bankAccountNumber(supplier.getBankAccountNumber())
            .bankIfsc(supplier.getBankIfsc())
            .bankName(supplier.getBankName())
            .rating(supplier.getRating())
            .isActive(supplier.getIsActive())
            .createdAt(supplier.getCreatedAt())
            .updatedAt(supplier.getUpdatedAt())
            .build();
    }
}

