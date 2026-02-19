-- Solicitudes de unión a intercambios públicos (usuarios no elegibles que piden unirse)
-- Un usuario solo puede tener una solicitud por intercambio (evita duplicados pendientes).

CREATE TABLE exchange_join_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    exchange_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    responded_at DATETIME(6) NULL,
    responded_by_id BIGINT NULL,
    CONSTRAINT fk_join_request_exchange FOREIGN KEY (exchange_id) REFERENCES exchanges(id) ON DELETE CASCADE,
    CONSTRAINT fk_join_request_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_join_request_responded_by FOREIGN KEY (responded_by_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT uq_exchange_user_join_request UNIQUE (exchange_id, user_id)
);

CREATE INDEX idx_join_requests_exchange_status ON exchange_join_requests(exchange_id, status);
