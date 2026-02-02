package com.tontin.platform.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.tontin.platform.domain.enums.dart.DartStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "darts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
    
    @Enumerated(EnumType.STRING)
    private DartStatus status;

    @OneToMany(mappedBy = "dart")
    private List<Member> member= new ArrayList<>();
}
