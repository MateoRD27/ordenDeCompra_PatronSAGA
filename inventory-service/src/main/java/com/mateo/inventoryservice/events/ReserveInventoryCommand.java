package com.mateo.inventoryservice.events;

public record ReserveInventoryCommand(String orderId, String productId, int quantity) {
}
