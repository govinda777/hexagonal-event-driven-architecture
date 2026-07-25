package com.govinda777.execution.infrastructure.delivery.webapp.consumers;

import com.govinda777.execution.business.usecase.ExecuteOrderStepUseCase;
import com.govinda777.execution.infrastructure.delivery.webapp.messages.OrderMessage;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OrderSqsConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderSqsConsumer.class);

    private final ExecuteOrderStepUseCase executeOrderStepUseCase;

    public OrderSqsConsumer(ExecuteOrderStepUseCase executeOrderStepUseCase) {
        this.executeOrderStepUseCase = executeOrderStepUseCase;
    }

    @SqsListener(value = "${app.sqs.queue-name:order-processing-queue}")
    public void consume(OrderMessage message) {
        log.info("Received SQS message for order: {} status: {}", message.orderId(), message.currentStatus());
        try {
            executeOrderStepUseCase.execute(message.orderId());
        } catch (Exception e) {
            log.error("Failed to process order step via SQS consumer for order ID: " + message.orderId(), e);
            // SQS message is rejected/dead-lettered depending on queue configuration
            throw e;
        }
    }
}
