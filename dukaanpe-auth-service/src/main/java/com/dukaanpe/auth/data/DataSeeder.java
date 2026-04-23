package com.dukaanpe.auth.data;

import com.dukaanpe.auth.entity.User;
import com.dukaanpe.auth.entity.UserRole;
import com.dukaanpe.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) {
        seedUser("9876543210", "Rajesh Kumar", UserRole.OWNER);
        seedUser("9876543211", "Priya Sharma", UserRole.MANAGER);
        seedUser("9876543212", "Amit Singh", UserRole.STAFF);
    }

    private void seedUser(String phone, String fullName, UserRole role) {
        userRepository.findByPhoneNumber(phone).orElseGet(() ->
            userRepository.save(User.builder()
                .phoneNumber(phone)
                .fullName(fullName)
                .role(role)
                .isActive(true)
                .build())
        );
    }
}

