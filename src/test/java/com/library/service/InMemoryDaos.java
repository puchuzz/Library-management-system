package com.library.service;

import com.library.dao.BookDao;
import com.library.dao.BorrowDao;
import com.library.dao.MemberDao;
import com.library.exception.ResourceNotFoundException;
import com.library.model.Book;
import com.library.model.BorrowRecord;
import com.library.model.Member;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** In-memory fakes so the service layer can be unit-tested without MySQL. */
public final class InMemoryDaos {
    private InMemoryDaos() { }

    public static class Books implements BookDao {
        private final Map<Integer, Book> store = new LinkedHashMap<>();
        private int nextId = 1;

        @Override public Book save(Book book) { book.setId(nextId++); store.put(book.getId(), book); return book; }
        @Override public Optional<Book> findById(Integer id) { return Optional.ofNullable(store.get(id)); }
        @Override public List<Book> findAll() { return new ArrayList<>(store.values()); }
        @Override public boolean update(Book book) { return store.put(book.getId(), book) != null; }
        @Override public boolean deleteById(Integer id) { return store.remove(id) != null; }

        @Override
        public Optional<Book> findByIsbn(String isbn) {
            return store.values().stream().filter(b -> b.getIsbn().equals(isbn)).findFirst();
        }

        @Override
        public List<Book> search(String keyword) {
            String k = keyword.toLowerCase();
            return store.values().stream()
                    .filter(b -> b.getTitle().toLowerCase().contains(k) || b.getAuthor().toLowerCase().contains(k))
                    .toList();
        }
    }

    public static class Members implements MemberDao {
        private final Map<Integer, Member> store = new LinkedHashMap<>();
        private int nextId = 1;

        @Override public Member save(Member m) { m.setId(nextId++); store.put(m.getId(), m); return m; }
        @Override public Optional<Member> findById(Integer id) { return Optional.ofNullable(store.get(id)); }
        @Override public List<Member> findAll() { return new ArrayList<>(store.values()); }
        @Override public boolean update(Member m) { return store.put(m.getId(), m) != null; }
        @Override public boolean deleteById(Integer id) { return store.remove(id) != null; }

        @Override
        public Optional<Member> findByEmail(String email) {
            return store.values().stream().filter(m -> m.getEmail().equals(email)).findFirst();
        }
    }

    public static class Borrows implements BorrowDao {
        private final Books books;
        private final Members members;
        private final List<BorrowRecord> records = new ArrayList<>();

        public Borrows(Books books, Members members) {
            this.books = books;
            this.members = members;
        }

        @Override
        public BorrowRecord issue(int bookId, int memberId, int issuedBy, LocalDate borrowDate, LocalDate dueDate) {
            Book book = books.findById(bookId).orElseThrow();
            book.checkOut(); // throws BookNotAvailableException when stock is zero
            BorrowRecord r = new BorrowRecord(records.size() + 1, bookId, memberId, issuedBy,
                    borrowDate, dueDate, null, BigDecimal.ZERO);
            r.setBookTitle(book.getTitle());
            r.setMemberName(members.findById(memberId).orElseThrow().getFullName());
            records.add(r);
            return r;
        }

        @Override
        public BorrowRecord closeRecord(int recordId, LocalDate returnedOn, BigDecimal fine) {
            BorrowRecord r = findById(recordId).orElseThrow(() -> new ResourceNotFoundException("no loan"));
            r.setReturnDate(returnedOn);
            r.setFineAmount(fine);
            books.findById(r.getBookId()).orElseThrow().checkIn();
            return r;
        }

        @Override
        public Optional<BorrowRecord> findById(int recordId) {
            return records.stream().filter(r -> r.getId() == recordId).findFirst();
        }

        @Override
        public List<BorrowRecord> findActiveByMember(int memberId) {
            return records.stream().filter(r -> r.getMemberId() == memberId && !r.isReturned()).toList();
        }

        @Override
        public List<BorrowRecord> findByMember(int memberId) {
            return records.stream().filter(r -> r.getMemberId() == memberId).toList();
        }

        @Override
        public List<BorrowRecord> findAllActive() {
            return records.stream().filter(r -> !r.isReturned()).toList();
        }

        @Override
        public List<BorrowRecord> findOverdue(LocalDate today) {
            return records.stream().filter(r -> r.isOverdue(today)).toList();
        }
    }
}
