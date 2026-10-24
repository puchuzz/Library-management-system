# Design

## Architecture
Layered architecture; each layer only talks to the one below it.

```mermaid
flowchart TD
    UI[ui.ConsoleApp] --> SVC[service: Auth / Book / Member / Borrow]
    SVC --> SEC[security: AccessControl, PasswordUtil]
    SVC --> DAOI[dao interfaces]
    DAOI --> DAOJ[dao.impl JDBC]
    DAOJ --> DB[(MySQL library_db)]
```

Design choices:
- **DAO pattern** - SQL is isolated in `dao.impl`; services depend on interfaces, so tests inject in-memory fakes.
- **Strategy pattern** - `FinePolicy` lets the fine rule change without touching `BorrowService`.
- **Factory methods** - `Member.create(...)` and `Staff.create(...)` build the correct subclass from a database value.
- **Exceptions** - a single `LibraryException` hierarchy; JDBC `SQLException`s are wrapped in `DataAccessException`.
- **Atomic circulation** - `UPDATE books SET available_copies = available_copies - 1 ... WHERE available_copies > 0` plus the loan insert run in one transaction.

## Entity-relationship diagram
```mermaid
erDiagram
    STAFF ||--o{ BORROW_RECORDS : issues
    MEMBERS ||--o{ BORROW_RECORDS : borrows
    BOOKS ||--o{ BORROW_RECORDS : "is loaned in"
    STAFF {
        int staff_id PK
        string username UK
        string password_hash
        string full_name
        string email UK
        enum role
        bool active
    }
    MEMBERS {
        int member_id PK
        string full_name
        string email UK
        enum member_type
        date joined_on
        bool active
    }
    BOOKS {
        int book_id PK
        string isbn UK
        string title
        string author
        string category
        int total_copies
        int available_copies
    }
    BORROW_RECORDS {
        int record_id PK
        int book_id FK
        int member_id FK
        int issued_by FK
        date borrow_date
        date due_date
        date return_date
        decimal fine_amount
    }
```

## Class diagram (entities)
```mermaid
classDiagram
    class Person { <<abstract>> id fullName email phone getTypeLabel() }
    class Member { <<abstract>> getBorrowLimit() getLoanPeriodDays() getFinePerDay() }
    class StudentMember
    class FacultyMember
    class Staff { <<abstract>> username passwordHash getRole() }
    class Admin
    class Librarian
    class LibraryItem { <<abstract>> id title category describe() }
    class Book { isbn author totalCopies availableCopies }
    class Borrowable { <<interface>> isAvailable() checkOut() checkIn() }
    class Authorizable { <<interface>> getRole() can(Permission) }
    class BorrowRecord

    Person <|-- Member
    Person <|-- Staff
    Member <|-- StudentMember
    Member <|-- FacultyMember
    Staff <|-- Admin
    Staff <|-- Librarian
    LibraryItem <|-- Book
    Borrowable <|.. Book
    Authorizable <|.. Staff
    BorrowRecord --> Book
    BorrowRecord --> Member
```

## Issue-book flow
1. Check the staff member has `ISSUE_RETURN`.
2. Load the member (must be active) and the book (must have a copy).
3. Reject duplicates, overdue loans and borrow-limit breaches.
4. Compute the due date from the membership type.
5. `BorrowDao.issue` decrements stock and inserts the loan in one transaction.
