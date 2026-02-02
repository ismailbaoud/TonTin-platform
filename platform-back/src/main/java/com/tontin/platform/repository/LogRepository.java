package com.tontin.platform.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tontin.platform.domain.Loggin;

public interface LogRepository extends JpaRepository<Loggin, UUID>{
    
}
