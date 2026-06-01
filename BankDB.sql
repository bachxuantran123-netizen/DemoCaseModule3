USE BankDB;
GO

CREATE TABLE Users (
    username VARCHAR(50) PRIMARY KEY,
    password VARCHAR(50) NOT NULL,
    full_name NVARCHAR(100)
);
INSERT INTO Users VALUES ('admin', '123456', N'Nhân Viên');
INSERT INTO Users VALUES ('admin1', '123456', N'Nhân Viên 1');
INSERT INTO Users VALUES ('admin2', '123456', N'Nhân Viên 2');
GO

CREATE TABLE Customers (
    customer_id INT IDENTITY(1,1) PRIMARY KEY,
    citizen_id VARCHAR(20) UNIQUE NOT NULL,
    full_name NVARCHAR(100) NOT NULL,
);
GO

CREATE TABLE Accounts (
    id INT IDENTITY(1,1) PRIMARY KEY,
    account_code VARCHAR(10) UNIQUE NOT NULL,
    customer_id INT NOT NULL,
    creation_date DATE,
    type VARCHAR(20),
    
    -- Savings
    deposit_amount FLOAT,
    deposit_date DATE,
    interest_rate FLOAT,
    term INT,
    
    -- Payment
    card_number VARCHAR(20),
    balance FLOAT,

    FOREIGN KEY (customer_id) REFERENCES Customers(customer_id)
);
GO

CREATE TABLE TransactionTypes (
    type_id INT PRIMARY KEY,
    type_name NVARCHAR(50) NOT NULL
);
INSERT INTO TransactionTypes VALUES (1, N'Nạp tiền'), (2, N'Rút tiền'), (3, N'Lãi suất');
GO

CREATE TABLE Transactions (
    trans_id INT IDENTITY(1,1) PRIMARY KEY,
    account_code VARCHAR(10) NOT NULL,
    type_id INT NOT NULL,
    amount FLOAT NOT NULL,
    trans_date DATETIME DEFAULT GETDATE(),
    description NVARCHAR(200),
    
    FOREIGN KEY (account_code) REFERENCES Accounts(account_code) ON DELETE CASCADE,
    FOREIGN KEY (type_id) REFERENCES TransactionTypes(type_id)
);
GO

CREATE TABLE ActivityLogs (
    log_id INT IDENTITY(1,1) PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    action NVARCHAR(200),
    log_time DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (username) REFERENCES Users(username) ON DELETE CASCADE
);
GO