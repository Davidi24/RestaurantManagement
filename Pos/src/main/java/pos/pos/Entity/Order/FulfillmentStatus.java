package pos.pos.Entity.Order;

public enum FulfillmentStatus {
    NEW, FIRED, READY, SERVED, VOIDED;

    public boolean canTransitionTo(FulfillmentStatus to) {
        if (to == null) return true;
        return !switch (this) {
            case NEW -> to == FIRED || to == VOIDED;
            case FIRED -> to == READY || to == VOIDED;
            case READY -> to == SERVED || to == VOIDED;
            case SERVED, VOIDED -> false;
        };
    }

    public static boolean isValidTransition(FulfillmentStatus from, FulfillmentStatus to) {
        if (from == null) return to == NEW || to == FIRED;
        return from.canTransitionTo(to);
    }
}
