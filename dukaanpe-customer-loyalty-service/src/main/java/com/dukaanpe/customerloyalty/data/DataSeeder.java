package com.dukaanpe.customerloyalty.data;

import com.dukaanpe.customerloyalty.entity.Campaign;
import com.dukaanpe.customerloyalty.entity.CampaignType;
import com.dukaanpe.customerloyalty.entity.Customer;
import com.dukaanpe.customerloyalty.entity.CustomerTier;
import com.dukaanpe.customerloyalty.entity.LoyaltySettings;
import com.dukaanpe.customerloyalty.repository.CampaignRepository;
import com.dukaanpe.customerloyalty.repository.CustomerRepository;
import com.dukaanpe.customerloyalty.repository.LoyaltySettingsRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CustomerRepository customerRepository;
    private final LoyaltySettingsRepository loyaltySettingsRepository;
    private final CampaignRepository campaignRepository;

    @Override
    public void run(String... args) {
        if (customerRepository.count() == 0) {
            seedCustomers();
        }

        if (loyaltySettingsRepository.count() == 0) {
            loyaltySettingsRepository.save(LoyaltySettings.builder()
                .storeId(1L)
                .pointsPerHundred(1)
                .pointsToRedeemUnit(100)
                .redeemValueRupees(10)
                .silverThreshold(new BigDecimal("5000"))
                .goldThreshold(new BigDecimal("15000"))
                .platinumThreshold(new BigDecimal("50000"))
                .build());
        }

        if (campaignRepository.count() == 0) {
            campaignRepository.save(Campaign.builder()
                .storeId(1L)
                .campaignName("Weekend Saver")
                .campaignType(CampaignType.SEASONAL)
                .description("Extra discount for loyal customers")
                .discountPercent(new BigDecimal("5.00"))
                .minPurchaseAmount(new BigDecimal("500.00"))
                .startDate(LocalDate.now().minusDays(1))
                .endDate(LocalDate.now().plusDays(10))
                .targetTier("SILVER,GOLD,PLATINUM")
                .messageTemplate("Weekend offer unlocked for you")
                .build());
        }
    }

    private void seedCustomers() {
        customerRepository.save(Customer.builder()
            .storeId(1L)
            .customerName("Ramesh Patil")
            .phone("9876500001")
            .email("ramesh@example.com")
            .address("Kothrud, Pune")
            .dateOfBirth(LocalDate.of(1989, 7, 10))
            .totalPurchases(new BigDecimal("4200.00"))
            .totalVisits(14)
            .lastVisitDate(LocalDate.now().minusDays(2))
            .loyaltyPoints(42)
            .customerTier(CustomerTier.REGULAR)
            .tags("frequent")
            .build());

        customerRepository.save(Customer.builder()
            .storeId(1L)
            .customerName("Sunita Verma")
            .phone("9876500002")
            .email("sunita@example.com")
            .address("Karve Nagar, Pune")
            .dateOfBirth(LocalDate.of(1991, 11, 21))
            .totalPurchases(new BigDecimal("16800.00"))
            .totalVisits(32)
            .lastVisitDate(LocalDate.now().minusDays(1))
            .loyaltyPoints(168)
            .customerTier(CustomerTier.GOLD)
            .tags("bulk-buyer")
            .build());

        customerRepository.save(Customer.builder()
            .storeId(2L)
            .customerName("Amit Saini")
            .phone("9876500003")
            .address("Dwarka, Delhi")
            .totalPurchases(new BigDecimal("62000.00"))
            .totalVisits(57)
            .lastVisitDate(LocalDate.now().minusDays(5))
            .loyaltyPoints(620)
            .customerTier(CustomerTier.PLATINUM)
            .tags("premium")
            .build());
    }
}
