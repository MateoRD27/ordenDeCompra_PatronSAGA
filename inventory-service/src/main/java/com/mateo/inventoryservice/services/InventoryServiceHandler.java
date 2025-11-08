package com.mateo.inventoryservice.services;


import com.mateo.inventoryservice.events.InventoryRejectedEvent;
import com.mateo.inventoryservice.events.InventoryReservedEvent;
import com.mateo.inventoryservice.events.ReleaseInventoryCommand;
import com.mateo.inventoryservice.events.ReserveInventoryCommand;
import com.mateo.inventoryservice.repository.InventoryServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceHandler {

    private final InventoryServiceRepository repository;
    private final StreamBridge streamBridge;

    @Bean
    public Consumer<ReserveInventoryCommand> reserveInventoryCommand() {
        return command -> {
            log.info("Recibido ReserveInventoryCommand: {}", command);
            repository.findByProductId(command.productId()).ifPresentOrElse(item -> {
                if (item.getAvailableQuantity() >= command.quantity()) {
                    item.setAvailableQuantity(item.getAvailableQuantity() - command.quantity());
                    repository.save(item);
                    var totalAmount = item.getPrice().multiply(java.math.BigDecimal.valueOf(command.quantity()));
                    streamBridge.send("inventoryReservedEvent-out-0",
                            new InventoryReservedEvent(command.orderId(), command.productId(), command.quantity(), totalAmount));
                } else {
                    streamBridge.send("inventoryRejectedEvent-out-0",
                            new InventoryRejectedEvent(command.orderId(), command.productId(), command.quantity(), "Insufficient stock"));
                }
            }, () -> {
                streamBridge.send("inventoryRejectedEvent-out-0",
                        new InventoryRejectedEvent(command.orderId(), command.productId(), command.quantity(), "Producto no"));

            });
        };
    }

    @Bean
    public Consumer<ReleaseInventoryCommand> releaseInventoryCommand() {
        return command -> {
            log.info("Recibido ReleaseInventoryCommand: {}", command);
            repository.findByProductId(command.productId()).ifPresent(item -> {
                item.setAvailableQuantity(item.getAvailableQuantity() + command.quantity());
                repository.save(item);
                log.info("Inventario liberado para orden {}", command.orderId());
            });
        };
    }
}
