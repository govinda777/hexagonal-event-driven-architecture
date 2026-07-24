package com.empresa.execution.business.logic;

import com.empresa.execution.business.domain.OrderEntity;
import com.empresa.execution.business.domain.OrderStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProcessPaymentLogicTest {

    private final ProcessPaymentLogic paymentLogic = new ProcessPaymentLogic();

    @Test
    void shouldHandleCreatedStatus() {
        assertTrue(paymentLogic.canHandle(OrderStatus.CREATED));
        assertFalse(paymentLogic.canHandle(OrderStatus.PAYMENT_APPROVED));
        assertFalse(paymentLogic.canHandle(OrderStatus.COMPLETED));
        assertFalse(paymentLogic.canHandle(OrderStatus.FAILED));
    }

    @Test
    void shouldExecuteAndChangeStateToPaymentApproved() {
        OrderEntity order = new OrderEntity();
        order.setId(UUID.randomUUID());
        order.setStatus(OrderStatus.CREATED);
        order.setCustomerId("CUST-1");
        order.setTotalAmount(BigDecimal.TEN);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        paymentLogic.execute(order);

        assertEquals(OrderStatus.PAYMENT_APPROVED, order.getStatus());
        assertNotNull(order.getHistoryLog());
        assertTrue(order.getHistoryLog().contains("[PAYMENT] Approved"));
    }

    @Test
    void shouldThrowExceptionWhenExecutingOnInvalidState() {
        OrderEntity order = new OrderEntity();
        order.setStatus(OrderStatus.COMPLETED);

        assertThrows(IllegalArgumentException.class, () -> paymentLogic.execute(order));
    }
}
