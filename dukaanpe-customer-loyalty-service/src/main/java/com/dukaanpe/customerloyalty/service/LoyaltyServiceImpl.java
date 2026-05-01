package com.dukaanpe.customerloyalty.service;

import com.dukaanpe.customerloyalty.dto.EarnLoyaltyPointsRequest;
import com.dukaanpe.customerloyalty.dto.LoyaltySummaryResponse;
import com.dukaanpe.customerloyalty.dto.LoyaltyTransactionResponse;
import com.dukaanpe.customerloyalty.dto.PagedResponse;
import com.dukaanpe.customerloyalty.dto.RedeemLoyaltyPointsRequest;
import com.dukaanpe.customerloyalty.entity.Customer;
import com.dukaanpe.customerloyalty.entity.LoyaltySettings;
import com.dukaanpe.customerloyalty.entity.LoyaltyTransaction;
import com.dukaanpe.customerloyalty.entity.LoyaltyTransactionType;
import com.dukaanpe.customerloyalty.exception.InvalidLoyaltyOperationException;
import com.dukaanpe.customerloyalty.exception.ResourceNotFoundException;
import com.dukaanpe.customerloyalty.repository.CustomerRepository;
import com.dukaanpe.customerloyalty.repository.LoyaltyTransactionRepository;
import java.math.BigDecimal;
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
public class LoyaltyServiceImpl implements LoyaltyService {

    private final CustomerRepository customerRepository;
    private final LoyaltyTransactionRepository loyaltyTransactionRepository;
    private final LoyaltySettingsService loyaltySettingsService;
    private final TierPolicyService tierPolicyService;

    @Override
    @Transactional(readOnly = true)
    public LoyaltySummaryResponse getLoyaltySummary(Long customerId) {
        Customer customer = loadActiveCustomer(customerId);
        return LoyaltySummaryResponse.builder()
            .customerId(customer.getId())
            .loyaltyPoints(customer.getLoyaltyPoints())
            .customerTier(customer.getCustomerTier())
            .availableForRedeem(customer.getLoyaltyPoints())
            .build();
    }

    @Override
    public LoyaltyTransactionResponse earnPoints(EarnLoyaltyPointsRequest request) {
        Customer customer = loadActiveCustomer(request.getCustomerId());
        LoyaltySettings settings = loyaltySettingsService.getOrCreateSettingsEntity(customer.getStoreId());
        int hundredBuckets = request.getPurchaseAmount().divide(new BigDecimal("100"), java.math.RoundingMode.DOWN).intValue();
        int earnedPoints = hundredBuckets * settings.getPointsPerHundred();
        if (earnedPoints <= 0) {
            throw new InvalidLoyaltyOperationException("Purchase amount is too low to earn points");
        }

        int runningBalance = customer.getLoyaltyPoints() + earnedPoints;
        LoyaltyTransaction saved = loyaltyTransactionRepository.save(LoyaltyTransaction.builder()
            .customerId(customer.getId())
            .storeId(customer.getStoreId())
            .transactionType(LoyaltyTransactionType.EARNED)
            .points(earnedPoints)
            .runningBalance(runningBalance)
            .referenceBillId(request.getReferenceBillId())
            .description(request.getDescription())
            .build());

        customer.setLoyaltyPoints(runningBalance);
        customer.setCustomerTier(tierPolicyService.resolveTier(customer.getStoreId(), customer.getTotalPurchases()));
        customerRepository.save(customer);

        return toResponse(saved);
    }

    @Override
    public LoyaltyTransactionResponse redeemPoints(RedeemLoyaltyPointsRequest request) {
        Customer customer = loadActiveCustomer(request.getCustomerId());
        if (request.getPoints() > customer.getLoyaltyPoints()) {
            throw new InvalidLoyaltyOperationException("Insufficient loyalty points for redemption");
        }

        int runningBalance = customer.getLoyaltyPoints() - request.getPoints();
        LoyaltyTransaction saved = loyaltyTransactionRepository.save(LoyaltyTransaction.builder()
            .customerId(customer.getId())
            .storeId(customer.getStoreId())
            .transactionType(LoyaltyTransactionType.REDEEMED)
            .points(request.getPoints())
            .runningBalance(runningBalance)
            .referenceBillId(request.getReferenceBillId())
            .description(request.getDescription())
            .build());

        customer.setLoyaltyPoints(runningBalance);
        customerRepository.save(customer);

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<LoyaltyTransactionResponse> getTransactions(Long customerId, int page, int size) {
        loadActiveCustomer(customerId);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id")));
        Page<LoyaltyTransaction> transactionPage = loyaltyTransactionRepository
            .findByCustomerIdOrderByCreatedAtDescIdDesc(customerId, pageable);

        return PagedResponse.<LoyaltyTransactionResponse>builder()
            .content(transactionPage.getContent().stream().map(this::toResponse).toList())
            .pageNumber(transactionPage.getNumber())
            .pageSize(transactionPage.getSize())
            .totalElements(transactionPage.getTotalElements())
            .totalPages(transactionPage.getTotalPages())
            .last(transactionPage.isLast())
            .build();
    }

    private Customer loadActiveCustomer(Long id) {
        return customerRepository.findByIdAndIsActiveTrue(id)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
    }

    private LoyaltyTransactionResponse toResponse(LoyaltyTransaction entity) {
        return LoyaltyTransactionResponse.builder()
            .id(entity.getId())
            .customerId(entity.getCustomerId())
            .storeId(entity.getStoreId())
            .transactionType(entity.getTransactionType())
            .points(entity.getPoints())
            .runningBalance(entity.getRunningBalance())
            .referenceBillId(entity.getReferenceBillId())
            .description(entity.getDescription())
            .createdAt(entity.getCreatedAt())
            .build();
    }
}

