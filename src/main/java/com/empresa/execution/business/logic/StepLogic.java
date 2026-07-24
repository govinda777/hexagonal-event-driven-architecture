package com.empresa.execution.business.logic;

import com.empresa.execution.business.domain.OrderEntity;
import com.empresa.execution.business.domain.OrderStatus;

public interface StepLogic {
    boolean canHandle(OrderStatus status);
    void execute(OrderEntity order);
}
