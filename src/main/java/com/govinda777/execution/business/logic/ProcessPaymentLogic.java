package com.govinda777.execution.business.logic;

import com.govinda777.execution.business.domain.OrderEntity;
import com.govinda777.execution.business.domain.OrderStatus;
import java.time.LocalDateTime;

public class ProcessPaymentLogic implements StepLogic {

    @Override
    public boolean canHandle(OrderStatus status) {
        return OrderStatus.CREATED.equals(status);
    }

    @Override
    public void execute(OrderEntity order) {
        if (!canHandle(order.getStatus())) {
            throw new IllegalArgumentException("Invalid state " + order.getStatus() + " for ProcessPaymentLogic");
        }
        order.setStatus(OrderStatus.PAYMENT_APPROVED);
        order.appendHistoryLog("[PAYMENT] Approved at " + LocalDateTime.now() + " - AuthCode: PAY-9982");
    }
}
