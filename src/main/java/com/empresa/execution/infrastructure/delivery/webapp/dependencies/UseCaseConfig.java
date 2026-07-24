package com.empresa.execution.infrastructure.delivery.webapp.dependencies;

import com.empresa.execution.business.gateway.ProcessPublisherGateway;
import com.empresa.execution.business.gateway.ProcessRepositoryGateway;
import com.empresa.execution.business.logic.ProcessPaymentLogic;
import com.empresa.execution.business.logic.ReserveInventoryLogic;
import com.empresa.execution.business.logic.StepLogic;
import com.empresa.execution.business.usecase.CreateOrderUseCase;
import com.empresa.execution.business.usecase.ExecuteOrderStepUseCase;
import com.empresa.execution.business.usecase.GetOrderByIdUseCase;
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
