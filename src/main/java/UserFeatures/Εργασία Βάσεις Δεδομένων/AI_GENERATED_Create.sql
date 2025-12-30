
CREATE TABLE Region (
    RegionID INT PRIMARY KEY,
    Name VARCHAR(100),
    Population INT,
    AvgIncome DECIMAL(10,2)
);

CREATE TABLE Customer (
    CustomerID INT PRIMARY KEY,
    FirstName VARCHAR(100),
    LastName VARCHAR(100),
    Address VARCHAR(200),
    AFM VARCHAR(20),
    Phone VARCHAR(20),
    RegionID INT,
    FOREIGN KEY (RegionID) REFERENCES Region(RegionID)
);

CREATE TABLE Account (
    AccountID INT PRIMARY KEY,
    Balance DECIMAL(10,2),
    CreatedDate DATE,
    BranchName VARCHAR(100),
    CustomerID INT,
    AccountType VARCHAR(20),
    FOREIGN KEY (CustomerID) REFERENCES Customer(CustomerID)
);

CREATE TABLE SavingsAccount (
    AccountID INT PRIMARY KEY,
    InterestRate DECIMAL(5,2),
    FOREIGN KEY (AccountID) REFERENCES Account(AccountID)
);

CREATE TABLE CheckingAccount (
    AccountID INT PRIMARY KEY,
    OverdraftLimit DECIMAL(10,2),
    FOREIGN KEY (AccountID) REFERENCES Account(AccountID)
);

CREATE TABLE CreditCard (
    CardID INT PRIMARY KEY,
    IssueDate DATE,
    ExpiryDate DATE,
    CreditLimit DECIMAL(10,2),
    LoanRate DECIMAL(5,2),
    Balance DECIMAL(10,2),
    CustomerID INT,
    AccountID INT,
    FOREIGN KEY (CustomerID) REFERENCES Customer(CustomerID),
    FOREIGN KEY (AccountID) REFERENCES Account(AccountID)
);

CREATE TABLE Store (
    StoreID INT PRIMARY KEY,
    Name VARCHAR(100),
    ServiceType INT,
    RegionID INT,
    FOREIGN KEY (RegionID) REFERENCES Region(RegionID)
);

CREATE TABLE Transactions (
    TransactionID INT PRIMARY KEY,
    Amount DECIMAL(10,2),
    TransactionDate DATETIME,
    StoreID INT,
    ClearingBankCode VARCHAR(50),
    CardID INT,
    FOREIGN KEY (StoreID) REFERENCES Store(StoreID),
    FOREIGN KEY (CardID) REFERENCES CreditCard(CardID)
);

CREATE TABLE Payment (
    CustomerID INT,
    PaymentNumber INT,
    PaymentDate DATE,
    Amount DECIMAL(10,2),
    PRIMARY KEY (CustomerID, PaymentNumber),
    FOREIGN KEY (CustomerID) REFERENCES Customer(CustomerID)
);
