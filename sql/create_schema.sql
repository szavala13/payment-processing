CREATE DATABASE payment;
GRANT ALL PRIVILEGES ON payment.* TO 'admin'@'%';
FLUSH PRIVILEGES;

CREATE DATABASE customer;
GRANT ALL PRIVILEGES ON customer.* TO 'admin'@'%';
FLUSH PRIVILEGES;

use payment;
create table payments
(
    amount           decimal(38, 2),
    product_quantity integer not null,
    id               bigint  not null auto_increment,
    payee            bigint,
    payer            bigint,
    concept          varchar(255),
    payment_status   varchar(255),
    primary key (id)
);

use customer;
CREATE TABLE customers (
    id bigint AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    balance DECIMAL(10, 2) NOT NULL,
    PRIMARY KEY (id)
);

INSERT INTO customers (name, address, phone, balance)
VALUES
    ('Juan Pérez', 'Calle 123, Col. Centro, Mexico D.F.', '55-1234-5678', 1000.00),
    ('María García', 'Av. Universidad 1234, Col. Polanco, Mexico D.F.', '55-8765-4321', 500.00),
    ('Luis López', 'Calle 5678, Col. Condesa, Mexico D.F.', '55-9876-5432', 2000.00),
    ('María Hernández', 'Av. Insurgentes Sur 123, Col. Roma, Mexico D.F.', '55-4321-8765', 1500.00),
    ('Juan García', 'Calle 1234, Col. Coyoacán, Mexico D.F.', '55-5432-1876', 2500.00),
    ('María Pérez', 'Av. Revolución 123, Col. Cuauhtémoc, Mexico D.F.', '55-8765-4321', 1000.00),
    ('Luis López', 'Calle 5678, Col. Condesa, Mexico D.F.', '55-9876-5432', 2000.00),
    ('María Hernández', 'Av. Insurgentes Sur 123, Col. Roma, Mexico D.F.', '55-4321-8765', 1500.00),
    ('Juan García', 'Calle 1234, Col. Coyoacán, Mexico D.F.', '55-5432-1876', 2500.00),
    ('María Pérez', 'Av. Revolución 123, Col. Cuauhtémoc, Mexico D.F.', '55-8765-4321', 1000.00),
    ('Luis López', 'Calle 5678, Col. Condesa, Mexico D.F.', '55-9876-5432', 2000.00),
    ('María Hernández', 'Av. Insurgentes Sur 123, Col. Roma, Mexico D.F.', '55-4321-8765', 1500.00),
    ('Juan García', 'Calle 1234, Col. Coyoacán, Mexico D.F.', '55-5432-1876', 2500.00),
    ('María Pérez', 'Av. Revolución 123, Col. Cuauhtémoc, Mexico D.F.', '55-8765-4321', 1000.00),
    ('Luis López', 'Calle 5678, Col. Condesa, Mexico D.F.', '55-9876-5432', 2000.00),
    ('María Hernández', 'Av. Insurgentes Sur 123, Col. Roma, Mexico D.F.', '55-4321-8765', 1500.00),
    ('Juan García', 'Calle 1234, Col. Coyoacán, Mexico D.F.', '55-5432-1876', 2500.00);