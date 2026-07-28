CREATE DATABASE dmart_db;
USE dmart_db;

CREATE TABLE users (
    user_id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100),
    password VARCHAR(50),
    role VARCHAR(20),
    is_active BOOLEAN
);

CREATE TABLE customers (
    customer_id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100),
    mobile_number VARCHAR(15) UNIQUE,
    age INT,
    location VARCHAR(100)
);

CREATE TABLE products (
    product_id VARCHAR(50) PRIMARY KEY,
    product_name VARCHAR(100),
    mrp DECIMAL(10,2),
    prp DECIMAL(10,2),
    gst_percentage DECIMAL(5,2),
    available_quantity INT,
    alert_threshold INT DEFAULT 10,
    held_quantity INT DEFAULT 0
);

CREATE TABLE bills (
    bill_id VARCHAR(50) PRIMARY KEY,
    customer_id VARCHAR(50),
    cashier_id VARCHAR(50),
    bill_date DATETIME,
    payment_mode VARCHAR(20),
    taxable_value DECIMAL(10,2),
    cgst DECIMAL(10,2),
    sgst DECIMAL(10,2),
    discount DECIMAL(10,2),
    final_amount DECIMAL(10,2),
    cash_received DECIMAL(10,2),
    cash_returned DECIMAL(10,2),
    status VARCHAR(20),
    customer_name VARCHAR(100),
    customer_mobile VARCHAR(15),
    customer_location VARCHAR(100),
    FOREIGN KEY (cashier_id) REFERENCES users(user_id),
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
);

CREATE TABLE bill_items (
    item_id VARCHAR(50) PRIMARY KEY,
    bill_id VARCHAR(50),
    product_id VARCHAR(50),
    product_name VARCHAR(100),
    quantity INT,
    mrp DECIMAL(10,2),
    prp DECIMAL(10,2),
    taxable_value DECIMAL(10,2),
    cgst DECIMAL(10,2),
    sgst DECIMAL(10,2),
    discount DECIMAL(10,2),
    final_amount DECIMAL(10,2),
    FOREIGN KEY (bill_id) REFERENCES bills(bill_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);


INSERT INTO users (user_id, name, password, role, is_active) 
VALUES ('USR-001', 'Achal Admin', 'admin123', 'ADMIN', true);

Show tables;
