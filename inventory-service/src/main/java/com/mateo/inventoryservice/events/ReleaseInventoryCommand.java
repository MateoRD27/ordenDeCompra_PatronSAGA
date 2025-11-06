package com.mateo.inventoryservice.events;

public record ReleaseInventoryCommand(String orderId, String productId, int quantity) {
}
