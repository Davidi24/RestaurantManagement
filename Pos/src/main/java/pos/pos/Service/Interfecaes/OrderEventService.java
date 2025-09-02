package pos.pos.Service.Interfecaes;

import pos.pos.DTO.Order.OrderEventResponseDTO;
import pos.pos.Entity.Order.OrderEventType;
import pos.pos.Entity.Order.Order;

import java.util.List;

public interface OrderEventService {
    void logEvent(Order order, OrderEventType type, String userEmail, String metadata);
    List<OrderEventResponseDTO> getEvents(Long orderId);
}
