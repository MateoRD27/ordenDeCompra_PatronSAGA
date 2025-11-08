package com.mateo.inventoryservice.config;

import com.mateo.inventoryservice.model.InventoryItem;
import com.mateo.inventoryservice.repository.InventoryServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final InventoryServiceRepository repository;

    @Override
    public void run(String... args) {
        log.info("Initializing inventory data...");

        repository.save(InventoryItem.builder()
                .productId("PROD-001")
                .availableQuantity(100)
                .price(new BigDecimal("50.00"))
                .build());

        repository.save(InventoryItem.builder()
                .productId("PROD-002")
                .availableQuantity(50)
                .price(new BigDecimal("150.00"))
                .build());

        repository.save(InventoryItem.builder()
                .productId("PROD-003")
                .availableQuantity(5)
                .price(new BigDecimal("500.00"))
                .build());

        log.info("Inventory data initialized successfully!");
    }
}