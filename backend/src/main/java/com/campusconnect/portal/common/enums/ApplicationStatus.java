package com.campusconnect.portal.common.enums;

import java.util.EnumSet;
import java.util.Set;

/**
 * Stages of a student's application. The {@link #allowedTransitions()} map encodes the
 * legal state machine so the service layer can reject illegal jumps (e.g. REJECTED ▶ SELECTED).
 */
public enum ApplicationStatus {
    APPLIED,
    SHORTLISTED,
    INTERVIEW_SCHEDULED,
    SELECTED,
    OFFER_RELEASED,
    REJECTED,
    WITHDRAWN;

    /** States from which no further transition is allowed. */
    public boolean isTerminal() {
        return this == REJECTED || this == WITHDRAWN || this == OFFER_RELEASED;
    }

    /** Returns the set of statuses this status may legally transition into. */
    public Set<ApplicationStatus> allowedTransitions() {
        return switch (this) {
            case APPLIED -> EnumSet.of(SHORTLISTED, REJECTED, WITHDRAWN);
            case SHORTLISTED -> EnumSet.of(INTERVIEW_SCHEDULED, REJECTED, WITHDRAWN);
            case INTERVIEW_SCHEDULED -> EnumSet.of(SELECTED, REJECTED, WITHDRAWN);
            case SELECTED -> EnumSet.of(OFFER_RELEASED, REJECTED, WITHDRAWN);
            case OFFER_RELEASED, REJECTED, WITHDRAWN -> EnumSet.noneOf(ApplicationStatus.class);
        };
    }

    public boolean canTransitionTo(ApplicationStatus target) {
        return allowedTransitions().contains(target);
    }
}
