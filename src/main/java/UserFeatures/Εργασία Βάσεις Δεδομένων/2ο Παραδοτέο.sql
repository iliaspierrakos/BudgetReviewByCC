USE DB_PROJECT
-- 1 --
SELECT CustomerCode, SSN, FirstName, LastName, Address, TelephoneNumber
FROM Customer
-- 2 --
SELECT CardNumber, Date
FROM Transactions
WHERE Date BETWEEN '2017-05-12' AND '2017-05-18'
-- 3 --
SELECT C.CustomerCode, FirstName, LastName, AccountNumber
FROM Customer AS C, Account AS A
WHERE C.CustomerCode = A.CustomerCode
-- 4 --
SELECT DISTINCT FirstName, LastName, TelephoneNumber
FROM Customer AS C, CreditCard AS CC, Transactions AS T, Store
WHERE Store.AreaCode = 291 
    AND T.Date BETWEEN '2017-06-01' AND '2025-06-30'
    AND C.CustomerCode = CC.CustomerCode
    AND CC.CardNumber = T.cardNumber
    AND T.StoreCode = Store.StoreCode

-- 5 --
SELECT CardNumber 
FROM CreditCard
WHERE DATEDIFF(MONTH, GETDATE(), ExpirationDate) = 1
-- 6 --
UPDATE CreditCard
SET LendingRate = LendingRate - 1 
-- 7 --
SELECT C.FirstName, LastName, C.CustomerCode
FROM Customer AS C, Account
WHERE C.CustomerCode = Account.CustomerCode 
GROUP BY C.CustomerCode, c.FirstName, C.LastName
HAVING sum(balance) > 10000
-- 8 --
SELECT month(Date) AS Month, sum(Amount) AS TransactionSum
FROM Transactions
WHERE year(Date) = 2017
GROUP BY month(Date)
-- 9 -- 
SELECT FirstName, LastName, month(Date) AS Month, sum(T.Amount) AS TotalAmount
FROM Customer AS C, Transactions AS T, CreditCard AS CC
WHERE year(Date) = 2017 AND C.CustomerCode = CC.CustomerCode AND T.CardNumber = CC.CardNumber
GROUP BY LastName, FirstName, month(Date)
ORDER BY LastName, month(Date)
-- 10 -- 
SELECT distinct C.CustomerCode
FROM Customer AS C, CreditCard AS CC1, Transactions AS T
WHERE C.CustomerCode = CC1.CustomerCode 
	  AND CC1.CardNumber = T.CardNumber 
	  AND (T.Amount > all (SELECT Amount
		   FROM Transactions AS T2, CreditCard AS CC2
	       WHERE T2.CardNumber = CC2.CardNumber 
		   AND CC1.CardNumber <> CC2.CardNumber ))
-- 11 -- 
SELECT C.CustomerCode, FirstName, LastName
FROM Customer AS C, Transactions AS T, CreditCard AS CC
WHERE month(Date) = 6 AND year(Date) = 2017
	AND C.CustomerCode = CC.CustomerCode AND T.CardNumber = CC.CardNumber
GROUP BY C.CustomerCode, FirstName, LastName
HAVING avg(Amount) > 50 AND count(ConfirmationNumber) > 5
-- 12 --
SELECT C.CustomerCode, GA.AreaCode, 100*sum(Amount)/AverageSalary AS RESULT
FROM Customer AS C, GeographicalArea AS GA, Transactions AS T, CreditCard AS CC
WHERE C.CustomerCode = CC.CustomerCode 
	AND year(Date) = 2017 
	AND CC.CardNumber = T.CardNumber 
	AND GA.AreaCode = C.AreaCode 
GROUP BY GA.AreaCode, C.CustomerCode, AverageSalary
-- 13 --
SELECT C1.FirstName, C1.LastName
FROM Customer AS C1, CreditCard AS CC, Transactions AS T
WHERE month(Date) = 6 
	AND year(Date) = 2017 
	AND C1.CustomerCode = CC.CustomerCode
	AND CC.CardNumber = T.CardNumber
GROUP BY C1.FirstName, C1.LastName
HAVING avg(Amount) > 3 * (
    SELECT avg(Amount)
    FROM Transactions
    WHERE month(Date) = 6 AND year(Date) = 2017 )

-- 14 --
DROP VIEW IF EXISTS JuneSum16
DROP VIEW IF EXISTS JuneSum17
GO
CREATE VIEW JuneSum16 (CustomerCode, SumTransactions) AS
SELECT C.CustomerCode, sum(Amount) 
FROM Customer AS C, Transactions AS T, CreditCard AS CC
WHERE month(Date) = 6 AND year(Date) = 2016
	AND C.CustomerCode = CC.CustomerCode AND T.CardNumber = CC.CardNumber
GROUP BY C.CustomerCode
GO

CREATE VIEW JuneSum17 AS
SELECT C.CustomerCode, sum(Amount) AS SumTransactions2017
FROM Customer AS C, Transactions AS T, CreditCard AS CC
WHERE month(Date) = 6 AND year(Date) = 2017
	AND C.CustomerCode = CC.CustomerCode AND T.CardNumber = CC.CardNumber
GROUP BY C.CustomerCode
GO

SELECT DISTINCT C.CustomerCode, J17.SumTransactions2017
FROM Customer AS C, CreditCard AS CC, Transactions AS T,JuneSum16 AS J16, JuneSum17 AS J17
WHERE C.CustomerCode = J16.CustomerCode
AND C.CustomerCode = J17.CustomerCode
AND J17.SumTransactions2017 >= 1.5 * J16.SumTransactions

-- 15 --
SELECT CustomerCode, Month, AvgBefore, AvgAfter
FROM (
    SELECT C.CustomerCode, 
           MONTH(T.Date) AS Month,
           (SELECT AVG(T2.Amount)
            FROM CreditCard CC2, Transactions T2
            WHERE CC2.CardNumber = T2.CardNumber
              AND CC2.CustomerCode = C.CustomerCode 
              AND YEAR(T2.Date) = 2017 
              AND MONTH(T2.Date) < MONTH(T.Date)) AS AvgBefore,
           (SELECT AVG(T3.Amount)
            FROM CreditCard CC3, Transactions T3
            WHERE CC3.CardNumber = T3.CardNumber
              AND CC3.CustomerCode = C.CustomerCode 
              AND YEAR(T3.Date) = 2017 
              AND MONTH(T3.Date) <= MONTH(T.Date)) AS AvgAfter
    
    FROM Customer C, CreditCard CC, Transactions T
    WHERE C.CustomerCode = CC.CustomerCode
      AND CC.CardNumber = T.CardNumber
      AND YEAR(T.Date) = 2017
    GROUP BY C.CustomerCode, MONTH(T.Date)
) AS MonthlyData
WHERE AvgAfter > AvgBefore

-- 16 --
DROP VIEW IF EXISTS TransactionsSum2017
DROP VIEW IF EXISTS PaymentsSum2017
GO
CREATE VIEW TransactionsSum2017 AS
SELECT C1.CustomerCode, sum(Amount) AS TransSum2017
    FROM Transactions AS T17, CreditCard AS CC17, Customer AS C1
    WHERE year(Date) = 2017
    AND C1.CustomerCode = CC17.CustomerCode
    AND CC17.CardNumber = T17.CardNumber
    GROUP BY C1.CustomerCode
GO
CREATE VIEW PaymentsSum2017 AS 
SELECT C2.CustomerCode, sum(PaymentAmount) AS PaySum2017
    FROM Payment AS P17, Customer AS C2
    WHERE year(Date) = 2017
    AND C2.CustomerCode = P17.CustomerCode
    GROUP BY C2.CustomerCode
GO 
SELECT C.CustomerCode, TransSum2017, PaySum2017
FROM Customer AS C, PaymentsSum2017, TransactionsSum2017
WHERE PaymentsSum2017.PaySum2017 > TransactionsSum2017.TransSum2017
AND PaymentsSum2017.CustomerCode = C.CustomerCode
AND TransactionsSum2017.CustomerCode = C.CustomerCode
