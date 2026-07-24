package com.empresa.execution.business.usecase;

import com.empresa.execution.business.domain.OrderEntity;
import com.empresa.execution.business.domain.OrderStatus;
import com.empresa.execution.business.gateway.ProcessPublisherGateway;
import com.empresa.execution.business.gateway.ProcessRepositoryGateway;
import com.empresa.execution.business.logic.StepLogic;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ExecuteOrderStepUseCase {

    private final ProcessRepositoryGateway repositoryGateway;
    private final ProcessPublisherGateway publisherGateway;
    private final List<StepLogic> steps;

    public ExecuteOrderStepUseCase(ProcessRepositoryGateway repositoryGateway,
                                   ProcessPublisherGateway publisherGateway,
                                   List<StepLogic> steps) {
        this.repositoryGateway = repositoryGateway;
        this.publisherGateway = publisherGateway;
        this.steps = steps;
    }

    public void execute(UUID orderId) {
        OrderEntity order = repositoryGateway.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order with ID " + orderId + " not found"));

        // If it's already in a final state, do nothing
        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.FAILED) {
            return;
        }

        try {
            StepLogic matchingLogic = steps.stream()
                    .filter(step -> step.canHandle(order.getStatus()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No strategy found for status: " + order.getStatus()));

            matchingLogic.execute(order);
            order.setUpdatedAt(LocalDateTime.now());
            OrderEntity savedOrder = repositoryGateway.save(order);

            // If the state is not final, publish to SQS for the next step
            if (savedOrder.getStatus() != OrderStatus.COMPLETED && savedOrder.getStatus() != OrderStatus.FAILED) {
                publisherGateway.publish(savedOrder);
            }

        } catch (Exception e) {
            order.setStatus(OrderStatus.FAILED);
            order.appendHistoryLog("[FAILED] Error executing step: " + e.getMessage());
            order.setUpdatedAt(LocalDateTime.now());
            repositoryGateway.save(order);
        }
    }
}
