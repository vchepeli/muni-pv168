-- PostgreSQL initialization script for Car Rental Database
-- Creates the schema and initializes tables

-- Create CUSTOMER table
CREATE TABLE customer (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20),
    email VARCHAR(255)
);

-- Create CAR table
CREATE TABLE car (
    id BIGSERIAL PRIMARY KEY,
    spz VARCHAR(20) NOT NULL UNIQUE,
    brand VARCHAR(100) NOT NULL,
    model VARCHAR(100) NOT NULL,
    color VARCHAR(50),
    seats INTEGER,
    price_per_day DECIMAL(10,2) NOT NULL
);

-- Create RENT table
CREATE TABLE rent (
    id BIGSERIAL PRIMARY KEY,
    car_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    from_date DATE NOT NULL,
    to_date DATE NOT NULL,
    FOREIGN KEY (car_id) REFERENCES car(id) ON DELETE CASCADE,
    FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE CASCADE
);

-- Create indexes for performance
CREATE INDEX idx_customer_name ON customer(last_name, first_name);
CREATE INDEX idx_car_spz ON car(spz);
CREATE INDEX idx_rent_car ON rent(car_id);
CREATE INDEX idx_rent_customer ON rent(customer_id);
CREATE INDEX idx_rent_dates ON rent(from_date, to_date);

-- Insert sample data
INSERT INTO car (spz, brand, model, color, seats, price_per_day)
VALUES
    ('4K7-8899', 'Toyota', 'Corolla', 'White', 5, 50.00),
    ('2P3-5566', 'Honda', 'Civic', 'Black', 5, 55.00),
    ('1A2-3344', 'Volkswagen', 'Golf', 'Silver', 5, 60.00),
    ('5D6-7788', 'BMW', '3 Series', 'Blue', 5, 80.00),
    ('3C4-5577', 'Mercedes', 'C-Class', 'Black', 5, 100.00);

INSERT INTO customer (first_name, last_name, phone_number, email)
VALUES
    ('John', 'Smith', '+1234567890', 'john.smith@example.com'),
    ('Jane', 'Doe', '+1234567891', 'jane.doe@example.com'),
    ('Robert', 'Johnson', '+1234567892', 'robert.johnson@example.com'),
    ('Maria', 'Garcia', '+1234567893', 'maria.garcia@example.com'),
    ('Michael', 'Brown', '+1234567894', 'michael.brown@example.com');
