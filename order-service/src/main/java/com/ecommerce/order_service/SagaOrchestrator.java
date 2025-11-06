/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.ecommerce.order_service;

/**
 *
 * @author ESTUDIANTES
 */
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.order_service.config.RabbitMQConfig;
import com.ecommerce.order_service.entity.Order;
import com.ecommerce.order_service.entity.enums.OrderStatus;
import com.ecommerce.order_service.messaging.command.ProcessPaymentCommand;
import com.ecommerce.order_service.messaging.command.ReleaseInventoryCommand;
import com.ecommerce.order_service.messaging.command.ReserveInventoryCommand;
import com.ecommerce.order_service.messaging.event.InventoryRejectedEvent;
import com.ecommerce.order_service.messaging.event.InventoryReservedEvent;
import com.ecommerce.order_service.messaging.event.OrderCompletedEvent;
import com.ecommerce.order_service.messaging.event.PaymentCompletedEvent;
import com.ecommerce.order_service.messaging.event.PaymentFailedEvent;
import com.ecommerce.order_service.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SagaOrchestrator {
    
    private final RabbitTemplate rabbitTemplate;
    private final OrderRepository orderRepository;
    
    public void startSaga(Order order) {
        log.info("Starting Saga for order: {}", order.getId());
        
        ReserveInventoryCommand command = new ReserveInventoryCommand(
            order.getId(),
            order.getProductId(),
            order.getQuantity()
        );
        
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.INVENTORY_EXCHANGE,
            RabbitMQConfig.RESERVE_INVENTORY_KEY,
            command
        );
        
        log.info("Sent ReserveInventoryCommand for order: {}", order.getId());
    }
    
    @RabbitListener(queues = RabbitMQConfig.INVENTORY_EVENT_QUEUE)
    @Transactional
    public void handleInventoryEvents(Object event) {
        if (event instanceof InventoryReservedEvent reservedEvent) {
            handleInventoryReserved(reservedEvent);
        } else if (event instanceof InventoryRejectedEvent rejectedEvent) {
            handleInventoryRejected(rejectedEvent);
        }
    }
    
    private void handleInventoryReserved(InventoryReservedEvent event) {
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
        
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.PAYMENT_EXCHANGE,
            RabbitMQConfig.PROCESS_PAYMENT_KEY,
            command
        );
    }
    
    private void handleInventoryRejected(InventoryRejectedEvent event) {
        log.warn("Received InventoryRejectedEvent for order: {}", event.orderId());
        
        Order order = orderRepository.findById(event.orderId())
            .orElseThrow(() -> new RuntimeException("Order not found: " + event.orderId()));
        
        order.setStatus(OrderStatus.REJECTED);
        orderRepository.save(order);
        
        log.info("Order {} marked as REJECTED", event.orderId());
    }
    
    @RabbitListener(queues = RabbitMQConfig.PAYMENT_EVENT_QUEUE)
    @Transactional
    public void handlePaymentEvents(Object event) {
        if (event instanceof PaymentCompletedEvent completedEvent) {
            handlePaymentCompleted(completedEvent);
        } else if (event instanceof PaymentFailedEvent failedEvent) {
            handlePaymentFailed(failedEvent);
        }
    }
    
    private void handlePaymentCompleted(PaymentCompletedEvent event) {
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
        
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.ORDER_EXCHANGE,
            "order.completed",
            completedEvent
        );
    }
    
    private void handlePaymentFailed(PaymentFailedEvent event) {
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
        
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.INVENTORY_EXCHANGE,
            RabbitMQConfig.RELEASE_INVENTORY_KEY,
            command
        );
        
        log.info("Sent ReleaseInventoryCommand for order: {}", event.orderId());
    }
}