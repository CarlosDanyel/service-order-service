CREATE TABLE IF NOT EXISTS customers
(
    id    VARCHAR(36)  NOT NULL,
    name  VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    CONSTRAINT pk_customers PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS vehicles
(
    id            VARCHAR(36)  NOT NULL,
    license_plate VARCHAR(10)  NOT NULL,
    brand         VARCHAR(100) NOT NULL,
    model         VARCHAR(100) NOT NULL,
    year          INT          NOT NULL,
    color         VARCHAR(50),
    CONSTRAINT pk_vehicles PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS service_orders
(
    id             VARCHAR(36)  NOT NULL,
    order_number   VARCHAR(50)  NOT NULL UNIQUE,
    status         VARCHAR(30)  NOT NULL,
    customer_id    VARCHAR(36)  NOT NULL,
    vehicle_id     VARCHAR(36)  NOT NULL,
    approval_token VARCHAR(36),
    notes          TEXT,
    deleted        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at     DATETIME(6)  NOT NULL,
    updated_at     DATETIME(6)  NOT NULL,
    CONSTRAINT pk_service_orders    PRIMARY KEY (id),
    CONSTRAINT fk_so_customer       FOREIGN KEY (customer_id) REFERENCES customers (id),
    CONSTRAINT fk_so_vehicle        FOREIGN KEY (vehicle_id)  REFERENCES vehicles (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_so_approval_token ON service_orders (approval_token);
CREATE INDEX idx_so_status         ON service_orders (status);
CREATE INDEX idx_so_deleted        ON service_orders (deleted);
CREATE INDEX idx_so_created_at     ON service_orders (created_at);

CREATE TABLE IF NOT EXISTS service_items
(
    id               VARCHAR(36)    NOT NULL,
    service_order_id VARCHAR(36)    NOT NULL,
    name             VARCHAR(255)   NOT NULL,
    description      TEXT,
    price            DECIMAL(10, 2) NOT NULL,
    estimated_hours  DOUBLE,
    CONSTRAINT pk_service_items    PRIMARY KEY (id),
    CONSTRAINT fk_si_service_order FOREIGN KEY (service_order_id)
        REFERENCES service_orders (id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS part_items
(
    id               VARCHAR(36)    NOT NULL,
    service_order_id VARCHAR(36)    NOT NULL,
    name             VARCHAR(255)   NOT NULL,
    part_number      VARCHAR(100),
    quantity         INT            NOT NULL,
    unit_price       DECIMAL(10, 2) NOT NULL,
    CONSTRAINT pk_part_items       PRIMARY KEY (id),
    CONSTRAINT fk_pi_service_order FOREIGN KEY (service_order_id)
        REFERENCES service_orders (id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
