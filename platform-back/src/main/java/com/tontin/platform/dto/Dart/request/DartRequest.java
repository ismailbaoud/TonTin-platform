package com.tontin.platform.dto.Dart.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DartRequest {

    @NotNull(message = "the name shoulden't be null")
    @NotBlank(message = "the name is required")
    private String name;

    @NotNull(message = "Monthly contribution is required")
    @DecimalMin(value = "0.01", message = "Monthly contribution must be greater than zero")
    private BigDecimal monthlyContribution;

    @NotNull(message = "the allocation method shouldent be null")
    @NotBlank(message = "the allocation method is required")
    private String allocationMethod;
    
}
