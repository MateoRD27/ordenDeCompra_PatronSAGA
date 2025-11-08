package com.ecommerce.order_service.messaging;

import com.ecommerce.order_service.messaging.command.ProcessPaymentCommand;
import com.ecommerce.order_service.messaging.command.ReleaseInventoryCommand;
import com.ecommerce.order_service.messaging.command.ReserveInventoryCommand;
import com.ecommerce.order_service.messaging.event.OrderCompletedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.function.Supplier;

@Configuration
public class MessageChannels {

    // Sinks para enviar comandos
    private final Sinks.Many<ReserveInventoryCommand> reserveInventorySink =
            Sinks.many().unicast().onBackpressureBuffer();

    private final Sinks.Many<ReleaseInventoryCommand> releaseInventorySink =
            Sinks.many().unicast().onBackpressureBuffer();

    private final Sinks.Many<ProcessPaymentCommand> processPaymentSink =
            Sinks.many().unicast().onBackpressureBuffer();

    private final Sinks.Many<OrderCompletedEvent> orderCompletedSink =
            Sinks.many().unicast().onBackpressureBuffer();

    // ============================================
    // OUTPUTS - Comandos que enviamos
    // ============================================

    @Bean
    public Supplier<Flux<ReserveInventoryCommand>> reserveInventory() {
        return () -> reserveInventorySink.asFlux();
    }

    @Bean
    public Supplier<Flux<ReleaseInventoryCommand>> releaseInventory() {
        return () -> releaseInventorySink.asFlux();
    }

    @Bean
    public Supplier<Flux<ProcessPaymentCommand>> processPayment() {
        return () -> processPaymentSink.asFlux();
    }

    @Bean
    public Supplier<Flux<OrderCompletedEvent>> orderCompleted() {
        return () -> orderCompletedSink.asFlux();
    }

    // ============================================
    // Métodos públicos para enviar mensajes
    // ============================================

    public void sendReserveInventory(ReserveInventoryCommand command) {
        reserveInventorySink.tryEmitNext(command);
    }

    public void sendReleaseInventory(ReleaseInventoryCommand command) {
        releaseInventorySink.tryEmitNext(command);
    }

    public void sendProcessPayment(ProcessPaymentCommand command) {
        processPaymentSink.tryEmitNext(command);
    }

    public void sendOrderCompleted(OrderCompletedEvent event) {
        orderCompletedSink.tryEmitNext(event);
    }
}