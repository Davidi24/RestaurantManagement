package pos.pos.Service.Interfecaes;



import pos.pos.DTO.Order.OrderCollectorDTO.OrderCreateDTO;
import pos.pos.DTO.Order.OrderCollectorDTO.OrderResponseDTO;
import pos.pos.DTO.Order.OrderCollectorDTO.OrderUpdateDTO;

import java.util.List;

public interface OrderService {
    OrderResponseDTO createOrder(OrderCreateDTO dto);
    OrderResponseDTO updateOrder(OrderUpdateDTO dto);
    OrderResponseDTO getOrderById(Long id);
    List<OrderResponseDTO> getAllOrders();
    void deleteOrder(Long id);
}
