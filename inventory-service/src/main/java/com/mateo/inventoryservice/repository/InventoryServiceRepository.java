package com.mateo.inventoryservice.repository;

import com.mateo.inventoryservice.model.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryServiceRepository extends JpaRepository<InventoryItem, Long> {
 Optional<InventoryItem> findByProductId(String productId);
}
