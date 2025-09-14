package pos.pos.Service.Interfecaes.KDS;

import pos.pos.Entity.Order.FulfillmentStatus;

public interface KdsService {

    void updateItemStatus(Long orderId, Long lineItemId, FulfillmentStatus status, String userEmail);

    void markTicketReady(Long orderId, String userEmail);

    void bumpTicket(Long orderId, String userEmail);

    void recallTicket(Long orderId, String userEmail);
}
