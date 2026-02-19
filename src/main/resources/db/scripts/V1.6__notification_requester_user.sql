-- Columna para identificar al solicitante en notificaciones EXCHANGE_JOIN_REQUEST
ALTER TABLE notifications ADD COLUMN requester_user_id BIGINT NULL;
ALTER TABLE notifications ADD CONSTRAINT fk_notification_requester_user
    FOREIGN KEY (requester_user_id) REFERENCES users(id) ON DELETE SET NULL;
