package com.tontin.platform.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.tontin.platform.domain.enums.dart.DartStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "darts")
@Getter
@Setter
@Builder
public class Dart {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uuid;
    
    @Column(name = "name")
    private String name;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "monthly_contribution")
    private BigDecimal monthlyContribution;


    @Column(name = "allocation_method")
    private String allocationMethod;

    private DartStatus status;
}
