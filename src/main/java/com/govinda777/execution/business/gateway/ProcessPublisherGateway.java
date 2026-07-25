package com.govinda777.execution.business.gateway;

import com.govinda777.execution.business.domain.OrderEntity;

public interface ProcessPublisherGateway {
    void publish(OrderEntity order);
}
