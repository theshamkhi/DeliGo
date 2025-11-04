package com.toctoc.toctoc2.domain.colis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ColisStatisticsDTO {
    private String entityId;
    private String entityName;
    private Long count;
    private BigDecimal totalWeight;
}