package com.tontin.platform.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tontin.platform.domain.Dart;

public interface DartRepository extends JpaRepository<Dart, UUID>{
    
}