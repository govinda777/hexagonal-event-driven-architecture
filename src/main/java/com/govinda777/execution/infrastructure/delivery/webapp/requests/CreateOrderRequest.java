package com.govinda777.execution.infrastructure.delivery.webapp.requests;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateOrderRequest(
    @NotBlank(message = "CustomerId must not be blank")
    String customerId,

    @NotNull(message = "TotalAmount must not be null")
    @DecimalMin(value = "0.01", message = "TotalAmount must be greater than zero")
    BigDecimal totalAmount
) {}
