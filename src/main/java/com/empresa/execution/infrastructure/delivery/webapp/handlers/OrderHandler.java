package com.empresa.execution.infrastructure.delivery.webapp.handlers;

import com.empresa.execution.business.domain.OrderEntity;
import com.empresa.execution.business.usecase.CreateOrderUseCase;
import com.empresa.execution.business.usecase.GetOrderByIdUseCase;
import com.empresa.execution.infrastructure.delivery.webapp.requests.CreateOrderRequest;
import com.empresa.execution.infrastructure.delivery.webapp.responses.OrderResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderHandler {

    private final CreateOrderUseCase createOrderUseCase;
    private final GetOrderByIdUseCase getOrderByIdUseCase;

    public OrderHandler(CreateOrderUseCase createOrderUseCase, GetOrderByIdUseCase getOrderByIdUseCase) {
        this.createOrderUseCase = createOrderUseCase;
        this.getOrderByIdUseCase = getOrderByIdUseCase;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderEntity order = createOrderUseCase.execute(request.customerId(), request.totalAmount());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(order));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable UUID id) {
        return getOrderByIdUseCase.execute(id)
                .map(order -> ResponseEntity.ok(toResponse(order)))
                .orElse(ResponseEntity.notFound().build());
    }

    private OrderResponse toResponse(OrderEntity order) {
        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getTotalAmount(),
                order.getStatus().name(),
                order.getHistoryLog(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
