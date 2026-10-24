package com.library.dao;

import com.library.model.Book;
import java.util.List;
import java.util.Optional;

public interface BookDao extends Repository<Book, Integer> {
    Optional<Book> findByIsbn(String isbn);

    /** Case-insensitive match on title, author, category or ISBN. */
    List<Book> search(String keyword);
}
