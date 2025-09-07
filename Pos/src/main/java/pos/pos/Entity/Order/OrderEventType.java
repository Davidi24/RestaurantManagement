package pos.pos.Entity.Order;

public enum OrderEventType {
    CREATED,
    ITEM_ADDED,
    ITEM_UPDATED,   //
    ITEM_DELETED,
    ITEM_REMOVED,
    DISCOUNT_APPLIED,
    DISCOUNT_REMOVED,
    SERVICE_CHARGE_ADDED,
    PAID,
    REFUNDED,
    CLOSED,
    VOIDED,
    STATUS_CHANGED
}
