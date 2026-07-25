package com.govinda777.execution.business.logic;

import com.govinda777.execution.business.domain.OrderEntity;
import com.govinda777.execution.business.domain.OrderStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ReserveInventoryLogicTest {

    private final ReserveInventoryLogic inventoryLogic = new ReserveInventoryLogic();

    @Test
    void shouldHandlePaymentApprovedStatus() {
        assertFalse(inventoryLogic.canHandle(OrderStatus.CREATED));
        assertTrue(inventoryLogic.canHandle(OrderStatus.PAYMENT_APPROVED));
        assertFalse(inventoryLogic.canHandle(OrderStatus.COMPLETED));
        assertFalse(inventoryLogic.canHandle(OrderStatus.FAILED));
    }

    @Test
    void shouldExecuteAndChangeStateToCompleted() {
        OrderEntity order = new OrderEntity();
        order.setId(UUID.randomUUID());
        order.setStatus(OrderStatus.PAYMENT_APPROVED);
        order.setCustomerId("CUST-1");
        order.setTotalAmount(BigDecimal.TEN);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        inventoryLogic.execute(order);

        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        assertNotNull(order.getHistoryLog());
        assertTrue(order.getHistoryLog().contains("[INVENTORY] Reserved items successfully"));
    }

    @Test
    void shouldThrowExceptionWhenExecutingOnInvalidState() {
        OrderEntity order = new OrderEntity();
        order.setStatus(OrderStatus.CREATED);

        assertThrows(IllegalArgumentException.class, () -> inventoryLogic.execute(order));
    }
}
