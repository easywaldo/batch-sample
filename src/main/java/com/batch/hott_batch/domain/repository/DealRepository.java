package com.batch.hott_batch.domain.repository;

import com.batch.hott_batch.domain.entity.Deal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DealRepository extends JpaRepository<Deal, String> {
}
