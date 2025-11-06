/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.ecommerce.order_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author ESTUDIANTES
 */
@Configuration
public class RabbitMQConfig {
    
    // Exchanges
    public static final String INVENTORY_EXCHANGE = "inventory.exchange";
    public static final String PAYMENT_EXCHANGE = "payment.exchange";
    public static final String ORDER_EXCHANGE = "order.exchange";
    
    // Queues
    public static final String INVENTORY_COMMAND_QUEUE = "inventory.command.queue";
    public static final String INVENTORY_EVENT_QUEUE = "inventory.event.queue";
    public static final String PAYMENT_COMMAND_QUEUE = "payment.command.queue";
    public static final String PAYMENT_EVENT_QUEUE = "payment.event.queue";
    
    // Routing Keys
    public static final String RESERVE_INVENTORY_KEY = "inventory.reserve";
    public static final String RELEASE_INVENTORY_KEY = "inventory.release";
    public static final String INVENTORY_RESERVED_KEY = "inventory.reserved";
    public static final String INVENTORY_REJECTED_KEY = "inventory.rejected";
    public static final String PROCESS_PAYMENT_KEY = "payment.process";
    public static final String PAYMENT_COMPLETED_KEY = "payment.completed";
    public static final String PAYMENT_FAILED_KEY = "payment.failed";
    
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
    
    // Inventory Exchange and Queues
    @Bean
    public TopicExchange inventoryExchange() {
        return new TopicExchange(INVENTORY_EXCHANGE);
    }
    
    @Bean
    public Queue inventoryCommandQueue() {
        return new Queue(INVENTORY_COMMAND_QUEUE, true);
    }
    
    @Bean
    public Queue inventoryEventQueue() {
        return new Queue(INVENTORY_EVENT_QUEUE, true);
    }
    
    @Bean
    public Binding reserveInventoryBinding() {
        return BindingBuilder
            .bind(inventoryCommandQueue())
            .to(inventoryExchange())
            .with(RESERVE_INVENTORY_KEY);
    }
    
    @Bean
    public Binding releaseInventoryBinding() {
        return BindingBuilder
            .bind(inventoryCommandQueue())
            .to(inventoryExchange())
            .with(RELEASE_INVENTORY_KEY);
    }
    
    @Bean
    public Binding inventoryReservedBinding() {
        return BindingBuilder
            .bind(inventoryEventQueue())
            .to(inventoryExchange())
            .with(INVENTORY_RESERVED_KEY);
    }
    
    @Bean
    public Binding inventoryRejectedBinding() {
        return BindingBuilder
            .bind(inventoryEventQueue())
            .to(inventoryExchange())
            .with(INVENTORY_REJECTED_KEY);
    }
    
    // Payment Exchange and Queues
    @Bean
    public TopicExchange paymentExchange() {
        return new TopicExchange(PAYMENT_EXCHANGE);
    }
    
    @Bean
    public Queue paymentCommandQueue() {
        return new Queue(PAYMENT_COMMAND_QUEUE, true);
    }
    
    @Bean
    public Queue paymentEventQueue() {
        return new Queue(PAYMENT_EVENT_QUEUE, true);
    }
    
    @Bean
    public Binding processPaymentBinding() {
        return BindingBuilder
            .bind(paymentCommandQueue())
            .to(paymentExchange())
            .with(PROCESS_PAYMENT_KEY);
    }
    
    @Bean
    public Binding paymentCompletedBinding() {
        return BindingBuilder
            .bind(paymentEventQueue())
            .to(paymentExchange())
            .with(PAYMENT_COMPLETED_KEY);
    }
    
    @Bean
    public Binding paymentFailedBinding() {
        return BindingBuilder
            .bind(paymentEventQueue())
            .to(paymentExchange())
            .with(PAYMENT_FAILED_KEY);
    }
}
