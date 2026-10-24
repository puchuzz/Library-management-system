# Library Management System

A console-based library management system built with **Core Java, JDBC and MySQL**.
It supports book inventory management, member registration, borrow/return tracking with
automatic fines, and **role-based access control** (Admin / Librarian).

![Java](https://img.shields.io/badge/Java-17-orange) ![MySQL](https://img.shields.io/badge/MySQL-8-blue) ![Build](https://img.shields.io/badge/build-Maven-green) ![License](https://img.shields.io/badge/license-MIT-lightgrey)

## Features

- **Book inventory** - add, update, search (title / author / category / ISBN), restock and delete books
- **Member management** - register students and faculty, activate / deactivate accounts
- **Circulation** - issue and return books; due dates, borrow limits and late fines are driven by membership type
- **Reports** - active loans, overdue loans, per-member borrow history
- **Role-based access control** - Admin and Librarian roles with fine-grained permissions
- **Secure logins** - salted PBKDF2 password hashing, no plain-text passwords
- **Transactional safety** - issuing/returning a book updates stock and loan record in one JDBC transaction

## OOP concepts used

| Pillar | Where |
|---|---|
| **Inheritance** | `Person` -> `Member` -> `StudentMember` / `FacultyMember`; `Person` -> `Staff` -> `Admin` / `Librarian`; `LibraryItem` -> `Book`; `LibraryException` -> specific exceptions |
| **Encapsulation** | Private fields with controlled getters/setters in every entity; invariants enforced in constructors (`Book` copy counts) and methods (`checkOut`, `addCopies`) |
| **Polymorphism** | `Member.getBorrowLimit()/getLoanPeriodDays()/getFinePerDay()` and `Staff.getRole()` behave differently per subclass; DAOs and `FinePolicy` are swapped via interfaces |
| **Abstraction** | Abstract classes `Person`, `Member`, `Staff`, `LibraryItem`; interfaces `Repository<T, ID>`, `Borrowable`, `Authorizable`, `FinePolicy`, and the DAO contracts |

## Roles and permissions

| Permission | Librarian | Admin |
|---|:--:|:--:|
| View / search catalog | yes | yes |
| Add / update books, add copies | yes | yes |
| Delete books | - | yes |
| Register / manage members | yes | yes |
| Issue / return books | yes | yes |
| View loan reports | yes | yes |
| Manage staff accounts | - | yes |

## Membership rules

| | Student | Faculty |
|---|:--:|:--:|
| Max books at once | 3 | 10 |
| Loan period | 14 days | 30 days |
| Fine per late day | Rs. 2 | Rs. 1 |

Fines are capped at Rs. 500 per loan. Members with an overdue book cannot borrow more until they return it.

## Project structure

```
library-management-system/
├── pom.xml
├── sql/
│   ├── schema.sql              # tables, constraints, indexes
│   └── seed.sql                # optional sample books and members
├── docs/                       # requirements, design (ER + class diagrams), deployment
├── src/main/java/com/library/
│   ├── Main.java               # wiring + entry point
│   ├── config/DBConnection.java
│   ├── model/                  # entities and enums
│   ├── dao/  + dao/impl/       # DAO interfaces and JDBC implementations
│   ├── service/                # business rules
│   ├── security/               # RBAC checks, password hashing
│   ├── exception/              # custom exception hierarchy
│   ├── util/                   # validation, console input
│   └── ui/ConsoleApp.java      # role-aware menu
├── src/test/java/com/library/  # JUnit 5 tests (in-memory fake DAOs, no DB needed)
└── .github/workflows/ci.yml    # build + test on every push
```

Layering: `ui -> service -> dao (interface) -> dao.impl (JDBC) -> MySQL`. Business rules live only in the service layer.

## Getting started

### Prerequisites
- JDK 17+
- Maven 3.8+
- MySQL 8+

### 1. Create the database
```bash
mysql -u root -p < sql/schema.sql
mysql -u root -p library_db < sql/seed.sql     # optional sample data
```

### 2. Configure the connection
Either copy the example file:
```bash
cp src/main/resources/db.properties.example src/main/resources/db.properties
# then edit db.url / db.user / db.password
```
or use environment variables (they take priority):
```bash
export DB_URL="jdbc:mysql://localhost:3306/library_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
export DB_USER=root
export DB_PASSWORD=your_password
```
`db.properties` is git-ignored so your password never gets committed.

### 3. Run
```bash
mvn compile exec:java
```
or build a runnable jar:
```bash
mvn clean package
java -jar target/library-management-system-1.0.0.jar
```

On first run an admin account is created automatically:

| Username | Password |
|---|---|
| `admin` | `Admin@123` |

Log in, then use **Change my password** and create librarian accounts from the menu.

## Testing

```bash
mvn test
```

Unit tests cover the entities (polymorphic rules, book stock), password hashing, fine calculation,
role permissions and the full borrow/return workflow (limits, overdue blocking, fines, stock restore).
The service tests use in-memory implementations of the DAO interfaces, so no database is needed.

## Software development life cycle

| Phase | Artifact |
|---|---|
| Requirements | [docs/REQUIREMENTS.md](docs/REQUIREMENTS.md) |
| Design | [docs/DESIGN.md](docs/DESIGN.md) - architecture, ER diagram, class diagram |
| Implementation | `src/main/java` |
| Testing | `src/test/java`, run by `mvn test` and GitHub Actions |
| Deployment | [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md) |

## Possible future improvements
- Swing / JavaFX or Spring Boot REST front end on top of the existing service layer
- Book reservations and fine payment tracking
- Connection pooling (HikariCP)
- Email reminders for due and overdue books

## License
MIT - see [LICENSE](LICENSE).
