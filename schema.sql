CREATE DATABASE dmart_db;
USE dmart_db;

CREATE TABLE users (
    user_id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100),
    password VARCHAR(50),
    role VARCHAR(20),
    is_active BOOLEAN
);