package com.govinda777.execution.infrastructure.delivery.webapp.handlers;

import com.govinda777.execution.business.domain.OrderEntity;
import com.govinda777.execution.business.usecase.CreateOrderUseCase;
import com.govinda777.execution.business.usecase.GetOrderByIdUseCase;
import com.govinda777.execution.infrastructure.delivery.webapp.requests.CreateOrderRequest;
import com.govinda777.execution.infrastructure.delivery.webapp.responses.OrderResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    @GetMapping(value = "/reports/bdd", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getBddReport() {
        Path reportPath = Paths.get("target/cucumber-reports/cucumber.html");
        if (!Files.exists(reportPath)) {
            String fallbackHtml = "<html>" +
                    "<head><title>BDD Report Error</title><meta charset='utf-8'></head>" +
                    "<body style='font-family: Arial, sans-serif; padding: 40px; text-align: center; color: #333;'>" +
                    "<h2>📊 Relatório BDD não disponível</h2>" +
                    "<p>O arquivo do relatório de testes Cucumber não foi localizado em <code>target/cucumber-reports/cucumber.html</code>.</p>" +
                    "<p>Por favor, execute o comando <strong><code>mvn test</code></strong> para gerar o relatório localmente antes de visualizá-lo.</p>" +
                    "</body>" +
                    "</html>";
            return ResponseEntity.ok(fallbackHtml);
        }

        try {
            String htmlContent = Files.readString(reportPath);
            return ResponseEntity.ok(htmlContent);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao carregar o relatório BDD: " + e.getMessage());
        }
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
