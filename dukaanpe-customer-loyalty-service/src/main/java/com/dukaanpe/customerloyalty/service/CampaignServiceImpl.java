package com.dukaanpe.customerloyalty.service;

import com.dukaanpe.customerloyalty.dto.CampaignResponse;
import com.dukaanpe.customerloyalty.dto.CustomerResponse;
import com.dukaanpe.customerloyalty.dto.CreateCampaignRequest;
import com.dukaanpe.customerloyalty.dto.EligibleCustomerCountResponse;
import com.dukaanpe.customerloyalty.dto.PagedResponse;
import com.dukaanpe.customerloyalty.dto.UpdateCampaignRequest;
import com.dukaanpe.customerloyalty.entity.Campaign;
import com.dukaanpe.customerloyalty.entity.CampaignType;
import com.dukaanpe.customerloyalty.entity.Customer;
import com.dukaanpe.customerloyalty.entity.CustomerTier;
import com.dukaanpe.customerloyalty.exception.InvalidLoyaltyOperationException;
import com.dukaanpe.customerloyalty.exception.ResourceNotFoundException;
import com.dukaanpe.customerloyalty.repository.CampaignRepository;
import com.dukaanpe.customerloyalty.repository.CustomerRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
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
public class CampaignServiceImpl implements CampaignService {

    private final CampaignRepository campaignRepository;
    private final CustomerRepository customerRepository;

    @Override
    public CampaignResponse createCampaign(CreateCampaignRequest request) {
        validateDates(request.getStartDate(), request.getEndDate());
        Campaign entity = Campaign.builder()
            .storeId(request.getStoreId())
            .campaignName(request.getCampaignName().trim())
            .campaignType(request.getCampaignType())
            .description(request.getDescription())
            .discountPercent(request.getDiscountPercent())
            .discountAmount(request.getDiscountAmount())
            .minPurchaseAmount(request.getMinPurchaseAmount())
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .targetTier(request.getTargetTier().trim())
            .messageTemplate(request.getMessageTemplate())
            .build();

        return toResponse(campaignRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CampaignResponse> listCampaigns(Long storeId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Campaign> campaigns = campaignRepository.findByStoreIdAndIsActiveTrue(storeId, pageable);
        return PagedResponse.<CampaignResponse>builder()
            .content(campaigns.getContent().stream().map(this::toResponse).toList())
            .pageNumber(campaigns.getNumber())
            .pageSize(campaigns.getSize())
            .totalElements(campaigns.getTotalElements())
            .totalPages(campaigns.getTotalPages())
            .last(campaigns.isLast())
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CampaignResponse getCampaign(Long id) {
        return toResponse(loadActiveCampaign(id));
    }

    @Override
    public CampaignResponse updateCampaign(Long id, UpdateCampaignRequest request) {
        Campaign campaign = loadActiveCampaign(id);
        validateDates(request.getStartDate(), request.getEndDate());

        campaign.setCampaignName(request.getCampaignName().trim());
        campaign.setCampaignType(request.getCampaignType());
        campaign.setDescription(request.getDescription());
        campaign.setDiscountPercent(request.getDiscountPercent());
        campaign.setDiscountAmount(request.getDiscountAmount());
        campaign.setMinPurchaseAmount(request.getMinPurchaseAmount());
        campaign.setStartDate(request.getStartDate());
        campaign.setEndDate(request.getEndDate());
        campaign.setTargetTier(request.getTargetTier().trim());
        campaign.setMessageTemplate(request.getMessageTemplate());

        return toResponse(campaignRepository.save(campaign));
    }

    @Override
    public void deactivateCampaign(Long id) {
        Campaign campaign = loadActiveCampaign(id);
        campaign.setIsActive(false);
        campaignRepository.save(campaign);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampaignResponse> listActiveCampaigns(Long storeId) {
        LocalDate today = LocalDate.now();
        return campaignRepository
            .findByStoreIdAndIsActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(storeId, today, today)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CustomerResponse> previewEligibleCustomers(Long id, int page, int size) {
        Campaign campaign = loadActiveCampaign(id);
        List<CustomerTier> targetTiers = parseTargetTiers(campaign.getTargetTier());
        LocalDate today = LocalDate.now();
        boolean birthdayCampaign = campaign.getCampaignType() == CampaignType.BIRTHDAY;

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "totalPurchases").and(Sort.by("id")));
        Page<Customer> customerPage = customerRepository.findEligibleCustomers(
            campaign.getStoreId(),
            targetTiers,
            campaign.getMinPurchaseAmount(),
            birthdayCampaign,
            today.getMonthValue(),
            today.getDayOfMonth(),
            pageable
        );

        return PagedResponse.<CustomerResponse>builder()
            .content(customerPage.getContent().stream().map(this::toCustomerResponse).toList())
            .pageNumber(customerPage.getNumber())
            .pageSize(customerPage.getSize())
            .totalElements(customerPage.getTotalElements())
            .totalPages(customerPage.getTotalPages())
            .last(customerPage.isLast())
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public EligibleCustomerCountResponse countEligibleCustomers(Long id) {
        Campaign campaign = loadActiveCampaign(id);
        List<CustomerTier> targetTiers = parseTargetTiers(campaign.getTargetTier());
        LocalDate today = LocalDate.now();
        boolean birthdayCampaign = campaign.getCampaignType() == CampaignType.BIRTHDAY;

        long count = customerRepository.countEligibleCustomers(
            campaign.getStoreId(),
            targetTiers,
            campaign.getMinPurchaseAmount(),
            birthdayCampaign,
            today.getMonthValue(),
            today.getDayOfMonth()
        );

        return EligibleCustomerCountResponse.builder()
            .count(count)
            .build();
    }

    private Campaign loadActiveCampaign(Long id) {
        return campaignRepository.findById(id)
            .filter(Campaign::getIsActive)
            .orElseThrow(() -> new ResourceNotFoundException("Campaign not found: " + id));
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new InvalidLoyaltyOperationException("endDate must be on or after startDate");
        }
    }

    private List<CustomerTier> parseTargetTiers(String targetTier) {
        if (targetTier == null || targetTier.isBlank()) {
            return Arrays.asList(CustomerTier.values());
        }

        String[] tokens = targetTier.split(",");
        boolean hasAll = Arrays.stream(tokens)
            .map(this::normalize)
            .filter(token -> token != null)
            .anyMatch(token -> token.equals("ALL"));
        if (hasAll) {
            return Arrays.asList(CustomerTier.values());
        }

        List<CustomerTier> tiers = Arrays.stream(tokens)
            .map(this::normalize)
            .filter(token -> token != null)
            .map(this::parseTier)
            .distinct()
            .toList();

        if (tiers.isEmpty()) {
            throw new InvalidLoyaltyOperationException("Campaign targetTier must include ALL or valid customer tiers");
        }
        return tiers;
    }

    private CustomerTier parseTier(String tierToken) {
        try {
            return CustomerTier.valueOf(tierToken);
        } catch (IllegalArgumentException ex) {
            throw new InvalidLoyaltyOperationException("Invalid tier value in campaign targetTier: " + tierToken);
        }
    }

    private String normalize(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        return token.trim().toUpperCase(Locale.ROOT);
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

    private CampaignResponse toResponse(Campaign entity) {
        return CampaignResponse.builder()
            .id(entity.getId())
            .storeId(entity.getStoreId())
            .campaignName(entity.getCampaignName())
            .campaignType(entity.getCampaignType())
            .description(entity.getDescription())
            .discountPercent(entity.getDiscountPercent())
            .discountAmount(entity.getDiscountAmount())
            .minPurchaseAmount(entity.getMinPurchaseAmount())
            .startDate(entity.getStartDate())
            .endDate(entity.getEndDate())
            .targetTier(entity.getTargetTier())
            .messageTemplate(entity.getMessageTemplate())
            .isActive(entity.getIsActive())
            .totalCustomersTargeted(entity.getTotalCustomersTargeted())
            .totalRedemptions(entity.getTotalRedemptions())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }
}

