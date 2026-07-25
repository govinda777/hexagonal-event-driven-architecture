package com.govinda777.execution.infrastructure.delivery.webapp.messages;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderMessage(
    UUID orderId,
    String currentStatus,
    LocalDateTime eventTimestamp
) implements Serializable {}
