package com.library.dao;

import com.library.model.Member;
import java.util.Optional;

public interface MemberDao extends Repository<Member, Integer> {
    Optional<Member> findByEmail(String email);
}
