package com.mateo.inventoryservice.events;

public record InventoryRejectedEvent(
        String orderId,
        String productId,
        int quantity,
        String reason  // AGREGAR ESTE CAMPO
) {}