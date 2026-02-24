package com.speakBuddy.speackBuddy_backend.models;

/**
 * Estado de un intercambio de idiomas.
 * - SCHEDULED: programado, pendiente de celebrarse
 * - ENDED_PENDING_CONFIRMATION: ya pasó la hora, esperando que todos confirmen
 * - COMPLETED: todos han confirmado, intercambio completado
 * - CANCELLED: cancelado
 */
public enum ExchangeStatus {
    SCHEDULED,
    ENDED_PENDING_CONFIRMATION,
    COMPLETED,
    CANCELLED
}
