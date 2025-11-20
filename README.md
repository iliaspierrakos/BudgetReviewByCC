# Prime Minister for a day
## Overview
This project is a Java-based application that allows users to **view, analyze, and modify the state budget** as if they were the Prime Minister for a day.
It is designed as a command-line interface (CLI) program with optional graphical extensions. 
The goal is to give users control over public budget data, simulate fiscal decisions, and observe their effects.
## Core Features
- Display budget data in a structured format
- Add or modify income and expenditure entries
- Validate user inputs and enforce constraints
- Display a summary of all changes made during the session
## Optional Extensions
- Multi-level analysis of revenues and expenses
- Categorization and aggregation of budget data
- Scenario simulation (e.g., changing VAT rates)
- Multi-year operation and comparisons
- Save and load budget states
- Mass or horizontal policy changes
- Comparison of different budget scenarios
- Graphs and visualizations
- Graphical or web-based user interface
## Technologies
- **Language:** Java SE (Standard Edition)
- **Build Tool:** Apache Maven
- **Libraries:** JFreeChart
- **Version Control:** Git / GitHub
- **IDE:** Visual Studio Code (VS Code)
## System Architecture

## Installation & Usage
bash
# Compile and run with Maven
mvn clean compile
mvn exec:java -Dexec.mainClass="com.example.Main"
#Project Structure
/scr
/pom.xml
README.md
<pre>
+------------------------------------------------------+
|                      USER INPUT                      |
|------------------------------------------------------|
| CLI / GUI Interface (AuthUI, View, ViewEditBudget)   |
| - Login / Register                                   |
| - Role selection (Minister, Governor, Citizen)        |
+-------------------------------+----------------------+
                                |
                                v
+------------------------------------------------------+
|                    CORE LOGIC                        |
|------------------------------------------------------|
| UserManager, Ministry, Governor, Edit, CreatingMinistries |
| - Validation & Authentication                        |
| - Budget editing & restrictions                      |
| - Role-based access control                          |
| - Scenario simulation (planned)                      |
+-------------------------------+----------------------+
                                |
                                v
+------------------------------------------------------+
|                     DATA LAYER                       |
|------------------------------------------------------|
| users.txt, ministries.txt, MinistriesBudgets.csv     |
| - File I/O (read, write, update)                     |
| - Persistent user & budget storage                   |
| - Import / export of financial data                  |
+-------------------------------+----------------------+
                                |
                                v
+------------------------------------------------------+
|                 VISUALIZATION & REPORTING            |
|------------------------------------------------------|
| View.java, ViewEditBudget.java                       |
| - Display budget summaries                           |
| - Generate text-based reports                        |
| - (Future) Graphs, charts, comparative analysis      |
+------------------------------------------------------+
<pre>


