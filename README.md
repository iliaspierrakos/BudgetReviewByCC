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

## Repository Structure

```text
BudgetReviewByCC/
├── README.md                     # Project documentation
├── pom.xml                       # Maven configuration
│
├── data/                         # Runtime and persistence data
│   ├── users.txt                 # Registered users
│   └── recommendation/           # Citizen and ministry recommendation data
│
├── docs/                         # Generated JavaDoc documentation
│   ├── UserFeatures/             # JavaDoc for user feature classes
│   ├── UserManagement/           # JavaDoc for user management classes
│   └── index.html                # JavaDoc entry point
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── Main/             # Application entry point
│   │   │   │   └── BudgetReviewFxMain.java
│   │   │   ├── UserManagement/   # Authentication, roles, users
│   │   │   ├── UserFeatures/     # Budget logic, proposals, statistics
│   │   │   ├── guiFolder/        # JavaFX UI screens
│   │   │   └── NecessaryFilesAndData/
│   │   │       └── UserBudgets/  # User-specific budget data
│   │   │
│   │   └── resources/
│   │       ├── NecessaryFilesAndData/   # Budget datasets (2020–2026)
│   │       ├── css/                     # UI styles
│   │       ├── icons/                   # UI icons
│   │       └── guiFolder/               # Images and UI assets
│   │
│   └── test/
│       └── java/
│           └── FeaturesTest/            # JUnit test cases
│
└── target/                       # Build output and reports
```

## UML Design

The UML class diagram illustrates the high-level design of the application,
focusing on:
- User role hierarchy
- Core budget management logic
- Key relationships between system components

The diagram is available at:

---

## Data Structures & Algorithms Overview

### Data Structures

The application makes extensive use of core Java data structures to manage users, budgets, and proposals efficiently:

- **Lists (`ArrayList`)**
  - Used to store collections of budget entries, ministries, proposals, and historical edits.
  - Widely utilized in classes under `UserFeatures` for budget visualization, comparisons, and statistics.

- **Maps (`HashMap`)**
  - Used to associate:
    - Ministries with their corresponding budget values
    - Users with roles and permissions
    - Tax distributions per ministry
  - Enables fast lookup operations during budget edits and calculations.

- **Custom Domain Objects**
  - Strongly-typed classes such as `User`, `Governor`, `MinistryMember`, `Ministry`, and `Citizen`
  - Encapsulate application logic and ensure role-based behavior through object-oriented design.

- **File-Based Data Storage**
  - CSV and TXT files are used for persistent storage:
    - Budget datasets (2020–2026)
    - User budgets
    - Proposals from citizens and ministers
    - Voting and recommendation data
  - File handling utilities such as `UserBudgetFileUtil` and `UserBudgetPersistence` manage data consistency.

---

### Algorithms

The system implements multiple algorithms to support decision-making, analysis, and visualization:

- **Budget Comparison Algorithms**
  - Compare budget data across different years (2020–2026)
  - Identify increases, decreases, and overall trends
  - Implemented in comparison-related classes under `UserFeatures`.

- **Sorting Algorithms**
  - Budgets and ministries are sorted based on user-selected criteria
  - Used for clearer visualization and analysis of financial data.

- **Tax Calculation Algorithm**
  - Computes tax obligations based on:
    - Age
    - Income
    - Number of children
  - Distributes calculated tax amounts proportionally across ministries.

- **Role-Based Access Control Logic**
  - Determines available features dynamically based on user role
  - Implemented through conditional checks and polymorphism in `UserManagement` classes.

- **Proposal Aggregation and Analysis**
  - Collects and analyzes recommendations from citizens and ministers
  - Generates statistical summaries and visual representations (pie and line charts).

---

### Architectural Considerations
- Separation of concerns between:
  - Business logic (`UserFeatures`)
  - User management (`UserManagement`)
  - Presentation layer (`guiFolder`)
- Data validation and processing occur before persistence and visualization.
- Algorithms are designed for clarity and maintainability rather than raw performance, given the application's domain.


---

## Testing & Code Quality

* Automated tests implemented using **JUnit 5**
* Test execution:

```bash
mvn clean test
```

* Code coverage generated using **JaCoCo**

---

## Additional Documentation

* JavaDoc documentation (generated via Maven)
* Test reports and coverage results available in:

```
/target/site/
```
