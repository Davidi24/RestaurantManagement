package pos.pos.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pos.pos.DTO.Mapper.OrderDiscountMapper;
import pos.pos.DTO.Order.OrderDiscount.OrderDiscountCreateDTO;
import pos.pos.DTO.Order.OrderDiscount.OrderDiscountResponseDTO;
import pos.pos.DTO.Order.OrderDiscount.OrderDiscountUpdateDTO;
import pos.pos.Entity.Order.Order;
import pos.pos.Entity.Order.OrderLineItem;
import pos.pos.Repository.OrderDiscountRepository;
import pos.pos.Repository.OrderLineItemRepository;
import pos.pos.Repository.OrderRepository;
import pos.pos.Service.Interfecaes.OrderDiscountService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderDiscountServiceImpl implements OrderDiscountService {

    private final OrderRepository orderRepository;
    private final OrderLineItemRepository lineItemRepository;
    private final OrderDiscountRepository discountRepository;
    private final OrderDiscountMapper discountMapper;

    @Override
    public OrderDiscountResponseDTO addDiscount(Long orderId, OrderDiscountCreateDTO dto) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        var discount = discountMapper.toOrderDiscount(dto);
        discount.setOrder(order);
        if (!Boolean.TRUE.equals(dto.getOrderLevel()) && dto.getLineItemId() != null) {
            OrderLineItem li = lineItemRepository.findById(dto.getLineItemId()).orElseThrow(() -> new RuntimeException("LineItem not found"));
            if (!li.getOrder().getId().equals(orderId)) throw new RuntimeException("LineItem does not belong to this order");
            discount.setLineItem(li);
            discount.setOrderLevel(false);
        } else {
            discount.setOrderLevel(true);
        }
        discount = discountRepository.save(discount);
        return discountMapper.toOrderDiscountResponse(discount);
    }

    @Override
    public OrderDiscountResponseDTO updateDiscount(Long orderId, OrderDiscountUpdateDTO dto) {
        var discount = discountRepository.findById(dto.getId()).orElseThrow(() -> new RuntimeException("Discount not found"));
        if (!discount.getOrder().getId().equals(orderId)) throw new RuntimeException("Discount does not belong to this order");
        discount.setName(dto.getName());
        discount.setPercentage(dto.getPercentage());
        discount.setAmount(dto.getAmount());
        discount = discountRepository.save(discount);
        return discountMapper.toOrderDiscountResponse(discount);
    }

    @Override
    public void removeDiscount(Long orderId, Long discountId) {
        var discount = discountRepository.findById(discountId).orElseThrow(() -> new RuntimeException("Discount not found"));
        if (!discount.getOrder().getId().equals(orderId)) throw new RuntimeException("Discount does not belong to this order");
        discountRepository.delete(discount);
    }

    @Override
    public List<OrderDiscountResponseDTO> getDiscounts(Long orderId) {
        orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        return discountRepository.findByOrder_Id(orderId).stream()
                .map(discountMapper::toOrderDiscountResponse)
                .toList();
    }

    @Override
    public OrderDiscountResponseDTO getDiscountById(Long orderId, Long discountId) {
        var discount = discountRepository.findById(discountId).orElseThrow(() -> new RuntimeException("Discount not found"));
        if (!discount.getOrder().getId().equals(orderId)) throw new RuntimeException("Discount does not belong to this order");
        return discountMapper.toOrderDiscountResponse(discount);
    }
}
