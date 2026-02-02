CREATE TABLE carriers (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true
);

INSERT INTO carriers (code, name, active) VALUES
    ('DHL', 'DHL Express', true),
    ('CHRONOPOST', 'Chronopost', true),
    ('COLISSIMO', 'Colissimo (La Poste)', true),
    ('UPS', 'UPS', true),
    ('FEDEX', 'FedEx', true),
    ('LAPOSTE', 'La Poste', true),
    ('GLS', 'GLS', true),
    ('DPD', 'DPD', true),
    ('TNT', 'TNT', true),
    ('MAERSK', 'Maersk Line', true);
