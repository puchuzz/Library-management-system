package com.library.dao;

import java.util.List;
import java.util.Optional;

/** Generic CRUD contract implemented by every DAO (abstraction + polymorphism). */
public interface Repository<T, ID> {
    /** Inserts a new entity and returns it with its generated id. */
    T save(T entity);

    Optional<T> findById(ID id);

    List<T> findAll();

    /** @return true if a row was updated */
    boolean update(T entity);

    /** @return true if a row was deleted */
    boolean deleteById(ID id);
}
