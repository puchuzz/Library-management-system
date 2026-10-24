package com.library.dao;

import com.library.model.Staff;
import java.util.Optional;

public interface StaffDao extends Repository<Staff, Integer> {
    Optional<Staff> findByUsername(String username);

    long count();
}
