package com.mateo.paymentservice.messaging.commands;

import java.math.BigDecimal;

public record ProcessPaymentCommand(
        String orderId,
        BigDecimal amount
) {
}
