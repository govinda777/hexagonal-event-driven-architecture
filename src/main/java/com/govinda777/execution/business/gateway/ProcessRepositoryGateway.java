package com.govinda777.execution.business.gateway;

import com.govinda777.execution.business.domain.OrderEntity;
import java.util.Optional;
import java.util.UUID;

public interface ProcessRepositoryGateway {
    OrderEntity save(OrderEntity order);
    Optional<OrderEntity> findById(UUID id);
}
