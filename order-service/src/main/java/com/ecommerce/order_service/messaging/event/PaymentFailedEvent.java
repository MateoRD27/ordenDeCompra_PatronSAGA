/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */

package com.ecommerce.order_service.messaging.event;

/**
 *
 * @author ESTUDIANTES
 */
public record PaymentFailedEvent(
    String orderId,
    String reason
) {}