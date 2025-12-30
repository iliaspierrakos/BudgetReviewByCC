USE DB_PROJECT

DROP TABLE IF EXISTS Transactions
DROP TABLE IF EXISTS Payment
DROP TABLE IF EXISTS CreditCard 
DROP TABLE IF EXISTS CurrentAccount
DROP TABLE IF EXISTS SavingsAccount
DROP TABLE IF EXISTS ACCOUNT
DROP TABLE IF EXISTS Customer
DROP TABLE IF EXISTS Store
DROP TABLE IF EXISTS GeographicalArea
DROP VIEW IF EXISTS TransactionsSum2017
DROP VIEW IF EXISTS PaymentsSum2017
DROP VIEW IF EXISTS JuneSum16
DROP VIEW IF EXISTS JuneSum17

CREATE TABLE GeographicalArea (
    AreaCode BIGINT PRIMARY KEY,
    AreaName VARCHAR(200) NOT NULL,
    Population BIGINT NOT NULL,
    AverageSalary DECIMAL(15,2), 
    CONSTRAINT CHK_Population CHECK (population >= 0),
    CONSTRAINT CHK_AverageSalary CHECK (AverageSalary >= 0)
)
CREATE TABLE Store (
    StoreCode BIGINT PRIMARY KEY,
    Name VARCHAR(200) NOT NULL, 
    ServiceType VARCHAR(20) NOT NULL,
    AreaCode BIGINT,
    CONSTRAINT FK_Store_GepgraphicalArea FOREIGN KEY (AreaCode) REFERENCES GeographicalArea(AreaCode)
)
CREATE TABLE Customer (
    CustomerCode VARCHAR(10) PRIMARY KEY,
    Address VARCHAR(200) NULL,
    FirstName VARCHAR(200) NOT NULL,
    LastName VARCHAR(200) NOT NULL,
    SSN CHAR(4) NOT NULL, 
    TelephoneNumber VARCHAR(10) NULL,
    AreaCode BIGINT FOREIGN KEY (AreaCode) REFERENCES GeographicalArea(AreaCode)
)
CREATE TABLE Account (
    AccountNumber CHAR(20) NOT NULL PRIMARY KEY,
    Balance DECIMAL(15,2) NOT NULL DEFAULT 0,
    CreationDate DATE NOT NULL,
    StoreName VARCHAR(100) NOT NULL,
    CustomerCode VARCHAR(10) NOT NULL,
    CONSTRAINT FK_Account_Customer FOREIGN KEY (CustomerCode) REFERENCES Customer(CustomerCode)
)
CREATE TABLE SavingsAccount (
    AccountNumber CHAR(20) PRIMARY KEY,
    InterestRate DECIMAL(5,4) NOT NULL,
    CONSTRAINT FK_SavingsAccount_Account FOREIGN KEY (AccountNumber) REFERENCES Account(AccountNumber)
        ON DELETE CASCADE
)
CREATE TABLE CurrentAccount (
    AccountNumber CHAR(20) PRIMARY KEY,
    OverdraftAmount DECIMAL(15,2) NOT NULL DEFAULT 0,
    CONSTRAINT FK_CurrentAccount_Account FOREIGN KEY (AccountNumber) REFERENCES Account(AccountNumber)
        ON DELETE CASCADE
)
CREATE TABLE CreditCard (
    CardNumber BIGINT NOT NULL PRIMARY KEY,
    DateOfIssue DATE NOT NULL,
    ExpirationDate DATE NOT NULL,
    CreditLimit DECIMAL(14,2),
    LendingRate DECIMAL(5,2),
    Balance DECIMAL(14,2),
    CustomerCode VARCHAR(10) NOT NULL,
    AccountNumber CHAR(20) NOT NULL, 
    CONSTRAINT FK_CreditCard_Customer FOREIGN KEY (CustomerCode) REFERENCES Customer(CustomerCode),
    CONSTRAINT FK_CreditCard_Account FOREIGN KEY (AccountNumber) REFERENCES Account(AccountNumber),
    CONSTRAINT CHK_CreditLimit CHECK (CreditLimit >= 0),
    CONSTRAINT CHK_LendingRate CHECK (LendingRate >= 0)
)
CREATE TABLE Payment (
    CustomerCode VARCHAR(10),
    SerialNumber BIGINT,
    date DATE NOT NULL,
    PaymentAmount DECIMAL(12,2), 
    CONSTRAINT PK_Payment PRIMARY KEY (CustomerCode, SerialNumber),
    CONSTRAINT FK_Payment_Customer FOREIGN KEY (CustomerCode) REFERENCES Customer(CustomerCode),
    CONSTRAINT CHK_PaymentAmount CHECK (PaymentAmount >= 0)
)
GO
CREATE TRIGGER trg_Payment_SerialNumber
ON Payment
INSTEAD OF INSERT
AS
BEGIN
    SET NOCOUNT ON;
    
    INSERT INTO Payment (CustomerCode, SerialNumber, date, PaymentAmount)
    SELECT 
        i.CustomerCode,
        ISNULL((SELECT MAX(SerialNumber) FROM Payment WHERE CustomerCode = i.CustomerCode), 0) + 1,
        i.date,
        i.PaymentAmount
    FROM inserted i
END
GO

CREATE TABLE Transactions(
    ConfirmationNumber BIGINT PRIMARY KEY,
    Date DATE NOT NULL,
    Time TIME NOT NULL,
    BankCode BIGINT NOT NULL,
    Amount NUMERIC(12,2),
    CardNumber BIGINT NOT NULL,
    StoreCode BIGINT NULL,
    CONSTRAINT FK_Transactions_CreditCard FOREIGN KEY (CardNumber) REFERENCES CreditCard(CardNumber),
    CONSTRAINT FK_Transactions_Store FOREIGN KEY (StoreCode) REFERENCES Store(StoreCode),
    CONSTRAINT CHK_Amount CHECK (Amount > 0),
)
