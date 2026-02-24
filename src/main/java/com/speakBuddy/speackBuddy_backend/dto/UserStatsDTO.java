package com.speakBuddy.speackBuddy_backend.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Estadísticas agregadas para el usuario autenticado, usadas en la Home del
 * frontend para mostrar intercambios y horas por periodo.
 */
@Data
@Builder
public class UserStatsDTO {

    /** Intercambios completados en el mes actual. */
    private Integer exchangesThisMonth;

    /** Intercambios completados en el mes anterior. */
    private Integer exchangesLastMonth;

    /** Horas totales de intercambios completados en la semana actual. */
    private Double hoursThisWeek;

    /** Horas totales de intercambios completados en la semana anterior. */
    private Double hoursLastWeek;
}

