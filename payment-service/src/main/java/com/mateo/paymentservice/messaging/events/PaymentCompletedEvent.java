package com.mateo.paymentservice.messaging.events;

import java.math.BigDecimal;

public record PaymentCompletedEvent(
        String orderId,
        BigDecimal amount
) {
}
