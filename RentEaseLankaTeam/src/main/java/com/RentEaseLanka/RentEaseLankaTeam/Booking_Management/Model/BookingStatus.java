package com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model;


import java.util.EnumSet;
import java.util.Set;

public enum BookingStatus {
    PENDING {
        @Override
        public Set<BookingStatus> nextValidStatuses() {
            return EnumSet.of(CONFIRMED, CANCELLED);
        }
    },
    CONFIRMED {
        @Override
        public Set<BookingStatus> nextValidStatuses() {
            return EnumSet.of(ACTIVE, CANCELLED);
        }
    },
    ACTIVE {
        @Override
        public Set<BookingStatus> nextValidStatuses() {
            return EnumSet.of(COMPLETED, CANCELLED);
        }
    },
    COMPLETED {
        @Override
        public Set<BookingStatus> nextValidStatuses() {
            return EnumSet.noneOf(BookingStatus.class); // Terminal state
        }
    },
    CANCELLED {
        @Override
        public Set<BookingStatus> nextValidStatuses() {
            return EnumSet.noneOf(BookingStatus.class); // Terminal state
        }
    };


    public abstract Set<BookingStatus> nextValidStatuses();


    public boolean canTransitionTo(BookingStatus target) {
        return nextValidStatuses().contains(target);
    }


    public BookingStatus transitionTo(BookingStatus target) {
        if (!canTransitionTo(target)) {
            throw new IllegalStateException(
                    String.format("Invalid state transition from [%s] to [%s]", this, target)
            );
        }
        return target;
    }
}
