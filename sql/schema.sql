-- Library Management System - database schema (MySQL 8+)
-- Usage:  mysql -u root -p < sql/schema.sql

CREATE DATABASE IF NOT EXISTS library_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE library_db;

-- Library staff (role-based access control: ADMIN / LIBRARIAN)
CREATE TABLE IF NOT EXISTS staff (
    staff_id       INT AUTO_INCREMENT PRIMARY KEY,
    username       VARCHAR(50)  NOT NULL UNIQUE,
    password_hash  VARCHAR(255) NOT NULL,
    full_name      VARCHAR(100) NOT NULL,
    email          VARCHAR(100) NOT NULL UNIQUE,
    phone          VARCHAR(20),
    role           ENUM('ADMIN', 'LIBRARIAN') NOT NULL,
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Library members (students and faculty have different borrowing rules)
CREATE TABLE IF NOT EXISTS members (
    member_id    INT AUTO_INCREMENT PRIMARY KEY,
    full_name    VARCHAR(100) NOT NULL,
    email        VARCHAR(100) NOT NULL UNIQUE,
    phone        VARCHAR(20),
    member_type  ENUM('STUDENT', 'FACULTY') NOT NULL,
    joined_on    DATE         NOT NULL,
    active       BOOLEAN      NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

-- Book inventory
CREATE TABLE IF NOT EXISTS books (
    book_id           INT AUTO_INCREMENT PRIMARY KEY,
    isbn              VARCHAR(20)  NOT NULL UNIQUE,
    title             VARCHAR(200) NOT NULL,
    author            VARCHAR(150) NOT NULL,
    category          VARCHAR(80)  NOT NULL,
    total_copies      INT          NOT NULL,
    available_copies  INT          NOT NULL,
    CONSTRAINT chk_copies_nonneg CHECK (total_copies >= 0 AND available_copies >= 0),
    CONSTRAINT chk_copies_bound  CHECK (available_copies <= total_copies),
    INDEX idx_books_title (title),
    INDEX idx_books_author (author)
) ENGINE=InnoDB;

-- Borrow / return tracking
CREATE TABLE IF NOT EXISTS borrow_records (
    record_id    INT AUTO_INCREMENT PRIMARY KEY,
    book_id      INT           NOT NULL,
    member_id    INT           NOT NULL,
    issued_by    INT           NOT NULL,
    borrow_date  DATE          NOT NULL,
    due_date     DATE          NOT NULL,
    return_date  DATE          NULL,
    fine_amount  DECIMAL(8, 2) NOT NULL DEFAULT 0.00,
    CONSTRAINT fk_br_book   FOREIGN KEY (book_id)   REFERENCES books(book_id),
    CONSTRAINT fk_br_member FOREIGN KEY (member_id) REFERENCES members(member_id),
    CONSTRAINT fk_br_staff  FOREIGN KEY (issued_by) REFERENCES staff(staff_id),
    INDEX idx_br_member_active (member_id, return_date),
    INDEX idx_br_due (due_date, return_date)
) ENGINE=InnoDB;
