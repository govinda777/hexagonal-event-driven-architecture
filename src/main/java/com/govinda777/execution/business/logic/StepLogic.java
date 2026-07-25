package com.govinda777.execution.business.logic;

import com.govinda777.execution.business.domain.OrderEntity;
import com.govinda777.execution.business.domain.OrderStatus;

public interface StepLogic {
    boolean canHandle(OrderStatus status);
    void execute(OrderEntity order);
}
