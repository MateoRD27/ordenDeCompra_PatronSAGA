package com.ecommerce.order_service.messaging.handler;

import com.ecommerce.order_service.entity.Order;
import com.ecommerce.order_service.entity.enums.OrderStatus;
import com.ecommerce.order_service.messaging.MessageChannels;
import com.ecommerce.order_service.messaging.command.ProcessPaymentCommand;
import com.ecommerce.order_service.messaging.event.InventoryRejectedEvent;
import com.ecommerce.order_service.messaging.event.InventoryReservedEvent;
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
public class InventoryEventHandler {

    private final OrderRepository orderRepository;
    private final MessageChannels messageChannels;
    private final ObjectMapper objectMapper;

    @Bean
    public Consumer<Message<?>> inventoryEvents() {
        return message -> {
            try {
                String routingKey = (String) message.getHeaders().get("amqp_receivedRoutingKey");
                log.info("Received inventory event with routing key: {}", routingKey);

                if ("inventory.reserved".equals(routingKey)) {
                    InventoryReservedEvent event = objectMapper.convertValue(
                            message.getPayload(),
                            InventoryReservedEvent.class
                    );
                    handleInventoryReserved(event);
                } else if ("inventory.rejected".equals(routingKey)) {
                    InventoryRejectedEvent event = objectMapper.convertValue(
                            message.getPayload(),
                            InventoryRejectedEvent.class
                    );
                    handleInventoryRejected(event);
                }
            } catch (Exception e) {
                log.error("Error processing inventory event", e);
            }
        };
    }

    @Transactional
    public void handleInventoryReserved(InventoryReservedEvent event) {
        log.info("Received InventoryReservedEvent for order: {}", event.orderId());

        Order order = orderRepository.findById(event.orderId())
                .orElseThrow(() -> new RuntimeException("Order not found: " + event.orderId()));

        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setTotalAmount(event.totalAmount());
        orderRepository.save(order);

        log.info("Order {} updated to PENDING_PAYMENT. Initiating payment...", event.orderId());

        ProcessPaymentCommand command = new ProcessPaymentCommand(
                event.orderId(),
                event.totalAmount()
        );

        messageChannels.sendProcessPayment(command);
        log.info("Sent ProcessPaymentCommand for order: {}", event.orderId());
    }

    @Transactional
    public void handleInventoryRejected(InventoryRejectedEvent event) {
        log.warn("Received InventoryRejectedEvent for order: {}", event.orderId());

        Order order = orderRepository.findById(event.orderId())
                .orElseThrow(() -> new RuntimeException("Order not found: " + event.orderId()));

        order.setStatus(OrderStatus.REJECTED);
        orderRepository.save(order);

        log.info("Order {} marked as REJECTED", event.orderId());
    }
}