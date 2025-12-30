
INSERT INTO Region VALUES (1, 'Athens', 3000000, 18000);
INSERT INTO Region VALUES (2, 'Thessaloniki', 1000000, 15000);

INSERT INTO Customer VALUES (1, 'Nikos', 'Papadopoulos', 'Athinas 10', '123456789', '2101111111', 1);
INSERT INTO Customer VALUES (2, 'Maria', 'Ioannou', 'Tsimiski 20', '987654321', '2310111111', 2);

INSERT INTO Account VALUES (100, 1500.50, '2020-01-01', 'Athens Central', 1, 'Savings');
INSERT INTO SavingsAccount VALUES (100, 1.5);

INSERT INTO Account VALUES (101, 500.00, '2019-05-05', 'Athens Central', 1, 'Checking');
INSERT INTO CheckingAccount VALUES (101, 300.00);

INSERT INTO CreditCard VALUES (5000, '2022-01-01', '2027-01-01', 2000.00, 12.5, 150.00, 1, 100);

INSERT INTO Store VALUES (10, 'ElectroWorld', 1, 1);

INSERT INTO Transactions VALUES (9000, 120.00, '2023-06-01 10:30:00', 10, 'BankXYZ', 5000);

INSERT INTO Payment VALUES (1, 1, '2023-06-05', 50.00);
