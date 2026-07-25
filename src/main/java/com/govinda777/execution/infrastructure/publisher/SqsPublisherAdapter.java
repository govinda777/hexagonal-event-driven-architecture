package com.govinda777.execution.infrastructure.publisher;

import com.govinda777.execution.business.domain.OrderEntity;
import com.govinda777.execution.business.gateway.ProcessPublisherGateway;
import com.govinda777.execution.infrastructure.delivery.webapp.messages.OrderMessage;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class SqsPublisherAdapter implements ProcessPublisherGateway {

    private static final Logger log = LoggerFactory.getLogger(SqsPublisherAdapter.class);

    private final SqsTemplate sqsTemplate;
    private final String queueName;

    public SqsPublisherAdapter(SqsTemplate sqsTemplate,
                               @Value("${app.sqs.queue-name:order-processing-queue}") String queueName) {
        this.sqsTemplate = sqsTemplate;
        this.queueName = queueName;
    }

    @Override
    public void publish(OrderEntity order) {
        log.info("Publishing transition event for order: {} in status: {} to SQS queue: {}",
                order.getId(), order.getStatus(), queueName);

        OrderMessage message = new OrderMessage(
                order.getId(),
                order.getStatus().name(),
                LocalDateTime.now()
        );

        sqsTemplate.send(queueName, message);
    }
}
