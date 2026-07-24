package com.empresa.execution.infrastructure.repository;

import com.empresa.execution.business.domain.OrderEntity;
import com.empresa.execution.business.domain.OrderStatus;
import com.empresa.execution.business.gateway.ProcessRepositoryGateway;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class OrderRepositoryAdapter implements ProcessRepositoryGateway {

    private final SpringDataOrderRepository springDataOrderRepository;

    public OrderRepositoryAdapter(SpringDataOrderRepository springDataOrderRepository) {
        this.springDataOrderRepository = springDataOrderRepository;
    }

    @Override
    public OrderEntity save(OrderEntity order) {
        OrderJpaEntity jpaEntity = toJpa(order);
        OrderJpaEntity savedJpa = springDataOrderRepository.save(jpaEntity);
        return toDomain(savedJpa);
    }

    @Override
    public Optional<OrderEntity> findById(UUID id) {
        return springDataOrderRepository.findById(id).map(this::toDomain);
    }

    private OrderJpaEntity toJpa(OrderEntity order) {
        OrderJpaEntity jpa = new OrderJpaEntity();
        jpa.setId(order.getId());
        jpa.setCustomerId(order.getCustomerId());
        jpa.setTotalAmount(order.getTotalAmount());
        jpa.setStatus(order.getStatus().name());
        jpa.setHistoryLog(order.getHistoryLog());
        jpa.setCreatedAt(order.getCreatedAt());
        jpa.setUpdatedAt(order.getUpdatedAt());
        return jpa;
    }

    private OrderEntity toDomain(OrderJpaEntity jpa) {
        OrderEntity order = new OrderEntity();
        order.setId(jpa.getId());
        order.setCustomerId(jpa.getCustomerId());
        order.setTotalAmount(jpa.getTotalAmount());
        order.setStatus(OrderStatus.valueOf(jpa.getStatus()));
        order.setHistoryLog(jpa.getHistoryLog());
        order.setCreatedAt(jpa.getCreatedAt());
        order.setUpdatedAt(jpa.getUpdatedAt());
        return order;
    }
}
