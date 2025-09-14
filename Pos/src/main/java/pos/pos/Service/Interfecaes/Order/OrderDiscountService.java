package pos.pos.Service.Interfecaes.Order;

import pos.pos.DTO.Order.OrderDiscount.OrderDiscountCreateDTO;
import pos.pos.DTO.Order.OrderDiscount.OrderDiscountResponseDTO;
import pos.pos.DTO.Order.OrderDiscount.OrderDiscountUpdateDTO;

import java.util.List;

public interface OrderDiscountService {
    OrderDiscountResponseDTO addDiscount(Long orderId, OrderDiscountCreateDTO dto, String userEmail);
    OrderDiscountResponseDTO updateDiscount(Long orderId, OrderDiscountUpdateDTO dto, String userEmail);
    void removeDiscount(Long orderId, Long discountId, String userEmail);
    List<OrderDiscountResponseDTO> getDiscounts(Long orderId);
    OrderDiscountResponseDTO getDiscountById(Long orderId, Long discountId);
}
