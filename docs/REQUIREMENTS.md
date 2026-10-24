# Requirements

## Purpose
Replace manual registers in a small college library with a system that tracks books, members and loans.

## Users
| User | Description |
|---|---|
| Admin | Full control, including staff accounts and deleting books |
| Librarian | Day-to-day work: catalog, members, issuing and returning books |
| Member (student / faculty) | Borrows books; does not log in to this console system |

## Functional requirements
| ID | Requirement |
|---|---|
| FR-1 | Staff must log in with a username and password |
| FR-2 | Access to each feature depends on the staff member's role |
| FR-3 | Add, update, search, list, restock and delete books (ISBN unique) |
| FR-4 | Register members as student or faculty (email unique); activate / deactivate them |
| FR-5 | Issue a book to an active member if a copy is available |
| FR-6 | Enforce borrow limits (student 3, faculty 10) and block members with overdue books |
| FR-7 | Calculate due date from membership type (student 14 days, faculty 30 days) |
| FR-8 | Return a book, restore stock and calculate the late fine |
| FR-9 | Show active loans, overdue loans and a member's borrow history |
| FR-10 | Admin can create, list and deactivate staff accounts |
| FR-11 | Any user can change their own password |

## Non-functional requirements
| ID | Requirement |
|---|---|
| NFR-1 | Passwords stored only as salted hashes |
| NFR-2 | Stock and loan updates are atomic (no lost or double-issued copies) |
| NFR-3 | Business rules are testable without a database |
| NFR-4 | Database credentials are configurable and never committed |
| NFR-5 | Runs on any OS with JDK 17+ and MySQL 8+ |

## Out of scope (v1)
Graphical UI, online member portal, payments, email/SMS notifications, reservations.
