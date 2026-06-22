DROP TABLE IF EXISTS cargos_per_container;

CREATE TABLE IF NOT EXISTS cargos_per_container(
    container_id VARCHAR(255) NOT NULL,
    cargos BIGINT NOT NULL,
    PRIMARY KEY (container_id));