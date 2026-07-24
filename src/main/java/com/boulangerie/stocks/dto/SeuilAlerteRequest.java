package com.boulangerie.stocks.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeuilAlerteRequest {

    @NotNull
    @Positive
    @DecimalMin("0.00")
    private BigDecimal seuilAlerte;

}