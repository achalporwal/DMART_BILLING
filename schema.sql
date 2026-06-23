
-- Drop tables if they exist (for clean setup)
DROP TABLE IF EXISTS bill_items;
DROP TABLE IF EXISTS bills;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS customers;
DROP TABLE IF EXISTS users;

-- 1. Users Table (Sub-Admins and Super-Admins)
CREATE TABLE users (
    user_id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL, -- SUPER_ADMIN, SUB_ADMIN
    password VARCHAR(100) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT 1
);

-- 1b. Cashier Drafts Table
CREATE TABLE cashier_drafts (
    cashier_id VARCHAR(50) PRIMARY KEY,
    draft_json TEXT NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (cashier_id) REFERENCES users(user_id)
);

-- 2. Customers Table
CREATE TABLE customers (
    customer_id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    mobile_number VARCHAR(15) UNIQUE NOT NULL,
    age INT,
    location VARCHAR(100)
);


-- 3. Products Table
CREATE TABLE products (
    product_id VARCHAR(50) PRIMARY KEY,
    product_name VARCHAR(150) NOT NULL,
    mrp DECIMAL(10, 2) NOT NULL,            -- Maximum Retail Price
    prp DECIMAL(10, 2) NOT NULL,            -- Purchase Retail Price (D-Mart Price)
    gst_percentage DECIMAL(5, 2) NOT NULL,   -- GST percentage (e.g. 5.00, 12.00, 18.00)
    available_quantity INT NOT NULL DEFAULT 0,
    alert_threshold INT NOT NULL DEFAULT 10,
    held_quantity INT NOT NULL DEFAULT 0
);

-- 4. Bills Table
CREATE TABLE bills (
    bill_id VARCHAR(50) PRIMARY KEY,
    customer_id VARCHAR(50),
    cashier_id VARCHAR(50) NOT NULL,
    bill_date TIMESTAMP NOT NULL,
    taxable_value DECIMAL(10, 2) NOT NULL,
    cgst DECIMAL(10, 2) NOT NULL,
    sgst DECIMAL(10, 2) NOT NULL,
    discount DECIMAL(10, 2) NOT NULL,
    final_amount DECIMAL(10, 2) NOT NULL,
    payment_mode VARCHAR(20) NOT NULL DEFAULT 'CASH',
    cash_received DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    cash_returned DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    FOREIGN KEY (cashier_id) REFERENCES users(user_id)
);

-- 5. Bill Items Table
CREATE TABLE bill_items (
    bill_item_id VARCHAR(50) PRIMARY KEY,
    bill_id VARCHAR(50) NOT NULL,
    product_id VARCHAR(50) NOT NULL,
    quantity INT NOT NULL,
    mrp DECIMAL(10, 2) NOT NULL,
    prp DECIMAL(10, 2) NOT NULL,
    taxable_value DECIMAL(10, 2) NOT NULL,
    cgst DECIMAL(10, 2) NOT NULL,
    sgst DECIMAL(10, 2) NOT NULL,
    discount DECIMAL(10, 2) NOT NULL,
    final_amount DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (bill_id) REFERENCES bills(bill_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);

-- Seed Initial Data
INSERT INTO users (user_id, name, role, password) VALUES 
('admin1', 'Super Admin Head', 'SUPER_ADMIN', 'admin123'),
('cashier1', 'Sub Admin Cashier 1', 'SUB_ADMIN', 'cashier123'),
('cashier2', 'Sub Admin Cashier 2', 'SUB_ADMIN', 'cashier123');

INSERT INTO customers (customer_id, name, mobile_number, age, location) VALUES
('cust1', 'Ramesh Kumar', '9876543210', 34, 'Mumbai'),
('cust2', 'Sita Sharma', '8765432109', 28, 'Pune');

INSERT INTO products (product_id, product_name, mrp, prp, gst_percentage, available_quantity) VALUES
('P1001', 'D-Mart Premium Basmati Rice 5kg', 600.00, 480.00, 5.00, 100),
('P1002', 'Surf Excel Easy Wash 1kg', 140.00, 120.00, 18.00, 200),
('P1003', 'Tata Salt 1kg', 25.00, 22.00, 0.00, 500),
('P1004', 'Cadbury Dairy Milk Silk', 80.00, 72.00, 18.00, 150),
('P1005', 'Fortune Sunflower Oil 1L', 160.00, 145.00, 5.00, 300),
('P1006', 'Dettol Liquid Handwash 750ml', 99.00, 85.00, 18.00, 80);
