package com.tontin.platform.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import com.tontin.platform.domain.enums.logs.LogsLevel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "logs")
@Getter
@Setter
@Builder
public class Loggin {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    private LogsLevel level;
    private String service;
    private String env;
    @Column(name = "request_id")
    private String requestId;
    private String event;
    private String path;
    private String status;
    private String userEmail;

}
