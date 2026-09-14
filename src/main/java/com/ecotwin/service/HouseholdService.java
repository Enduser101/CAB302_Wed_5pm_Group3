package com.ecotwin.service;

import com.ecotwin.dao.ActivityLogDao;
import com.ecotwin.dao.HouseholdDao;
import com.ecotwin.dao.HouseholdMembershipDao;
import com.ecotwin.model.Household;
import com.ecotwin.model.HouseholdMembership;
import com.ecotwin.model.User;

import java.security.SecureRandom;
import java.util.Optional;

public class HouseholdService {

    private static final String JOIN_CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // no 0/O/1/I
    private static final int JOIN_CODE_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final HouseholdDao householdDao;
    private final HouseholdMembershipDao membershipDao;
    private final ActivityLogDao activityLogDao;

    public HouseholdService(HouseholdDao householdDao, HouseholdMembershipDao membershipDao,
                             ActivityLogDao activityLogDao) {
        this.householdDao = householdDao;
        this.membershipDao = membershipDao;
        this.activityLogDao = activityLogDao;
    }

    /** US-08: creator becomes admin, household persists after restart. */
    public Household createHousehold(User creator, String name, int occupants, String dwellingType, String state) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Household name is required");
        }
        if (occupants < 1) {
            throw new IllegalArgumentException("Number of occupants must be at least 1");
        }
        String joinCode = generateUniqueJoinCode();
        Household household = householdDao.create(name, occupants, dwellingType, state, joinCode);
        membershipDao.addMember(creator.getId(), household.getId(), HouseholdMembership.Role.ADMIN);
        activityLogDao.log(household.getId(), creator.getId(), displayName(creator) + " created the household");
        return household;
    }

    /** US-09: join by code; invalid code raises an error the UI turns into "No household found with that code". */
    public Household joinHousehold(User user, String joinCode) {
        if (joinCode == null || joinCode.isBlank()) {
            throw new IllegalArgumentException("Enter a join code");
        }
        Household household = householdDao.findByJoinCode(joinCode.trim().toUpperCase())
            .orElseThrow(() -> new IllegalArgumentException("No household found with that code"));
        membershipDao.addMember(user.getId(), household.getId(), HouseholdMembership.Role.MEMBER);
        activityLogDao.log(household.getId(), user.getId(), displayName(user) + " joined the household");
        return household;
    }

    public Optional<Household> findActiveHouseholdForUser(User user) {
        return membershipDao.findAnyActiveByUser(user.getId())
            .flatMap(membership -> householdDao.findById(membership.getHouseholdId()));
    }

    public Optional<HouseholdMembership> findActiveMembership(User user, Household household) {
        return membershipDao.findActiveByUserAndHousehold(user.getId(), household.getId());
    }

    private String generateUniqueJoinCode() {
        String code;
        do {
            StringBuilder sb = new StringBuilder(JOIN_CODE_LENGTH);
            for (int i = 0; i < JOIN_CODE_LENGTH; i++) {
                sb.append(JOIN_CODE_ALPHABET.charAt(RANDOM.nextInt(JOIN_CODE_ALPHABET.length())));
            }
            code = sb.toString();
        } while (householdDao.findByJoinCode(code).isPresent());
        return code;
    }

    private String displayName(User user) {
        return user.getDisplayName() != null ? user.getDisplayName() : user.getUsername();
    }
}
