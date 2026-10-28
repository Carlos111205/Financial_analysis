# Accounting System - Automated Revenue Recognition Service

An enterprise Spring Boot backend application for financial accounting and automated revenue recognition across three primary pricing models: **Fixed-Price**, **Time-and-Materials (T&M)**, and **Cost-Plus**, compliant with **ASC 606 / IFRS 15** revenue from contracts with customers.

---

## 1. Architectural Overview & Design

This system implements a **Clean Layered Architecture** with the **Strategy Pattern** to isolate revenue recognition algorithms for distinct contractual pricing structures:

```
[ HTTP REST Clients / API Consumers ]
                   │
                   ▼
       [ REST Controller Layer ]
  (ProjectController, ActivityController, InvoiceController, FinancialAnalysisController)
                   │
                   ▼
        [ Service Business Layer ]
  (ContractProjectService, ActivityLoggingService, InvoiceGenerationService, FinancialAnalysisService)
                   │
       ┌───────────┴───────────────────────────────┐
       ▼                                           ▼
[ Revenue Strategy Factory ]             [ Repository Layer (Spring Data JPA) ]
  ├── FixedPriceRevenueStrategy                     ├── ContractProjectRepository
  ├── TimeAndMaterialsRevenueStrategy               ├── MilestoneRepository
  ├── CostPlusRevenueStrategy                       ├── ActivityLogRepository
  └── RevenueCalculationResult                      └── InvoiceRepository
                                                   │
                                                   ▼
                                        [ Relational Database ]
                                        (PostgreSQL / H2 Dev)
```

---

## 2. Core Pricing Models & Revenue Recognition Logic

### A. Fixed-Price Model
* **Mechanism**: The contract has a fixed total monetary value (`totalContractValue`). Revenue is recognized based on **performance milestone completion** (ASC 606 Output Method).
* **Billing & Schedules**: Milestones are defined with deliverable amounts. Once marked `COMPLETED`, an invoice line item is generated for that deliverable, and 100% of the deliverable amount is recognized as earned revenue.
* **Over-recognition Guard**: Ensures cumulative recognized revenue cannot exceed `totalContractValue`.
* **Unbilled Revenue (WIP)**: Milestones marked `COMPLETED` that have not yet been billed.

### B. Time-and-Materials (T&M) Model
* **Mechanism**: Revenue is earned as services are rendered (ASC 606 Right-to-Invoice practical expedient).
* **Billing Calculation**:
  $$\text{Labor Amount} = \text{Hours Worked} \times \text{Hourly Billing Rate}$$
  $$\text{Material Amount} = \text{Material Cost} \times \left(1 + \frac{\text{Markup \%}}{100}\right)$$
  $$\text{Total Invoice Subtotal} = \sum \text{Labor Amount} + \sum \text{Material Amount}$$
* **Unbilled Revenue (WIP)**: Uninvoiced activity logs currently logged by staff.

### C. Cost-Plus Model
* **Mechanism**: Billing and revenue recognition are based on allowable direct incurred costs (labor, hardware, subscriptions, travel) plus an agreed contractual markup percentage (e.g., Cost + 15%).
* **Billing Calculation**:
  $$\text{Billable Line Item} = \text{Incurred Cost} \times \left(1 + \frac{\text{Markup \%}}{100}\right)$$
  $$\text{Recognized Revenue} = \text{Incurred Cost} + \text{Markup Fee}$$
* **Audit Trail**: Each line item explicitly separates base incurred cost and contractual fee.

---

## 3. Financial Analysis & Metrics Formulas

The `FinancialAnalysisService` calculates real-time profitability and balance sheet position metrics:

| Metric | Accounting Definition | Formula |
|---|---|---|
| **Recognized Revenue** | Total earned revenue recognized to date | Strategy-calculated per ASC 606 |
| **Incurred Costs** | Direct cost of goods/services sold (COGS) | $\sum \text{Incurred Labor Costs} + \sum \text{Material Costs}$ |
| **Gross Profit** | Direct project gross profit | $\text{Recognized Revenue} - \text{Incurred Costs}$ |
| **Gross Margin %** | Profitability ratio | $\left(\frac{\text{Gross Profit}}{\text{Recognized Revenue}}\right) \times 100$ |
| **Unbilled Revenue (WIP)** | Earned revenue not yet invoiced (Asset) | $\max(0, \text{Recognized Revenue} - \text{Invoiced Amount})$ |
| **Deferred Revenue** | Invoiced in advance of performance (Liability) | $\max(0, \text{Invoiced Amount} - \text{Recognized Revenue})$ |
| **Contract Consumption %** | Contract value or budget consumed | $\left(\frac{\text{Recognized Revenue}}{\text{Total Contract Value}}\right) \times 100$ |

---

## 4. Technology Stack

* **Language**: Java 21 LTS
* **Framework**: Spring Boot 3.3.4
  * `spring-boot-starter-web` (REST APIs)
  * `spring-boot-starter-data-jpa` (Hibernate ORM)
  * `spring-boot-starter-validation` (Jakarta Bean Validation)
  * `spring-boot-starter-actuator` (Health & metrics)
* **Database**:
  * Production: **PostgreSQL** (JDBC Driver `org.postgresql:postgresql`)
  * Local Dev/Testing: **H2 in-memory** (Profile `dev`)
* **API Documentation**: Springdoc OpenAPI / Swagger UI 2.6.0
* **Testing**: JUnit 5, Mockito, AssertJ, Spring WebMvcTest

---

## 5. Project Directory Structure

```
Financial_System/
├── pom.xml
├── README.md
├── src/
│   ├── main/
│   │   ├── java/com/accounting/revenuerecognition/
│   │   │   ├── AccountingSystemApplication.java
│   │   │   ├── config/
│   │   │   │   └── OpenApiConfig.java
│   │   │   ├── domain/
│   │   │   │   ├── enums/
│   │   │   │   │   ├── ActivityType.java
│   │   │   │   │   ├── InvoiceStatus.java
│   │   │   │   │   ├── MilestoneStatus.java
│   │   │   │   │   ├── PricingModel.java
│   │   │   │   │   └── ProjectStatus.java
│   │   │   │   └── model/
│   │   │   │       ├── ActivityLog.java
│   │   │   │       ├── ContractProject.java
│   │   │   │       ├── Invoice.java
│   │   │   │       ├── InvoiceLineItem.java
│   │   │   │       └── Milestone.java
│   │   │   ├── dto/
│   │   │   │   ├── request/
│   │   │   │   │   ├── CreateMilestoneRequest.java
│   │   │   │   │   ├── CreateProjectRequest.java
│   │   │   │   │   ├── GenerateInvoiceRequest.java
│   │   │   │   │   ├── LogActivityRequest.java
│   │   │   │   │   ├── UpdateMilestoneStatusRequest.java
│   │   │   │   │   └── UpdateProjectStatusRequest.java
│   │   │   │   └── response/
│   │   │   │       ├── ActivityLogResponse.java
│   │   │   │       ├── FinancialSummaryReport.java
│   │   │   │       ├── InvoiceLineItemResponse.java
│   │   │   │       ├── InvoiceResponse.java
│   │   │   │       ├── MilestoneResponse.java
│   │   │   │       ├── ProjectFinancialAnalysis.java
│   │   │   │       └── ProjectResponse.java
│   │   │   ├── exception/
│   │   │   │   ├── ErrorResponse.java
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── InvalidRevenueOperationException.java
│   │   │   │   └── ResourceNotFoundException.java
│   │   │   ├── repository/
│   │   │   │   ├── ActivityLogRepository.java
│   │   │   │   ├── ContractProjectRepository.java
│   │   │   │   ├── InvoiceRepository.java
│   │   │   │   └── MilestoneRepository.java
│   │   │   ├── service/
│   │   │   │   ├── ActivityLoggingService.java
│   │   │   │   ├── ContractProjectService.java
│   │   │   │   ├── FinancialAnalysisService.java
│   │   │   │   ├── InvoiceGenerationService.java
│   │   │   │   └── strategy/
│   │   │   │       ├── CostPlusRevenueStrategy.java
│   │   │   │       ├── FixedPriceRevenueStrategy.java
│   │   │   │       ├── RevenueCalculationResult.java
│   │   │   │       ├── RevenueRecognitionStrategy.java
│   │   │   │       ├── RevenueStrategyFactory.java
│   │   │   │       └── TimeAndMaterialsRevenueStrategy.java
│   │   │   └── web/
│   │   │       ├── ActivityController.java
│   │   │       ├── FinancialAnalysisController.java
│   │   │       ├── InvoiceController.java
│   │   │       └── ProjectController.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       └── application-prod.yml
│   └── test/
│       └── java/com/accounting/revenuerecognition/
│           ├── service/
│           │   ├── CostPlusRevenueStrategyTest.java
│           │   ├── FinancialAnalysisServiceTest.java
│           │   ├── FixedPriceRevenueStrategyTest.java
│           │   └── TimeAndMaterialsRevenueStrategyTest.java
│           └── web/
│               └── ProjectControllerIntegrationTest.java
```

---

## 6. How to Run the Application

### Option A: Run with Dev Profile (H2 In-Memory Database - Instant Out-of-the-Box)
The application defaults to the `dev` profile with an in-memory database and automatic schema generation:
```bash
mvn spring-boot:run
# or on Windows using the included wrapper:
.\mvnw.cmd spring-boot:run
```
* **Interactive Web Dashboard**: [http://localhost:8080/](http://localhost:8080/)
* **Interactive Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
* **H2 Database Console**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console) (JDBC URL: `jdbc:h2:mem:financial_db`, User: `sa`, Password: empty)

### Option B: Run with Production PostgreSQL Database
1. Create database in PostgreSQL:
   ```sql
   CREATE DATABASE financial_db;
   ```
2. Start the application with `prod` profile:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=prod
   ```
   Or set environment variables:
   ```bash
   export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/financial_db
   export SPRING_DATASOURCE_USERNAME=postgres
   export SPRING_DATASOURCE_PASSWORD=your_password
   mvn spring-boot:run -Dspring-boot.run.profiles=prod
   ```

### Option C: Run Automated Tests
```bash
mvn test
```

---

## 7. Step-by-Step API Usage & Testing Walkthrough

### Scenario 1: Fixed-Price Contract Flow

#### 1. Create Fixed-Price Project
```bash
curl -X POST http://localhost:8080/api/v1/projects \
  -H "Content-Type: application/json" \
  -d '{
    "projectCode": "PRJ-FIXED-2026",
    "name": "Core Banking Modernization",
    "clientName": "First Capital Bank",
    "pricingModel": "FIXED_PRICE",
    "totalContractValue": 100000.00,
    "startDate": "2026-11-01"
  }'
```

#### 2. Add Milestones to the Fixed-Price Project
```bash
curl -X POST http://localhost:8080/api/v1/projects/1/milestones \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Phase 1 - Solution Blueprint & Architecture",
    "description": "Sign-off on target architectural blueprint",
    "deliverableAmount": 30000.00,
    "completionPercentage": 30.00,
    "targetCompletionDate": "2026-12-15"
  }'
```

```bash
curl -X POST http://localhost:8080/api/v1/projects/1/milestones \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Phase 2 - Core Engine & Integration",
    "description": "Core deployment and integration tests",
    "deliverableAmount": 70000.00,
    "completionPercentage": 70.00,
    "targetCompletionDate": "2027-03-31"
  }'
```

#### 3. Log Incurred Direct Costs (Internal Labor)
```bash
curl -X POST http://localhost:8080/api/v1/projects/1/activities \
  -H "Content-Type: application/json" \
  -d '{
    "activityType": "LABOR_HOURS",
    "description": "Architectural Design & Review Sprints",
    "logDate": "2026-11-20",
    "hoursWorked": 120.00,
    "incurredCost": 12000.00
  }'
```

#### 4. Mark Milestone 1 as COMPLETED
```bash
curl -X PATCH http://localhost:8080/api/v1/projects/1/milestones/1/status \
  -H "Content-Type: application/json" \
  -d '{
    "status": "COMPLETED",
    "actualCompletionDate": "2026-11-25"
  }'
```

#### 5. Generate Automated Invoice
```bash
curl -X POST http://localhost:8080/api/v1/invoices/generate \
  -H "Content-Type: application/json" \
  -d '{
    "projectId": 1,
    "targetMilestoneId": 1,
    "taxRate": 10.00,
    "notes": "Billing for Phase 1 Milestone Completion"
  }'
```

#### 6. View Project Financial Analysis Report
```bash
curl -X GET http://localhost:8080/api/v1/analysis/projects/1
```
*Expected Financial Output:*
* `totalContractValue`: `$100,000.00`
* `totalIncurredCost`: `$12,000.00`
* `totalInvoicedAmount`: `$33,000.00` (Subtotal `$30,000` + Tax `$3,000`)
* `totalRecognizedRevenue`: `$30,000.00`
* `grossProfit`: `$18,000.00` (`$30,000 - $12,000`)
* `grossProfitMarginPercentage`: `60.00%`
* `unbilledRevenue`: `$0.00`
* `financialHealth`: `"HEALTHY"`

---

### Scenario 2: Time-and-Materials (T&M) Contract Flow

#### 1. Create T&M Project
```bash
curl -X POST http://localhost:8080/api/v1/projects \
  -H "Content-Type: application/json" \
  -d '{
    "projectCode": "PRJ-TM-2026",
    "name": "Cloud DevOps Pipeline Engineering",
    "clientName": "Retail Logistics Global",
    "pricingModel": "TIME_AND_MATERIALS",
    "defaultHourlyRate": 150.00,
    "costMarkupPercentage": 10.00
  }'
```

#### 2. Log Billable Labor Hours & Materials
```bash
# Log 80 hours of Senior Engineering
curl -X POST http://localhost:8080/api/v1/projects/2/activities \
  -H "Content-Type: application/json" \
  -d '{
    "activityType": "LABOR_HOURS",
    "description": "Kubernetes Cluster IaC Setup",
    "logDate": "2026-11-15",
    "hoursWorked": 80.00,
    "hourlyBillingRate": 150.00,
    "incurredCost": 6000.00
  }'

# Log Third-Party Cloud Hardware / License Cost
curl -X POST http://localhost:8080/api/v1/projects/2/activities \
  -H "Content-Type: application/json" \
  -d '{
    "activityType": "MATERIAL_EXPENSE",
    "description": "Dedicated Enterprise VPN & Security Gateway",
    "logDate": "2026-11-18",
    "materialOrDirectCost": 1000.00,
    "incurredCost": 1000.00
  }'
```

#### 3. Generate Automated Invoice
```bash
curl -X POST http://localhost:8080/api/v1/invoices/generate \
  -H "Content-Type: application/json" \
  -d '{
    "projectId": 2,
    "taxRate": 5.00
  }'
```
*Calculated Invoice:*
* Labor: $80 \times \$150 = \$12,000.00$
* Material (with 10% markup): $\$1,000 \times 1.10 = \$1,100.00$
* Subtotal & Recognized Revenue: $\$13,100.00$
* Total with 5% Tax: $\$13,755.00$

---

### Scenario 3: Cost-Plus Contract Flow

#### 1. Create Cost-Plus Project (Cost + 20%)
```bash
curl -X POST http://localhost:8080/api/v1/projects \
  -H "Content-Type: application/json" \
  -d '{
    "projectCode": "PRJ-COSTPLUS-2026",
    "name": "Smart City IoT Infrastructure",
    "clientName": "Municipal Transportation Authority",
    "pricingModel": "COST_PLUS",
    "costMarkupPercentage": 20.00
  }'
```

#### 2. Log Project Direct Expenses
```bash
curl -X POST http://localhost:8080/api/v1/projects/3/activities \
  -H "Content-Type: application/json" \
  -d '{
    "activityType": "MATERIAL_EXPENSE",
    "description": "Sensor Hardware & Radar Nodes",
    "logDate": "2026-11-12",
    "incurredCost": 25000.00,
    "materialOrDirectCost": 25000.00
  }'
```

#### 3. Generate Invoice
```bash
curl -X POST http://localhost:8080/api/v1/invoices/generate \
  -H "Content-Type: application/json" \
  -d '{
    "projectId": 3,
    "taxRate": 0.00
  }'
```
*Calculated Invoice:*
* Base Cost: $\$25,000.00$
* Markup (20%): $\$5,000.00$
* Total Invoiced & Recognized Revenue: $\$30,000.00$

---

### Scenario 4: Cross-Portfolio Executive Summary

Retrieve the consolidated financial health and balance sheet status of all projects:
```bash
curl -X GET http://localhost:8080/api/v1/analysis/summary
```
*Aggregates:*
* Total portfolio contract value
* Total incurred cost
* Total invoiced amount
* Total recognized revenue
* Overall gross profit margin %
* Total unbilled Work-In-Progress (WIP) and deferred revenue across all business lines
