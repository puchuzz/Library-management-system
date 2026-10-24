package com.library.ui;

import com.library.exception.LibraryException;
import com.library.model.Book;
import com.library.model.BorrowRecord;
import com.library.model.Member;
import com.library.model.MembershipType;
import com.library.model.Permission;
import com.library.model.Role;
import com.library.model.Staff;
import com.library.service.AuthService;
import com.library.service.BookService;
import com.library.service.BorrowService;
import com.library.service.MemberService;
import com.library.util.InputReader;
import java.time.LocalDate;
import java.util.List;

/** Menu-driven console front end. Menu entries are shown according to the user's role. */
public class ConsoleApp {
    private record MenuItem(String label, Permission permission, Runnable action) { }

    private final AuthService authService;
    private final BookService bookService;
    private final MemberService memberService;
    private final BorrowService borrowService;
    private final InputReader in;
    private Staff current;

    public ConsoleApp(AuthService authService, BookService bookService, MemberService memberService,
                      BorrowService borrowService, InputReader in) {
        this.authService = authService;
        this.bookService = bookService;
        this.memberService = memberService;
        this.borrowService = borrowService;
        this.in = in;
    }

    public void run() {
        System.out.println("=====================================");
        System.out.println("      LIBRARY MANAGEMENT SYSTEM");
        System.out.println("=====================================");
        boolean running = true;
        while (running) {
            running = (current == null) ? loginScreen() : showMenu();
        }
        System.out.println("Goodbye!");
    }

    // ------------------------------------------------------------------ login / menu

    private boolean loginScreen() {
        System.out.println("\n--- Login (enter 'exit' as username to quit) ---");
        String username = in.readLine("Username: ");
        if (username.equalsIgnoreCase("exit")) {
            return false;
        }
        String password = in.readLine("Password: ");
        try {
            current = authService.login(username, password);
            System.out.println("Welcome, " + current.getFullName() + " [" + current.getRole() + "]");
        } catch (LibraryException e) {
            System.out.println("! " + e.getMessage());
        }
        return true;
    }

    private boolean showMenu() {
        List<MenuItem> items = buildMenu();
        System.out.println("\n--- Main Menu (" + current.getUsername() + " / " + current.getRole() + ") ---");
        for (int i = 0; i < items.size(); i++) {
            System.out.printf("%2d. %s%n", i + 1, items.get(i).label());
        }
        int choice = in.readInt("Choose an option: ");
        if (choice == -1) {
            return false; // end of input
        }
        if (choice < 1 || choice > items.size()) {
            System.out.println("Invalid choice.");
            return true;
        }
        try {
            items.get(choice - 1).action().run();
        } catch (LibraryException e) {
            System.out.println("! " + e.getMessage());
        } catch (RuntimeException e) {
            System.out.println("! Unexpected error: " + e.getMessage());
        }
        return true;
    }

    private List<MenuItem> buildMenu() {
        List<MenuItem> all = List.of(
                new MenuItem("Search books", Permission.VIEW_CATALOG, this::searchBooks),
                new MenuItem("List all books", Permission.VIEW_CATALOG, this::listBooks),
                new MenuItem("Add a book", Permission.MANAGE_BOOKS, this::addBook),
                new MenuItem("Update book details", Permission.MANAGE_BOOKS, this::updateBook),
                new MenuItem("Add copies to a book", Permission.MANAGE_BOOKS, this::addCopies),
                new MenuItem("Delete a book", Permission.DELETE_BOOKS, this::deleteBook),
                new MenuItem("Register a member", Permission.MANAGE_MEMBERS, this::registerMember),
                new MenuItem("List members", Permission.MANAGE_MEMBERS, this::listMembers),
                new MenuItem("Activate / deactivate a member", Permission.MANAGE_MEMBERS, this::toggleMember),
                new MenuItem("Issue a book", Permission.ISSUE_RETURN, this::issueBook),
                new MenuItem("Return a book", Permission.ISSUE_RETURN, this::returnBook),
                new MenuItem("View active loans", Permission.VIEW_REPORTS, this::activeLoans),
                new MenuItem("View overdue loans", Permission.VIEW_REPORTS, this::overdueLoans),
                new MenuItem("View a member's borrow history", Permission.VIEW_REPORTS, this::memberHistory),
                new MenuItem("Add a staff account", Permission.MANAGE_STAFF, this::addStaff),
                new MenuItem("List staff accounts", Permission.MANAGE_STAFF, this::listStaff),
                new MenuItem("Activate / deactivate a staff account", Permission.MANAGE_STAFF, this::toggleStaff),
                new MenuItem("Change my password", null, this::changePassword),
                new MenuItem("Logout", null, () -> current = null));
        return all.stream()
                .filter(item -> item.permission() == null || current.can(item.permission()))
                .toList();
    }

    // ------------------------------------------------------------------ books

    private void searchBooks() {
        String keyword = in.readLine("Search (title / author / category / ISBN): ");
        printBooks(bookService.search(current, keyword));
    }

    private void listBooks() {
        printBooks(bookService.listAll(current));
    }

    private void addBook() {
        String isbn = in.readLine("ISBN: ");
        String title = in.readLine("Title: ");
        String author = in.readLine("Author: ");
        String category = in.readLine("Category: ");
        int copies = in.readInt("Number of copies: ");
        Book book = bookService.addBook(current, isbn, title, author, category, copies);
        System.out.println("Book added with ID " + book.getId());
    }

    private void updateBook() {
        int id = in.readInt("Book ID: ");
        Book book = bookService.getBook(id);
        System.out.println("Leave a field blank to keep the current value.");
        String title = in.readLine("Title [" + book.getTitle() + "]: ");
        String author = in.readLine("Author [" + book.getAuthor() + "]: ");
        String category = in.readLine("Category [" + book.getCategory() + "]: ");
        System.out.println("Updated: " + bookService.updateDetails(current, id, title, author, category).describe());
    }

    private void addCopies() {
        int id = in.readInt("Book ID: ");
        int copies = in.readInt("Copies to add: ");
        System.out.println("Updated: " + bookService.addCopies(current, id, copies).describe());
    }

    private void deleteBook() {
        int id = in.readInt("Book ID: ");
        if (in.confirm("Delete this book permanently?")) {
            bookService.deleteBook(current, id);
            System.out.println("Book deleted.");
        }
    }

    // ------------------------------------------------------------------ members

    private void registerMember() {
        String name = in.readLine("Full name: ");
        String email = in.readLine("Email: ");
        String phone = in.readLine("Phone (optional): ");
        int type = in.readInt("Membership type (1 = Student, 2 = Faculty): ");
        MembershipType membershipType = type == 2 ? MembershipType.FACULTY : MembershipType.STUDENT;
        Member member = memberService.register(current, name, email, phone, membershipType);
        System.out.printf("Registered %s with ID %d (limit %d books, %d days)%n",
                member.getFullName(), member.getId(), member.getBorrowLimit(), member.getLoanPeriodDays());
    }

    private void listMembers() {
        List<Member> members = memberService.listAll(current);
        if (members.isEmpty()) {
            System.out.println("No members found.");
            return;
        }
        System.out.printf("%-5s %-25s %-30s %-8s %-6s%n", "ID", "Name", "Email", "Type", "Active");
        for (Member m : members) {
            System.out.printf("%-5d %-25s %-30s %-8s %-6s%n", m.getId(), cut(m.getFullName(), 25),
                    cut(m.getEmail(), 30), m.getTypeLabel(), m.isActive() ? "yes" : "no");
        }
    }

    private void toggleMember() {
        int id = in.readInt("Member ID: ");
        boolean activate = in.confirm("Activate this member? (n = deactivate)");
        memberService.setActive(current, id, activate);
        System.out.println("Member " + (activate ? "activated." : "deactivated."));
    }

    // ------------------------------------------------------------------ circulation

    private void issueBook() {
        int bookId = in.readInt("Book ID: ");
        int memberId = in.readInt("Member ID: ");
        BorrowRecord record = borrowService.issueBook(current, bookId, memberId);
        System.out.printf("Issued '%s' to %s. Loan ID %d, due on %s%n",
                record.getBookTitle(), record.getMemberName(), record.getId(), record.getDueDate());
    }

    private void returnBook() {
        int recordId = in.readInt("Loan ID: ");
        BorrowRecord record = borrowService.returnBook(current, recordId);
        System.out.printf("Returned '%s' from %s.%n", record.getBookTitle(), record.getMemberName());
        if (record.getFineAmount().signum() > 0) {
            System.out.println("Late return fine to collect: Rs. " + record.getFineAmount());
        } else {
            System.out.println("No fine due.");
        }
    }

    private void activeLoans() {
        printLoans(borrowService.activeLoans(current));
    }

    private void overdueLoans() {
        printLoans(borrowService.overdueLoans(current));
    }

    private void memberHistory() {
        int memberId = in.readInt("Member ID: ");
        printLoans(borrowService.memberHistory(current, memberId));
    }

    // ------------------------------------------------------------------ staff / account

    private void addStaff() {
        int roleChoice = in.readInt("Role (1 = Librarian, 2 = Admin): ");
        Role role = roleChoice == 2 ? Role.ADMIN : Role.LIBRARIAN;
        String username = in.readLine("Username: ");
        String password = in.readLine("Password (min 8 chars): ");
        String name = in.readLine("Full name: ");
        String email = in.readLine("Email: ");
        String phone = in.readLine("Phone (optional): ");
        Staff staff = authService.createStaff(current, role, username, password, name, email, phone);
        System.out.println("Created " + staff.getRole() + " account '" + staff.getUsername() + "' (ID " + staff.getId() + ")");
    }

    private void listStaff() {
        System.out.printf("%-5s %-15s %-25s %-10s %-6s%n", "ID", "Username", "Name", "Role", "Active");
        for (Staff s : authService.listStaff(current)) {
            System.out.printf("%-5d %-15s %-25s %-10s %-6s%n", s.getId(), s.getUsername(),
                    cut(s.getFullName(), 25), s.getRole(), s.isActive() ? "yes" : "no");
        }
    }

    private void toggleStaff() {
        int id = in.readInt("Staff ID: ");
        boolean activate = in.confirm("Activate this account? (n = deactivate)");
        authService.setStaffActive(current, id, activate);
        System.out.println("Account " + (activate ? "activated." : "deactivated."));
    }

    private void changePassword() {
        String oldPassword = in.readLine("Current password: ");
        String newPassword = in.readLine("New password (min 8 chars): ");
        authService.changePassword(current, oldPassword, newPassword);
        System.out.println("Password changed.");
    }

    // ------------------------------------------------------------------ printing helpers

    private void printBooks(List<Book> books) {
        if (books.isEmpty()) {
            System.out.println("No books found.");
            return;
        }
        System.out.printf("%-5s %-15s %-32s %-22s %-18s %s%n", "ID", "ISBN", "Title", "Author", "Category", "Avail");
        for (Book b : books) {
            System.out.printf("%-5d %-15s %-32s %-22s %-18s %d/%d%n", b.getId(), b.getIsbn(),
                    cut(b.getTitle(), 32), cut(b.getAuthor(), 22), cut(b.getCategory(), 18),
                    b.getAvailableCopies(), b.getTotalCopies());
        }
    }

    private void printLoans(List<BorrowRecord> records) {
        if (records.isEmpty()) {
            System.out.println("No records found.");
            return;
        }
        LocalDate today = LocalDate.now();
        System.out.printf("%-6s %-28s %-20s %-11s %-11s %-11s %s%n",
                "Loan", "Book", "Member", "Borrowed", "Due", "Returned", "Fine / Status");
        for (BorrowRecord r : records) {
            String status = r.isReturned() ? "Rs. " + r.getFineAmount() : (r.isOverdue(today) ? "OVERDUE" : "On loan");
            System.out.printf("%-6d %-28s %-20s %-11s %-11s %-11s %s%n", r.getId(), cut(r.getBookTitle(), 28),
                    cut(r.getMemberName(), 20), r.getBorrowDate(), r.getDueDate(),
                    r.isReturned() ? r.getReturnDate() : "-", status);
        }
    }

    private static String cut(String text, int max) {
        if (text == null) {
            return "";
        }
        return text.length() <= max ? text : text.substring(0, max - 1) + "~";
    }
}
