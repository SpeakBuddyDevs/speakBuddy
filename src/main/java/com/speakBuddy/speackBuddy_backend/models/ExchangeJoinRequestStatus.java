package com.speakBuddy.speackBuddy_backend.models;

/**
 * Estado de una solicitud de unión a un intercambio público.
 * - PENDING: esperando respuesta del creador
 * - ACCEPTED: el creador aceptó, el usuario ya es participante
 * - REJECTED: el creador rechazó
 */
public enum ExchangeJoinRequestStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}
