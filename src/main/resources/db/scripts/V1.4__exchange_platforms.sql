-- Plataformas de videollamada por intercambio (Zoom, Meet, Discord, Otra: nombre, etc.)
CREATE TABLE exchange_platforms (
    exchange_id BIGINT NOT NULL,
    platform VARCHAR(100) NOT NULL,
    PRIMARY KEY (exchange_id, platform),
    CONSTRAINT fk_exchange_platforms_exchange FOREIGN KEY (exchange_id) REFERENCES exchanges(id) ON DELETE CASCADE
);
