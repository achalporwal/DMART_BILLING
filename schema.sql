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
