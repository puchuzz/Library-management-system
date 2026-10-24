package com.library.service;

import com.library.dao.MemberDao;
import com.library.exception.DuplicateResourceException;
import com.library.exception.ResourceNotFoundException;
import com.library.model.Member;
import com.library.model.MembershipType;
import com.library.model.Permission;
import com.library.model.Staff;
import com.library.security.AccessControl;
import com.library.util.Validator;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/** Member registration and account status. */
public class MemberService {
    private final MemberDao memberDao;
    private final Clock clock;

    public MemberService(MemberDao memberDao) {
        this(memberDao, Clock.systemDefaultZone());
    }

    public MemberService(MemberDao memberDao, Clock clock) {
        this.memberDao = memberDao;
        this.clock = clock;
    }

    public Member register(Staff actor, String fullName, String email, String phone, MembershipType type) {
        AccessControl.require(actor, Permission.MANAGE_MEMBERS);
        String cleanName = Validator.requireNonBlank(fullName, "Full name");
        String cleanEmail = Validator.requireEmail(email);
        String cleanPhone = Validator.optionalPhone(phone);
        if (memberDao.findByEmail(cleanEmail).isPresent()) {
            throw new DuplicateResourceException("A member with email " + cleanEmail + " already exists");
        }
        Member member = Member.create(type, 0, cleanName, cleanEmail, cleanPhone, LocalDate.now(clock), true);
        return memberDao.save(member);
    }

    public Member getMember(int memberId) {
        return memberDao.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found: " + memberId));
    }

    public List<Member> listAll(Staff actor) {
        AccessControl.require(actor, Permission.MANAGE_MEMBERS);
        return memberDao.findAll();
    }

    public void setActive(Staff actor, int memberId, boolean active) {
        AccessControl.require(actor, Permission.MANAGE_MEMBERS);
        Member member = getMember(memberId);
        member.setActive(active);
        memberDao.update(member);
    }
}
