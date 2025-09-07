package pos.pos.Service.Interfecaes;

import pos.pos.DTO.Order.OrderCollectorDTO.OrderCreateDTO;
import pos.pos.DTO.Order.OrderCollectorDTO.OrderResponseDTO;
import pos.pos.DTO.Order.OrderCollectorDTO.OrderStatusUpdateDTO;
import pos.pos.DTO.Order.OrderCollectorDTO.OrderUpdateDTO;

import java.util.List;

public interface OrderService {
    OrderResponseDTO createOrder(OrderCreateDTO dto, String userEmail);
    OrderResponseDTO updateOrder(Long orderId, OrderUpdateDTO dto);
    OrderResponseDTO getOrderById(Long id);
    List<OrderResponseDTO> getAllOrders();
    void deleteOrder(Long id);
    OrderResponseDTO updateStatus(Long id, OrderStatusUpdateDTO dto, String userEmail);
    OrderResponseDTO serveAllItems(Long orderId, String userEmail);
}
