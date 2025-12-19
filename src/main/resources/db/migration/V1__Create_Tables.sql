-- Create Users Table
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create API Tokens Table
CREATE TABLE api_tokens (
    token VARCHAR(255) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create Transactions Table
CREATE TABLE purchase_transactions (
    id VARCHAR(36) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    description VARCHAR(50) NOT NULL,
    transaction_date DATE NOT NULL,
    amount_usd NUMERIC(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
