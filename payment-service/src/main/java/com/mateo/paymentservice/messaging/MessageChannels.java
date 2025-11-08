package com.mateo.paymentservice.messaging;

import com.mateo.paymentservice.messaging.events.PaymentCompletedEvent;
import com.mateo.paymentservice.messaging.events.PaymentFailedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.function.Supplier;

@Configuration
public class MessageChannels {
    // Sinks para enviar eventos
    private final Sinks.Many<PaymentCompletedEvent> paymentCompletedSink =
            Sinks.many().unicast().onBackpressureBuffer();

    private final Sinks.Many<PaymentFailedEvent> paymentFailedSink =
            Sinks.many().unicast().onBackpressureBuffer();


    // OUTPUTS - Eventos que enviamos
    @Bean
    public Supplier<Flux<PaymentCompletedEvent>> paymentCompleted() {
        return () -> paymentCompletedSink.asFlux();
    }

    @Bean
    public Supplier<Flux<PaymentFailedEvent>> paymentFailed() {
        return () -> paymentFailedSink.asFlux();
    }


    // Métodos públicos para enviar mensajes
    public void sendPaymentCompleted(PaymentCompletedEvent event) {
        paymentCompletedSink.tryEmitNext(event);
    }

    public void sendPaymentFailed(PaymentFailedEvent event) {
        paymentFailedSink.tryEmitNext(event);
    }
}
