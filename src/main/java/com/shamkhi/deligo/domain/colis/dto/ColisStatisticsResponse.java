package com.shamkhi.deligo.domain.colis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColisStatisticsResponse {
    private long total;
    private long cree;
    private long collecte;
    private long enStock;
    private long enTransit;
    private long livre;
    private long annule;
    private long retourne;
}