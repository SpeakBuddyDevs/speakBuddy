-- Token único para intercambios privados accesibles solo por enlace
ALTER TABLE exchanges ADD COLUMN share_token VARCHAR(64) NULL;
CREATE UNIQUE INDEX uq_exchanges_share_token ON exchanges(share_token);
