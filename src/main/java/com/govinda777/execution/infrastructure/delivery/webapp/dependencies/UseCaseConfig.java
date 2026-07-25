package com.govinda777.execution.infrastructure.delivery.webapp.dependencies;

import com.govinda777.execution.business.gateway.ProcessPublisherGateway;
import com.govinda777.execution.business.gateway.ProcessRepositoryGateway;
import com.govinda777.execution.business.logic.ProcessPaymentLogic;
import com.govinda777.execution.business.logic.ReserveInventoryLogic;
import com.govinda777.execution.business.logic.StepLogic;
import com.govinda777.execution.business.usecase.CreateOrderUseCase;
import com.govinda777.execution.business.usecase.ExecuteOrderStepUseCase;
import com.govinda777.execution.business.usecase.GetOrderByIdUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class UseCaseConfig {

    @Bean
    public ProcessPaymentLogic processPaymentLogic() {
        return new ProcessPaymentLogic();
    }

    @Bean
    public ReserveInventoryLogic reserveInventoryLogic() {
        return new ReserveInventoryLogic();
    }

    @Bean
    public CreateOrderUseCase createOrderUseCase(ProcessRepositoryGateway repositoryGateway,
                                                 ProcessPublisherGateway publisherGateway) {
        return new CreateOrderUseCase(repositoryGateway, publisherGateway);
    }

    @Bean
    public ExecuteOrderStepUseCase executeOrderStepUseCase(ProcessRepositoryGateway repositoryGateway,
                                                           ProcessPublisherGateway publisherGateway,
                                                           List<StepLogic> steps) {
        return new ExecuteOrderStepUseCase(repositoryGateway, publisherGateway, steps);
    }

    @Bean
    public GetOrderByIdUseCase getOrderByIdUseCase(ProcessRepositoryGateway repositoryGateway) {
        return new GetOrderByIdUseCase(repositoryGateway);
    }
}
