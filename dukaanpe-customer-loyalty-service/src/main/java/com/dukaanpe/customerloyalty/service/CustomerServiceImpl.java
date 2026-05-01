package com.dukaanpe.customerloyalty.service;

import com.dukaanpe.customerloyalty.dto.CreateCustomerRequest;
import com.dukaanpe.customerloyalty.dto.CustomerResponse;
import com.dukaanpe.customerloyalty.dto.PagedResponse;
import com.dukaanpe.customerloyalty.dto.PurchaseHistoryResponse;
import com.dukaanpe.customerloyalty.dto.RecordPurchaseRequest;
import com.dukaanpe.customerloyalty.dto.UpdateCustomerRequest;
import com.dukaanpe.customerloyalty.entity.Customer;
import com.dukaanpe.customerloyalty.entity.CustomerTier;
import com.dukaanpe.customerloyalty.entity.PurchaseHistory;
import com.dukaanpe.customerloyalty.exception.InvalidLoyaltyOperationException;
import com.dukaanpe.customerloyalty.exception.ResourceNotFoundException;
import com.dukaanpe.customerloyalty.repository.CustomerRepository;
import com.dukaanpe.customerloyalty.repository.PurchaseHistoryRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final PurchaseHistoryRepository purchaseHistoryRepository;
    private final TierPolicyService tierPolicyService;

    @Override
    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        customerRepository.findByStoreIdAndPhoneAndIsActiveTrue(request.getStoreId(), request.getPhone())
            .ifPresent(existing -> {
                throw new InvalidLoyaltyOperationException("Customer with this phone already exists in the store");
            });

        Customer entity = Customer.builder()
            .storeId(request.getStoreId())
            .customerName(request.getCustomerName().trim())
            .phone(request.getPhone())
            .email(request.getEmail())
            .address(request.getAddress())
            .dateOfBirth(request.getDateOfBirth())
            .anniversaryDate(request.getAnniversaryDate())
            .tags(request.getTags())
            .build();

        return toCustomerResponse(customerRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CustomerResponse> listCustomers(Long storeId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<Customer> customerPage = customerRepository.findByStoreIdAndIsActiveTrue(storeId, pageable);
        return toPagedCustomerResponse(customerPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CustomerResponse> searchCustomers(
        Long storeId,
        String query,
        String tier,
        BigDecimal minPurchases,
        LocalDate lastVisitFrom,
        LocalDate lastVisitTo,
        int page,
        int size
    ) {
        String normalizedQuery = normalize(query);
        String normalizedTier = normalize(tier);

        if (normalizedQuery == null && normalizedTier == null && minPurchases == null && lastVisitFrom == null && lastVisitTo == null) {
            throw new InvalidLoyaltyOperationException("Provide at least one search filter: q, tier, minPurchases, lastVisitFrom, or lastVisitTo");
        }
        if (lastVisitFrom != null && lastVisitTo != null && lastVisitFrom.isAfter(lastVisitTo)) {
            throw new InvalidLoyaltyOperationException("lastVisitFrom must be on or before lastVisitTo");
        }

        CustomerTier tierFilter = parseTier(normalizedTier);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<Customer> customerPage = customerRepository.searchByStoreAndFilters(
            storeId,
            normalizedQuery,
            tierFilter,
            minPurchases,
            lastVisitFrom,
            lastVisitTo,
            pageable
        );
        return toPagedCustomerResponse(customerPage);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse findByPhone(Long storeId, String phone) {
        return customerRepository.findByStoreIdAndPhoneAndIsActiveTrue(storeId, phone)
            .map(this::toCustomerResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found for phone: " + phone));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponse> topCustomers(Long storeId, int limit) {
        return customerRepository
            .findByStoreIdAndIsActiveTrueOrderByTotalPurchasesDescIdAsc(storeId, PageRequest.of(0, limit))
            .stream()
            .map(this::toCustomerResponse)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomer(Long id) {
        return toCustomerResponse(loadActiveCustomer(id));
    }

    @Override
    public CustomerResponse updateCustomer(Long id, UpdateCustomerRequest request) {
        Customer customer = loadActiveCustomer(id);
        customerRepository.findByStoreIdAndPhoneAndIsActiveTrue(customer.getStoreId(), request.getPhone())
            .filter(found -> !found.getId().equals(id))
            .ifPresent(found -> {
                throw new InvalidLoyaltyOperationException("Customer with this phone already exists in the store");
            });

        customer.setCustomerName(request.getCustomerName().trim());
        customer.setPhone(request.getPhone());
        customer.setEmail(request.getEmail());
        customer.setAddress(request.getAddress());
        customer.setDateOfBirth(request.getDateOfBirth());
        customer.setAnniversaryDate(request.getAnniversaryDate());
        customer.setTags(request.getTags());

        return toCustomerResponse(customerRepository.save(customer));
    }

    @Override
    public void deactivateCustomer(Long id) {
        Customer customer = loadActiveCustomer(id);
        customer.setIsActive(false);
        customerRepository.save(customer);
    }

    @Override
    public PurchaseHistoryResponse recordPurchase(Long customerId, RecordPurchaseRequest request) {
        Customer customer = loadActiveCustomer(customerId);

        PurchaseHistory history = PurchaseHistory.builder()
            .customerId(customer.getId())
            .storeId(customer.getStoreId())
            .billId(request.getBillId())
            .billNumber(request.getBillNumber())
            .billDate(request.getBillDate())
            .totalAmount(request.getTotalAmount())
            .itemsSummary(request.getItemsSummary())
            .paymentMode(request.getPaymentMode())
            .build();

        PurchaseHistory saved = purchaseHistoryRepository.save(history);

        customer.setTotalPurchases(customer.getTotalPurchases().add(request.getTotalAmount()));
        customer.setTotalVisits(customer.getTotalVisits() + 1);
        customer.setLastVisitDate(LocalDate.now());
        customer.setCustomerTier(tierPolicyService.resolveTier(customer.getStoreId(), customer.getTotalPurchases()));
        customerRepository.save(customer);

        return toPurchaseHistoryResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<PurchaseHistoryResponse> getPurchaseHistory(Long customerId, int page, int size) {
        loadActiveCustomer(customerId);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "billDate").and(Sort.by(Sort.Direction.DESC, "id")));
        Page<PurchaseHistory> historyPage = purchaseHistoryRepository.findByCustomerIdOrderByBillDateDescIdDesc(customerId, pageable);

        return PagedResponse.<PurchaseHistoryResponse>builder()
            .content(historyPage.getContent().stream().map(this::toPurchaseHistoryResponse).toList())
            .pageNumber(historyPage.getNumber())
            .pageSize(historyPage.getSize())
            .totalElements(historyPage.getTotalElements())
            .totalPages(historyPage.getTotalPages())
            .last(historyPage.isLast())
            .build();
    }

    private Customer loadActiveCustomer(Long id) {
        return customerRepository.findByIdAndIsActiveTrue(id)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
    }

    private PagedResponse<CustomerResponse> toPagedCustomerResponse(Page<Customer> customerPage) {
        return PagedResponse.<CustomerResponse>builder()
            .content(customerPage.getContent().stream().map(this::toCustomerResponse).toList())
            .pageNumber(customerPage.getNumber())
            .pageSize(customerPage.getSize())
            .totalElements(customerPage.getTotalElements())
            .totalPages(customerPage.getTotalPages())
            .last(customerPage.isLast())
            .build();
    }

    private CustomerTier parseTier(String tier) {
        if (tier == null) {
            return null;
        }
        try {
            return CustomerTier.valueOf(tier.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvalidLoyaltyOperationException("Invalid tier value: " + tier);
        }
    }

    private String normalize(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        return input.trim();
    }

    private CustomerResponse toCustomerResponse(Customer entity) {
        return CustomerResponse.builder()
            .id(entity.getId())
            .storeId(entity.getStoreId())
            .customerName(entity.getCustomerName())
            .phone(entity.getPhone())
            .email(entity.getEmail())
            .address(entity.getAddress())
            .dateOfBirth(entity.getDateOfBirth())
            .anniversaryDate(entity.getAnniversaryDate())
            .totalPurchases(entity.getTotalPurchases())
            .totalVisits(entity.getTotalVisits())
            .lastVisitDate(entity.getLastVisitDate())
            .loyaltyPoints(entity.getLoyaltyPoints())
            .customerTier(entity.getCustomerTier())
            .tags(entity.getTags())
            .isActive(entity.getIsActive())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

    private PurchaseHistoryResponse toPurchaseHistoryResponse(PurchaseHistory entity) {
        return PurchaseHistoryResponse.builder()
            .id(entity.getId())
            .customerId(entity.getCustomerId())
            .storeId(entity.getStoreId())
            .billId(entity.getBillId())
            .billNumber(entity.getBillNumber())
            .billDate(entity.getBillDate())
            .totalAmount(entity.getTotalAmount())
            .itemsSummary(entity.getItemsSummary())
            .paymentMode(entity.getPaymentMode())
            .build();
    }
}

