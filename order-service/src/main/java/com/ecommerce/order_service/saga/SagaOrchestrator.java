package com.ecommerce.order_service.saga;

import com.ecommerce.order_service.entity.Order;
import com.ecommerce.order_service.messaging.MessageChannels;
import com.ecommerce.order_service.messaging.command.ReserveInventoryCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SagaOrchestrator {

    private final MessageChannels messageChannels;

    /**
     * Inicia la Saga enviando el comando para reservar inventario
     */
    public void startSaga(Order order) {
        log.info("Starting Saga for order: {}", order.getId());

        ReserveInventoryCommand command = new ReserveInventoryCommand(
                order.getId(),
                order.getProductId(),
                order.getQuantity()
        );

        messageChannels.sendReserveInventory(command);

        log.info("Sent ReserveInventoryCommand for order: {}", order.getId());
    }
}