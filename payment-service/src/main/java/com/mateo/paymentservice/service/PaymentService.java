package com.mateo.paymentservice.service;

import com.mateo.paymentservice.domain.Enum.PaymentStatus;
import com.mateo.paymentservice.domain.Payment;
import com.mateo.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;


@Service
@RequiredArgsConstructor
@Slf4j

public class PaymentService {
    private final PaymentRepository paymentRepository;

    // Monto límite para simular fallo de pago
    private static final BigDecimal PAYMENT_FAILURE_THRESHOLD = new BigDecimal("10000.00");

    @Transactional
    public Payment processPayment(String orderId, BigDecimal amount) {
        log.info("Processing payment for order: {} with amount: {}", orderId, amount);

        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setAmount(amount);

        // Simulación de lógica de pago
        // Falla si el monto es mayor al umbral definido
        if (shouldPaymentFail(amount)) {
            log.warn("Payment failed for order: {} - Amount exceeds threshold", orderId);
            payment.setStatus(PaymentStatus.FAILED);
        } else {
            log.info("Payment successful for order: {}", orderId);
            payment.setStatus(PaymentStatus.SUCCESS);
        }

        return paymentRepository.save(payment);
    }
   // Determina si el pago debe fallar

    private boolean shouldPaymentFail(BigDecimal amount) {
        // Regla 1: Falla si el monto es mayor al umbral
        if (amount.compareTo(PAYMENT_FAILURE_THRESHOLD) > 0) {
            return true;
        }
        return false;
    }
}
