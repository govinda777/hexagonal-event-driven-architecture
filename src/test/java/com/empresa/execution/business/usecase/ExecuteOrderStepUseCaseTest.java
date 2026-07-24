package com.empresa.execution.business.usecase;

import com.empresa.execution.business.domain.OrderEntity;
import com.empresa.execution.business.domain.OrderStatus;
import com.empresa.execution.business.gateway.ProcessPublisherGateway;
import com.empresa.execution.business.gateway.ProcessRepositoryGateway;
import com.empresa.execution.business.logic.ProcessPaymentLogic;
import com.empresa.execution.business.logic.ReserveInventoryLogic;
import com.empresa.execution.business.logic.StepLogic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExecuteOrderStepUseCaseTest {

    @Mock
    private ProcessRepositoryGateway repositoryGateway;

    @Mock
    private ProcessPublisherGateway publisherGateway;

    private ExecuteOrderStepUseCase executeOrderStepUseCase;

    @BeforeEach
    void setUp() {
        List<StepLogic> steps = List.of(new ProcessPaymentLogic(), new ReserveInventoryLogic());
        executeOrderStepUseCase = new ExecuteOrderStepUseCase(repositoryGateway, publisherGateway, steps);
    }

    @Test
    void shouldExecuteFirstStepAndPublishNext() {
        UUID orderId = UUID.randomUUID();
        OrderEntity order = new OrderEntity(orderId, "CUST-1", BigDecimal.TEN, OrderStatus.CREATED, "[CREATED]", LocalDateTime.now(), LocalDateTime.now());

        when(repositoryGateway.findById(orderId)).thenReturn(Optional.of(order));
        when(repositoryGateway.save(any(OrderEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        executeOrderStepUseCase.execute(orderId);

        assertEquals(OrderStatus.PAYMENT_APPROVED, order.getStatus());
        assertTrue(order.getHistoryLog().contains("[PAYMENT] Approved"));

        verify(repositoryGateway, times(1)).save(order);
        verify(publisherGateway, times(1)).publish(order);
    }

    @Test
    void shouldExecuteSecondStepAndNotPublishBecauseCompleted() {
        UUID orderId = UUID.randomUUID();
        OrderEntity order = new OrderEntity(orderId, "CUST-1", BigDecimal.TEN, OrderStatus.PAYMENT_APPROVED, "[PAYMENT] Approved", LocalDateTime.now(), LocalDateTime.now());

        when(repositoryGateway.findById(orderId)).thenReturn(Optional.of(order));
        when(repositoryGateway.save(any(OrderEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        executeOrderStepUseCase.execute(orderId);

        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        assertTrue(order.getHistoryLog().contains("[INVENTORY] Reserved items successfully"));

        verify(repositoryGateway, times(1)).save(order);
        verify(publisherGateway, never()).publish(any());
    }

    @Test
    void shouldHandleExceptionAndMarkAsFailed() {
        UUID orderId = UUID.randomUUID();
        // An order with status CREATED, but we will force finding no strategies or some throw
        OrderEntity order = new OrderEntity(orderId, "CUST-1", BigDecimal.TEN, OrderStatus.CREATED, "[CREATED]", LocalDateTime.now(), LocalDateTime.now());

        // Let's create a usecase with no steps to force strategy not found exception
        ExecuteOrderStepUseCase brokenUseCase = new ExecuteOrderStepUseCase(repositoryGateway, publisherGateway, List.of());

        when(repositoryGateway.findById(orderId)).thenReturn(Optional.of(order));

        brokenUseCase.execute(orderId);

        assertEquals(OrderStatus.FAILED, order.getStatus());
        assertTrue(order.getHistoryLog().contains("[FAILED] Error executing step"));

        verify(repositoryGateway, times(1)).save(order);
        verify(publisherGateway, never()).publish(any());
    }
}
