package com.dukaanpe.store.data;

import com.dukaanpe.store.entity.BusinessCategory;
import com.dukaanpe.store.entity.Store;
import com.dukaanpe.store.entity.StoreDayOfWeek;
import com.dukaanpe.store.entity.StoreStaff;
import com.dukaanpe.store.entity.StoreStaffRole;
import com.dukaanpe.store.entity.StoreTiming;
import com.dukaanpe.store.entity.SubscriptionPlan;
import com.dukaanpe.store.repository.StoreRepository;
import com.dukaanpe.store.repository.StoreStaffRepository;
import com.dukaanpe.store.repository.StoreTimingRepository;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final StoreRepository storeRepository;
    private final StoreTimingRepository storeTimingRepository;
    private final StoreStaffRepository storeStaffRepository;

    @Override
    public void run(String... args) {
        if (storeRepository.count() > 0) {
            return;
        }

        Store rajeshStore = storeRepository.save(Store.builder()
            .ownerPhone("9876543210")
            .storeName("Rajesh General Store")
            .businessCategory(BusinessCategory.GROCERY)
            .city("Pune")
            .state("Maharashtra")
            .pincode("411001")
            .phone("9876543210")
            .gstin("27AADCB2230M1Z5")
            .subscriptionPlan(SubscriptionPlan.FREE)
            .isActive(true)
            .build());

        Store sharmaStore = storeRepository.save(Store.builder()
            .ownerPhone("9876543211")
            .storeName("Sharma Medical Store")
            .businessCategory(BusinessCategory.MEDICAL)
            .city("Delhi")
            .state("Delhi")
            .pincode("110001")
            .phone("9876543211")
            .drugLicense("DL-20B-12345")
            .subscriptionPlan(SubscriptionPlan.BASIC)
            .isActive(true)
            .build());

        seedDefaultTimings(rajeshStore);
        seedDefaultTimings(sharmaStore);

        storeStaffRepository.save(StoreStaff.builder()
            .store(rajeshStore)
            .staffPhone("9876543220")
            .staffName("Vikram Patil")
            .role(StoreStaffRole.MANAGER)
            .isActive(true)
            .build());

        storeStaffRepository.save(StoreStaff.builder()
            .store(sharmaStore)
            .staffPhone("9876543221")
            .staffName("Anita Verma")
            .role(StoreStaffRole.CASHIER)
            .isActive(true)
            .build());
    }

    private void seedDefaultTimings(Store store) {
        List<StoreTiming> timings = List.of(
            timing(store, StoreDayOfWeek.MON),
            timing(store, StoreDayOfWeek.TUE),
            timing(store, StoreDayOfWeek.WED),
            timing(store, StoreDayOfWeek.THU),
            timing(store, StoreDayOfWeek.FRI),
            timing(store, StoreDayOfWeek.SAT),
            timing(store, StoreDayOfWeek.SUN)
        );
        storeTimingRepository.saveAll(timings);
    }

    private StoreTiming timing(Store store, StoreDayOfWeek day) {
        return StoreTiming.builder()
            .store(store)
            .dayOfWeek(day)
            .openTime(LocalTime.of(9, 0))
            .closeTime(LocalTime.of(21, 0))
            .isClosed(false)
            .build();
    }
}

