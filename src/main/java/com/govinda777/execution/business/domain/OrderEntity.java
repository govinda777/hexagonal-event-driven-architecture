package com.govinda777.execution.business.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class OrderEntity {
    private UUID id;
    private String customerId;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private String historyLog;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public OrderEntity() {}

    public OrderEntity(UUID id, String customerId, BigDecimal totalAmount, OrderStatus status, String historyLog, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.customerId = customerId;
        this.totalAmount = totalAmount;
        this.status = status;
        this.historyLog = historyLog;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getHistoryLog() {
        return historyLog;
    }

    public void setHistoryLog(String historyLog) {
        this.historyLog = historyLog;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void appendHistoryLog(String logEntry) {
        if (this.historyLog == null || this.historyLog.isEmpty()) {
            this.historyLog = logEntry;
        } else {
            this.historyLog = this.historyLog + "\n" + logEntry;
        }
        this.updatedAt = LocalDateTime.now();
    }
}
