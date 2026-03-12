# 🧪 LIMS Platform System

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![Postgres](https://img.shields.io/badge/postgres-%23316192.svg?style=for-the-badge&logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)

**Status: 🚧 Work in Progress (WIP)**

## About the Project
A comprehensive Laboratory Information Management System (LIMS) focused on data integrity and traceability. This full-stack platform manages the entire lifecycle of lab samples, from reception to final reporting.

### Key Features:
- **Customer Management:** Full relational mapping between clients and their requests.
- **Sample Tracking:** Unique barcode system and material classification.
- **Infrastructure:** Containerized database using Docker for easy deployment.

---

## 🛠 Tech Stack
- **Language:** Java 21+
- **Framework:** Spring Boot 3
- **Data:** Spring Data JPA / Hibernate
- **Database:** PostgreSQL (Docker)
- **Tooling:** Lombok, Maven

## 📈 Development Roadmap
- [x] **Phase 1: Foundation** - Spring Boot 3 environment setup with PostgreSQL and Docker containerization.
- [x] **Phase 2: Domain Mapping** - Robust relational mapping between Customers and Samples using JPA/Hibernate.
- [x] **Phase 3: Data Integrity & Audit** - Implementation of Hibernate Envers for full traceability and historical record tracking.
- [x] **Phase 4: API Optimization** - Decoupling entities from the web layer using the DTO (Data Transfer Object) pattern for enhanced security.
- [ ] **Phase 5: Business Logic Layer** - Refactoring to Service Layer to ensure clean code and separation of concerns.
- [ ] **Phase 6: Security & Identity** - Implementing Authentication and Authorization with Spring Security and JWT.
- [ ] **Phase 7: Frontend Interface** - Building a responsive dashboard with React to consume the LIMS API.
---
**Developed by Vinicius Rodrigues Cruz** [GitHub](https://github.com/kruzvinicius) | [LinkedIn](https://www.linkedin.com/in/vinicius-rodrigues-cruz-832985292/)
