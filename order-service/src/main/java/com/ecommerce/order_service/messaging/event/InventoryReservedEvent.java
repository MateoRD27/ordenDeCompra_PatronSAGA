/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */

package com.ecommerce.order_service.messaging.event;

import java.math.BigDecimal;
/**
 *
 * @author ESTUDIANTES
 */

public record InventoryReservedEvent(
    String orderId,
    String productId,
    Integer quantity,
    BigDecimal totalAmount
) {}