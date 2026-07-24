package com.empresa.execution.business.gateway;

import com.empresa.execution.business.domain.OrderEntity;
import java.util.Optional;
import java.util.UUID;

public interface ProcessRepositoryGateway {
    OrderEntity save(OrderEntity order);
    Optional<OrderEntity> findById(UUID id);
}
