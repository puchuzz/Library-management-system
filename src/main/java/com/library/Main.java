package com.library;

import com.library.dao.BookDao;
import com.library.dao.BorrowDao;
import com.library.dao.MemberDao;
import com.library.dao.StaffDao;
import com.library.dao.impl.BookDaoImpl;
import com.library.dao.impl.BorrowDaoImpl;
import com.library.dao.impl.MemberDaoImpl;
import com.library.dao.impl.StaffDaoImpl;
import com.library.exception.LibraryException;
import com.library.service.AuthService;
import com.library.service.BookService;
import com.library.service.BorrowService;
import com.library.service.DailyFinePolicy;
import com.library.service.MemberService;
import com.library.ui.ConsoleApp;
import com.library.util.InputReader;
import java.time.Clock;
import java.util.Scanner;

/** Application entry point: wires DAOs -> services -> console UI. */
public class Main {
    public static void main(String[] args) {
        BookDao bookDao = new BookDaoImpl();
        MemberDao memberDao = new MemberDaoImpl();
        StaffDao staffDao = new StaffDaoImpl();
        BorrowDao borrowDao = new BorrowDaoImpl();
        Clock clock = Clock.systemDefaultZone();

        AuthService authService = new AuthService(staffDao);
        BookService bookService = new BookService(bookDao);
        MemberService memberService = new MemberService(memberDao, clock);
        BorrowService borrowService = new BorrowService(bookDao, memberDao, borrowDao, new DailyFinePolicy(), clock);

        try {
            if (authService.bootstrapDefaultAdmin()) {
                System.out.println("First run: created default admin account.");
                System.out.println("  username: " + AuthService.DEFAULT_ADMIN_USERNAME
                        + "  password: " + AuthService.DEFAULT_ADMIN_PASSWORD);
                System.out.println("  Please change this password after logging in.");
            }
        } catch (LibraryException e) {
            System.err.println("Startup failed: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("Cause: " + e.getCause().getMessage());
            }
            System.exit(1);
        }

        new ConsoleApp(authService, bookService, memberService, borrowService,
                new InputReader(new Scanner(System.in))).run();
    }
}
