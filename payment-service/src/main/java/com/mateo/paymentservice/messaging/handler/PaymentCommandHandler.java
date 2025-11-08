package com.mateo.paymentservice.messaging.handler;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mateo.paymentservice.domain.Enum.PaymentStatus;
import com.mateo.paymentservice.domain.Payment;
import com.mateo.paymentservice.messaging.MessageChannels;
import com.mateo.paymentservice.messaging.commands.ProcessPaymentCommand;
import com.mateo.paymentservice.messaging.events.PaymentCompletedEvent;
import com.mateo.paymentservice.messaging.events.PaymentFailedEvent;
import com.mateo.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class PaymentCommandHandler {

    private final PaymentService paymentService;
    private final MessageChannels messageChannels;
    private final ObjectMapper objectMapper;

    /**
     * Listener que recibe comandos de procesamiento de pago
     */
    @Bean
    public Consumer<Message<?>> processPaymentConsumer() {
        return message -> {
            try {
                log.info("Received ProcessPaymentCommand");

                ProcessPaymentCommand command = objectMapper.convertValue(
                        message.getPayload(),
                        ProcessPaymentCommand.class
                );

                handleProcessPayment(command);

            } catch (Exception e) {
                log.error("Error processing payment command", e);
            }
        };
    }

    /**
     * Procesa el comando de pago y emite el evento correspondiente
     */
    private void handleProcessPayment(ProcessPaymentCommand command) {
        log.info("Processing payment for order: {} with amount: {}",
                command.orderId(), command.amount());

        // Procesar el pago usando el servicio
        Payment payment = paymentService.processPayment(
                command.orderId(),
                command.amount()
        );

        // Emitir el evento correspondiente según el resultado
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            PaymentCompletedEvent event = new PaymentCompletedEvent(
                    payment.getOrderId(),
                    payment.getAmount()
            );

            messageChannels.sendPaymentCompleted(event);
            log.info("Sent PaymentCompletedEvent for order: {}", command.orderId());

        } else {
            PaymentFailedEvent event = new PaymentFailedEvent(
                    payment.getOrderId(),
                    payment.getAmount(),
                    "Amount exceeds limit or payment processing failed"
            );

            messageChannels.sendPaymentFailed(event);
            log.warn("Sent PaymentFailedEvent for order: {}", command.orderId());
        }
    }
}
