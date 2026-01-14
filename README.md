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

* Java **21+** (the project is compiled targeting Java 21; newer JDKs such as Java 25 are supported)
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
## Architecture Overview

The application follows a layered architecture that separates responsibilities
between data management, business logic, and presentation.

- **Presentation Layer**: JavaFX UI screens responsible for user interaction
- **Business Logic Layer**: Core application logic related to budgets, proposals, and calculations
- **Data Layer**: File-based persistence for budgets, users, and recommendations

This design improves maintainability, extensibility, and clarity of the system.

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
│   │   │
│   │   └── resources/
│   │       ├── NecessaryFilesAndData/   # Budget datasets (2020–2026), csv files
│   │       ├── css/                     # UI styles
│   │       ├── icons/                   # UI icons
│   │       └── guiFolder/               # Images and UI assets
│   │
│   └── test/
│       └── java/
│           ├── FeaturesTest/            # JUnit tests for user features
│           └── UserManagementTest/      # JUnit tests for user management
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
The application includes an extensive suite of automated tests implemented
using **JUnit 5**, focusing on validating the correctness of the core business
logic.

The tests primarily cover:
- Budget management and modification logic
- Proposal submission and aggregation
- Tax calculation functionality
- Ministry and budget data handling
- User-related operations where applicable

Test execution can be performed using:

```bash
mvn clean test
```
---
### Code Coverage

Code coverage analysis is performed using **JaCoCo**, which is integrated into
the Maven build lifecycle of the project.

JaCoCo is used to measure how much of the source code is exercised by the
automated test suite during execution.

- Coverage analysis is executed automatically during the `test` phase
- The current test suite achieves approximately **70% overall code coverage**
- The coverage primarily focuses on the core business logic of the application

The generated HTML coverage report is available at:

```text
/target/site/jacoco/index.html
```

---
## JavaDoc Comments and Code Documentation

The source code of the application is extensively documented using **JavaDoc comments**, following standard Java documentation conventions.

JavaDoc comments are used to describe:

* The purpose and responsibility of each class
* The role of core components within the system architecture
* The behavior of public methods and their parameters
* The interaction between user roles, budget management, and proposal mechanisms

Documentation is primarily provided for:

* Core domain classes (e.g., users, ministries, budget handlers)
* Business logic classes under the `UserFeatures` package
* User management and authentication classes under the `UserManagement` package

Each documented class includes:

* A high-level description of its responsibility within the system
* Explanations of key methods and workflows
* Clear separation between business logic and presentation logic

The use of JavaDoc improves:

* Code readability and maintainability
* Understanding of the object-oriented design
* Ease of collaboration and future extension of the system

---

### JavaDoc Access

The generated JavaDoc output is included in the repository and can be accessed locally via:

```bash
Windows
start docs/index.html
macOS
open docs/index.html

```
Opening this file in a web browser provides a complete, navigable view of the application's API documentation, including class hierarchies and package overviews.

---

