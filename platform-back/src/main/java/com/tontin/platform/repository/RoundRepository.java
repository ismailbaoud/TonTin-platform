package com.tontin.platform.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tontin.platform.domain.Round;

public interface RoundRepository extends JpaRepository<Round, UUID> {
}
