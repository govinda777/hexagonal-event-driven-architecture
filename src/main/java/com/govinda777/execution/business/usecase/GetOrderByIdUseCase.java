package com.govinda777.execution.business.usecase;

import com.govinda777.execution.business.domain.OrderEntity;
import com.govinda777.execution.business.gateway.ProcessRepositoryGateway;

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
