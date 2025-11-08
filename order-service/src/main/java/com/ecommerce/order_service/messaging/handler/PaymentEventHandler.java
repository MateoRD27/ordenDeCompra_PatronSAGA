package com.ecommerce.order_service.messaging.handler;

import com.ecommerce.order_service.entity.Order;
import com.ecommerce.order_service.entity.enums.OrderStatus;
import com.ecommerce.order_service.messaging.MessageChannels;
import com.ecommerce.order_service.messaging.command.ReleaseInventoryCommand;
import com.ecommerce.order_service.messaging.event.OrderCompletedEvent;
import com.ecommerce.order_service.messaging.event.PaymentCompletedEvent;
import com.ecommerce.order_service.messaging.event.PaymentFailedEvent;
import com.ecommerce.order_service.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Consumer;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class PaymentEventHandler {

    private final OrderRepository orderRepository;
    private final MessageChannels messageChannels;
    private final ObjectMapper objectMapper;

    @Bean
    public Consumer<Message<?>> paymentEvents() {
        return message -> {
            try {
                String routingKey = (String) message.getHeaders().get("amqp_receivedRoutingKey");
                log.info("Received payment event with routing key: {}", routingKey);

                if ("payment.completed".equals(routingKey)) {
                    PaymentCompletedEvent event = objectMapper.convertValue(
                            message.getPayload(),
                            PaymentCompletedEvent.class
                    );
                    handlePaymentCompleted(event);
                } else if ("payment.failed".equals(routingKey)) {
                    PaymentFailedEvent event = objectMapper.convertValue(
                            message.getPayload(),
                            PaymentFailedEvent.class
                    );
                    handlePaymentFailed(event);
                }
            } catch (Exception e) {
                log.error("Error processing payment event", e);
            }
        };
    }

    @Transactional
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("Received PaymentCompletedEvent for order: {}", event.orderId());

        Order order = orderRepository.findById(event.orderId())
                .orElseThrow(() -> new RuntimeException("Order not found: " + event.orderId()));

        order.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);

        log.info("Order {} marked as COMPLETED. Saga finished successfully!", event.orderId());

        OrderCompletedEvent completedEvent = new OrderCompletedEvent(
                order.getId(),
                order.getProductId(),
                order.getQuantity(),
                order.getTotalAmount()
        );

        messageChannels.sendOrderCompleted(completedEvent);
        log.info("Sent OrderCompletedEvent for order: {}", event.orderId());
    }

    @Transactional
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.warn("Received PaymentFailedEvent for order: {}", event.orderId());

        Order order = orderRepository.findById(event.orderId())
                .orElseThrow(() -> new RuntimeException("Order not found: " + event.orderId()));

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        log.info("Order {} marked as CANCELLED. Initiating compensation...", event.orderId());

        ReleaseInventoryCommand command = new ReleaseInventoryCommand(
                order.getId(),
                order.getProductId(),
                order.getQuantity()
        );

        messageChannels.sendReleaseInventory(command);
        log.info("Sent ReleaseInventoryCommand for order: {}", event.orderId());
    }
}