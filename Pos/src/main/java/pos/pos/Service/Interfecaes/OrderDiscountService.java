package pos.pos.Service.Interfecaes;

import pos.pos.DTO.Order.OrderDiscount.OrderDiscountCreateDTO;
import pos.pos.DTO.Order.OrderDiscount.OrderDiscountResponseDTO;
import pos.pos.DTO.Order.OrderDiscount.OrderDiscountUpdateDTO;

import java.util.List;

public interface OrderDiscountService {
    OrderDiscountResponseDTO addDiscount(Long orderId, OrderDiscountCreateDTO dto);
    OrderDiscountResponseDTO updateDiscount(Long orderId, OrderDiscountUpdateDTO dto);
    void removeDiscount(Long orderId, Long discountId);
    List<OrderDiscountResponseDTO> getDiscounts(Long orderId);
    OrderDiscountResponseDTO getDiscountById(Long orderId, Long discountId);
}
