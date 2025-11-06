package com.mateo.inventoryservice.events;

public record InventoryReservedEvent(String orderId, String productId, int quantity, java.math.BigDecimal totalAmount) {
}
