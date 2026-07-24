package com.empresa.execution.infrastructure.delivery.webapp.responses;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderResponse(
    UUID id,
    String customerId,
    BigDecimal totalAmount,
    String status,
    String historyLog,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
