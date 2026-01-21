package com.ankurshala.backend.entity;

public final class BookingStateMapper {
    private BookingStateMapper() {}

    public static String toState(BookingStatus status) {
        if (status == null) {
            return null;
        }

        switch (status) {
            case PENDING:
                return BookingState.REQUESTED.name();
            case IN_PROGRESS:
                return BookingState.ACTIVE.name();
            case ACCEPTED:
            case CONFIRMED:
            case COMPLETED:
            case CANCELLED:
            case EXPIRED:
                return status.name();
            case REFUNDED:
            case NO_SHOW_STUDENT:
            case NO_SHOW_TEACHER:
                return status.name();
            default:
                return status.name();
        }
    }
}
