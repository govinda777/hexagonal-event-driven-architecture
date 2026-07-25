package com.govinda777.execution.infrastructure.delivery.webapp.consumers;

import com.govinda777.execution.business.usecase.ExecuteOrderStepUseCase;
import com.govinda777.execution.infrastructure.delivery.webapp.messages.OrderMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderSqsConsumerTest {

    @Mock
    private ExecuteOrderStepUseCase executeOrderStepUseCase;

    @InjectMocks
    private OrderSqsConsumer orderSqsConsumer;

    @Test
    void shouldInvokeUseCaseWhenMessageReceived() {
        UUID orderId = UUID.randomUUID();
        OrderMessage message = new OrderMessage(orderId, "CREATED", LocalDateTime.now());

        orderSqsConsumer.consume(message);

        verify(executeOrderStepUseCase, times(1)).execute(orderId);
    }

    @Test
    void shouldPropagateExceptionWhenUseCaseThrows() {
        UUID orderId = UUID.randomUUID();
        OrderMessage message = new OrderMessage(orderId, "CREATED", LocalDateTime.now());

        doThrow(new RuntimeException("Database down")).when(executeOrderStepUseCase).execute(orderId);

        assertThrows(RuntimeException.class, () -> orderSqsConsumer.consume(message));

        verify(executeOrderStepUseCase, times(1)).execute(orderId);
    }
}
