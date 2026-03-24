# 🧪 LIMS Platform System

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![Postgres](https://img.shields.io/badge/postgres-%23316192.svg?style=for-the-badge&logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)
![JUnit5](https://img.shields.io/badge/JUnit5-25A162?style=for-the-badge&logo=junit5&logoColor=white)

**Status: 🚧 Work in Progress (WIP)**

## 🔬 Project Overview
A professional **Laboratory Information Management System (LIMS)** designed with a focus on **data integrity**, **traceability**, and **clean architecture**. This platform manages the full lifecycle of laboratory samples, ensuring that every modification is audited and every business rule is isolated.

### 🌟 Technical Highlights:
- **Full Audit Trail:** Automated historical tracking for critical entities (Customers, Samples, Test Results) using **Hibernate Envers**.
- **Test-Driven Environment:** Robust integration testing suite with an isolated **H2 In-Memory Database** profile for fast and reliable CI/CD.
- **Clean Architecture:** Strict separation of concerns using **Service Layers** and **DTO (Data Transfer Object)** patterns to decouple the domain from the web layer.
- **Live Documentation:** Fully interactive API exploration via **SpringDoc OpenAPI (Swagger UI)**.

---

## 🛠 Tech Stack
- **Backend:** Java 21 (LTS) & Spring Boot 3.4.x
- **Persistence:** Spring Data JPA & Hibernate
- **Database Migration:** Flyway
- **Infrastructure:** PostgreSQL & Docker Compose
- **Testing:** JUnit 5, MockMvc, AssertJ, and H2 Database
- **API Documentation:** Swagger UI (OpenAPI 3)

---

## 📈 Development Roadmap

- [x] **Phase 1: Foundation** – Spring Boot 3 environment setup with PostgreSQL and Docker containerization.
- [x] **Phase 2: Domain Mapping** – Complex relational mapping (One-to-Many / Many-to-One) between Customers, Samples, and Results.
- [x] **Phase 3: Traceability & Auditing** – Implementation of Hibernate Envers for automated `_aud` table management.
- [x] **Phase 4: Architecture Refactoring** – Decoupling logic into a dedicated **Service Layer** and securing data exposure with **DTOs**.
- [x] **Phase 5: Quality Assurance** – Configuration of the `test` profile with H2 and integration tests for REST controllers.
- [x] **Phase 6: API Documentation** – Swagger integration for real-time endpoint testing.
- [ ] **Phase 7: Security & Identity** – Implementing JWT-based Authentication and Authorization with Spring Security.
- [ ] **Phase 8: Frontend Interface** – Developing a responsive dashboard with React to consume the LIMS API.

---

## 🚀 How to Run (Development)

1. **Clone the repository:**
```
   git clone [https://github.com/kruzvinicius/lims-backend.git](https://github.com/kruzvinicius/lims-backend.git)
```
2. **Spin up the Database:**
```
  docker-compose up -d
```
3. **Run the Application:**
```
  ./mvnw spring-boot:run
```
4. **Run test**
```
  ./mvnw clean install
```

**Developed by Vinicius Rodrigues Cruz** [GitHub](https://github.com/kruzvinicius) | [LinkedIn](https://www.linkedin.com/in/vinicius-rodrigues-cruz-832985292/)
