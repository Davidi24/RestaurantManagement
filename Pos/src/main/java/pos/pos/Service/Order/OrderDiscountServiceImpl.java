package pos.pos.Service.Order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pos.pos.DTO.Mapper.OrderMapper.OrderDiscountMapper;
import pos.pos.DTO.Order.OrderDiscount.OrderDiscountCreateDTO;
import pos.pos.DTO.Order.OrderDiscount.OrderDiscountResponseDTO;
import pos.pos.DTO.Order.OrderDiscount.OrderDiscountUpdateDTO;
import pos.pos.Entity.Order.Order;
import pos.pos.Entity.Order.OrderDiscount;
import pos.pos.Entity.Order.OrderEventType;
import pos.pos.Entity.Order.OrderLineItem;
import pos.pos.Exeption.LineItemOrderMismatchException;
import pos.pos.Exeption.OrderItemNotFound;
import pos.pos.Exeption.OrderNotFound;
import pos.pos.Repository.Order.OrderDiscountRepository;
import pos.pos.Repository.Order.OrderLineItemRepository;
import pos.pos.Repository.Order.OrderRepository;
import pos.pos.Service.Interfecaes.Order.OrderDiscountService;
import pos.pos.Service.Interfecaes.Order.OrderEventService;
import pos.pos.Service.Interfecaes.Order.TotalsService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderDiscountServiceImpl implements OrderDiscountService {

    private final OrderRepository orderRepository;
    private final OrderLineItemRepository lineItemRepository;
    private final OrderDiscountRepository discountRepository;
    private final OrderDiscountMapper discountMapper;
    private final TotalsService totalsService;
    private final OrderEventService orderEventService;

    @Override
    public OrderDiscountResponseDTO addDiscount(Long orderId, OrderDiscountCreateDTO dto, String userEmail) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFound(orderId));
        OrderDiscount discount = discountMapper.toOrderDiscount(dto);
        discount.setOrder(order);
        if (Boolean.TRUE.equals(dto.getOrderLevel())) {
            discount.setOrderLevel(true);
        } else if (dto.getLineItemId() != null) {
            OrderLineItem li = lineItemRepository.findById(dto.getLineItemId())
                    .orElseThrow(() -> new OrderItemNotFound(orderId, dto.getLineItemId()));
            if (!li.getOrder().getId().equals(orderId)) throw new LineItemOrderMismatchException(li.getId(), orderId);
            discount.setLineItem(li);
            discount.setOrderLevel(false);
        } else {
            discount.setOrderLevel(true);
        }
        discount = discountRepository.save(discount);
        totalsService.recalculateTotals(order);
        orderEventService.logEvent(order, OrderEventType.DISCOUNT_APPLIED, userEmail, "Discount added: " + discount.getName());
        return discountMapper.toOrderDiscountResponse(discount);
    }

    @Override
    public OrderDiscountResponseDTO updateDiscount(Long orderId, OrderDiscountUpdateDTO dto, String userEmail) {
        OrderDiscount discount = discountRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Discount not found"));
        if (!discount.getOrder().getId().equals(orderId)) throw new RuntimeException("Discount does not belong to this order");
        discount.setName(dto.getName());
        discount.setPercentage(dto.getPercentage());
        discount.setAmount(dto.getAmount());
        discount = discountRepository.save(discount);
        totalsService.recalculateTotals(discount.getOrder());
        orderEventService.logEvent(discount.getOrder(), OrderEventType.DISCOUNT_APPLIED, userEmail, "Discount updated: " + discount.getName());
        return discountMapper.toOrderDiscountResponse(discount);
    }

    @Override
    public void removeDiscount(Long orderId, Long discountId, String userEmail) {
        OrderDiscount discount = discountRepository.findById(discountId)
                .orElseThrow(() -> new RuntimeException("Discount not found"));
        if (!discount.getOrder().getId().equals(orderId)) throw new RuntimeException("Discount does not belong to this order");
        Order order = discount.getOrder();
        discountRepository.delete(discount);
        totalsService.recalculateTotals(order);
        orderEventService.logEvent(order, OrderEventType.DISCOUNT_REMOVED, userEmail, "Discount removed: " + discount.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDiscountResponseDTO> getDiscounts(Long orderId) {
        orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFound(orderId));
        return discountRepository.findByOrder_Id(orderId).stream()
                .map(discountMapper::toOrderDiscountResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDiscountResponseDTO getDiscountById(Long orderId, Long discountId) {
        OrderDiscount discount = discountRepository.findById(discountId)
                .orElseThrow(() -> new RuntimeException("Discount not found"));
        if (!discount.getOrder().getId().equals(orderId)) throw new RuntimeException("Discount does not belong to this order");
        return discountMapper.toOrderDiscountResponse(discount);
    }
}
