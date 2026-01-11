# BudgetReviewByCC

## Overview

**BudgetReviewByCC** is a JavaFX-based desktop application for managing and reviewing the national budget.
The system supports **role-based access control**, providing different functionalities depending on the user's role:

* **Prime Minister**
* **Minister**
* **Citizen**

The application is built using **Java**, **Maven**, and **JavaFX**, and includes automated tests for core functionality.

---

## Prerequisites

* Java **25** (main code), Java **21** (for compiling and testing)
* Apache **Maven**
* JavaFX (handled via Maven dependencies)

---

## Compilation Instructions

To compile the project, run:

```bash
mvn clean compile
```

---

## Execution Instructions

The application is executed via Maven using the JavaFX plugin:

```bash
mvn javafx:run
```

Main entry point:

```
Main.BudgetReviewFxMain
```

---

## Application Usage

### User Registration & Authentication

* Users register using:

  * Username
  * Password
  * Role (Prime Minister, Minister, Citizen)
* Access is granted after approval.
* Upon successful login, users are presented with a **role-specific menu**.

---

### Common Features (All Roles)

* **View Budget**

  * Sorting options available
* **Compare Budgets**

  * Comparison between two years (2020–2026)

---

### Prime Minister Features

* **Edit Budget**

  * Modify the budget of the current year only
* **View Minister Proposals**

  * Review proposed changes from ministers
* **View Statistics**

  * Analyze citizen recommendations
* **Charts**

  * Pie charts
  * Line charts

---

### Minister Features

* **Propose Budget Edits**

  * Submit recommendations to the Prime Minister
* **View Citizen Proposals**
* **Charts**

  * Pie charts
  * Line charts

---

### Citizen Features

* **Virtual Budget Edit**

  * Simulate budget modifications
* **Tax Receipt Calculation**

  * Tax calculated based on:

    * Name
    * Age
    * Number of children
    * Income
  * Tax distribution per ministry
* **Submit Recommendations**

  * Proposals submitted to relevant ministries

---

## Repository Structure

```text
BudgetReviewByCC/
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   └── test/
│       └── java/
├── pom.xml
├── README.md
└── target/
```

---

## UML Design

The UML diagram describing the system architecture and class relationships is available in the repository:

```
/docs/uml-diagram.png
```

(or as a separate UML file)

---

## Data Structures & Algorithms

* **Data Structures**

  * Lists and Maps for budget entries
  * User-role mappings
  * Proposal collections per role
* **Algorithms**

  * Budget comparison algorithms (year-to-year)
  * Sorting algorithms for budget visualization
  * Tax calculation logic based on user data
  * Role-based access control checks

---

## Testing & Code Quality

* Automated tests implemented using **JUnit 5**
* Test execution:

```bash
mvn test
```

* Code coverage generated using **JaCoCo**

---

## Additional Documentation

* JavaDoc documentation (generated via Maven)
* Test reports and coverage results available in:

```
/target/site/
```
