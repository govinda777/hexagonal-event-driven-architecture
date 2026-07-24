package com.empresa.execution.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface SpringDataOrderRepository extends JpaRepository<OrderJpaEntity, UUID> {
}
