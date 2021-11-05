package com.batch.hott_batch.domain.repository;

import com.batch.hott_batch.domain.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<Member, Long> {
}
