package com.empresa.execution.business.gateway;

import com.empresa.execution.business.domain.OrderEntity;

public interface ProcessPublisherGateway {
    void publish(OrderEntity order);
}
