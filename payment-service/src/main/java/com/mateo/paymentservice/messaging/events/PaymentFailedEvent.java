package com.mateo.paymentservice.messaging.events;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentFailedEvent(
        String orderId,
        BigDecimal amount,
        String reason
) {
}
