package pos.pos.Service.Interfecaes;


import pos.pos.DTO.Order.OrderLineItemDTO.OrderLineItemCreateDTO;
import pos.pos.DTO.Order.OrderLineItemDTO.OrderLineItemResponseDTO;
import pos.pos.DTO.Order.OrderLineItemDTO.OrderLineItemUpdateDTO;

import java.util.List;

public interface OrderLineItemService {
    OrderLineItemResponseDTO addLineItem(Long orderId, OrderLineItemCreateDTO dto);
    OrderLineItemResponseDTO updateLineItem(Long orderId, OrderLineItemUpdateDTO dto);
    List<OrderLineItemResponseDTO> getLineItems(Long orderId);
    void deleteLineItem(Long orderId, Long lineItemId);
    OrderLineItemResponseDTO getLineItemById(Long orderId, Long lineItemId);

}
