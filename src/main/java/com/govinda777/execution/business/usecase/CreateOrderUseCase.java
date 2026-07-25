package com.govinda777.execution.business.usecase;

import com.govinda777.execution.business.domain.OrderEntity;
import com.govinda777.execution.business.domain.OrderStatus;
import com.govinda777.execution.business.gateway.ProcessPublisherGateway;
import com.govinda777.execution.business.gateway.ProcessRepositoryGateway;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class CreateOrderUseCase {

    private final ProcessRepositoryGateway repositoryGateway;
    private final ProcessPublisherGateway publisherGateway;

    public CreateOrderUseCase(ProcessRepositoryGateway repositoryGateway, ProcessPublisherGateway publisherGateway) {
        this.repositoryGateway = repositoryGateway;
        this.publisherGateway = publisherGateway;
    }

    public OrderEntity execute(String customerId, BigDecimal totalAmount) {
        OrderEntity order = new OrderEntity();
        order.setId(UUID.randomUUID());
        order.setCustomerId(customerId);
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setHistoryLog("[CREATED] Order initialized");

        OrderEntity savedOrder = repositoryGateway.save(order);
        publisherGateway.publish(savedOrder);
        return savedOrder;
    }
}
