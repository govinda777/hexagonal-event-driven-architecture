package com.govinda777.execution.business.usecase;

import com.govinda777.execution.business.domain.OrderEntity;
import com.govinda777.execution.business.domain.OrderStatus;
import com.govinda777.execution.business.gateway.ProcessPublisherGateway;
import com.govinda777.execution.business.gateway.ProcessRepositoryGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateOrderUseCaseTest {

    @Mock
    private ProcessRepositoryGateway repositoryGateway;

    @Mock
    private ProcessPublisherGateway publisherGateway;

    @InjectMocks
    private CreateOrderUseCase createOrderUseCase;

    @Test
    void shouldCreateOrderSaveAndPublish() {
        String customerId = "CUST-123";
        BigDecimal amount = new BigDecimal("299.90");

        when(repositoryGateway.save(any(OrderEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderEntity createdOrder = createOrderUseCase.execute(customerId, amount);

        assertNotNull(createdOrder);
        assertNotNull(createdOrder.getId());
        assertEquals(customerId, createdOrder.getCustomerId());
        assertEquals(amount, createdOrder.getTotalAmount());
        assertEquals(OrderStatus.CREATED, createdOrder.getStatus());
        assertTrue(createdOrder.getHistoryLog().contains("[CREATED]"));

        verify(repositoryGateway, times(1)).save(any(OrderEntity.class));
        verify(publisherGateway, times(1)).publish(createdOrder);
    }
}
