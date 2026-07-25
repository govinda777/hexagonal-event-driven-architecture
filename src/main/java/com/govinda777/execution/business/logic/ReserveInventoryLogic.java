package com.govinda777.execution.business.logic;

import com.govinda777.execution.business.domain.OrderEntity;
import com.govinda777.execution.business.domain.OrderStatus;
import java.time.LocalDateTime;

public class ReserveInventoryLogic implements StepLogic {

    @Override
    public boolean canHandle(OrderStatus status) {
        return OrderStatus.PAYMENT_APPROVED.equals(status);
    }

    @Override
    public void execute(OrderEntity order) {
        if (!canHandle(order.getStatus())) {
            throw new IllegalArgumentException("Invalid state " + order.getStatus() + " for ReserveInventoryLogic");
        }
        order.setStatus(OrderStatus.COMPLETED);
        order.appendHistoryLog("[INVENTORY] Reserved items successfully at " + LocalDateTime.now());
    }
}
