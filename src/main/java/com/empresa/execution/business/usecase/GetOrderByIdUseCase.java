package com.empresa.execution.business.usecase;

import com.empresa.execution.business.domain.OrderEntity;
import com.empresa.execution.business.gateway.ProcessRepositoryGateway;

import java.util.Optional;
import java.util.UUID;

public class GetOrderByIdUseCase {

    private final ProcessRepositoryGateway repositoryGateway;

    public GetOrderByIdUseCase(ProcessRepositoryGateway repositoryGateway) {
        this.repositoryGateway = repositoryGateway;
    }

    public Optional<OrderEntity> execute(UUID id) {
        return repositoryGateway.findById(id);
    }
}
