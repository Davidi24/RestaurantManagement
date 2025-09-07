package pos.pos.Service.Order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pos.pos.DTO.Mapper.OrderMapper.OrderMapper;
import pos.pos.DTO.Order.OrderCollectorDTO.OrderCreateDTO;
import pos.pos.DTO.Order.OrderCollectorDTO.OrderResponseDTO;
import pos.pos.DTO.Order.OrderCollectorDTO.OrderStatusUpdateDTO;
import pos.pos.DTO.Order.OrderCollectorDTO.OrderUpdateDTO;
import pos.pos.Entity.Order.FulfillmentStatus;
import pos.pos.Entity.Order.OrderNumberCounter;
import pos.pos.Entity.Order.OrderStatus;
import pos.pos.Entity.Order.OrderEventType;
import pos.pos.Exeption.InvalidOrderStateException;
import pos.pos.Exeption.OrderNotFound;
import pos.pos.Repository.Order.OrderRepository;
import pos.pos.Service.Interfecaes.OrderEventService;
import pos.pos.Service.Interfecaes.OrderService;
import pos.pos.Service.Interfecaes.TotalsService;
import pos.pos.Util.OrderNumberFormatter;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderEventService orderEventService;
    private final TotalsService totalsService;
    private final OrderNumberService orderNumberService;
    private final OrderNumberFormatter orderNumberFormatter;

    @Override
    public OrderResponseDTO createOrder(OrderCreateDTO dto, String userEmail) {
        var order = orderMapper.toOrder(dto);
        order.setUserEmail(userEmail);

        var today = java.time.LocalDate.now();
        long seq = orderNumberService.nextFor(today, order.getTableId());
        String orderNumber = orderNumberFormatter.format(order.getTableId(), today, seq);
        order.setOrderNumber(orderNumber);

        order = orderRepository.save(order);
        orderEventService.logEvent(order, OrderEventType.CREATED, userEmail, "Order created");
        return orderMapper.toOrderResponse(order);
    }



    @Override
    public OrderResponseDTO updateOrder(Long orderId, OrderUpdateDTO dto) {
        var order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFound(orderId));
        if (order.getStatus() == OrderStatus.CLOSED || order.getStatus() == OrderStatus.VOIDED) {
            throw new InvalidOrderStateException("Cannot update a closed or voided order");
        }
        if (dto.getNotes() != null) {
            order.setNotes(dto.getNotes());
        }
        if (dto.getTableId() != null) {
            order.setTableId(dto.getTableId());
        }
        order = orderRepository.save(order);
        return orderMapper.toOrderResponse(order);
    }

    @Override
    public OrderResponseDTO getOrderById(Long id) {
        var order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFound(id));
        return orderMapper.toOrderResponse(order);
    }

    @Override
    public List<OrderResponseDTO> getAllOrders() {
        return orderRepository.findAll().stream().map(orderMapper::toOrderResponse).toList();
    }

    @Override
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new OrderNotFound(id);
        }
        orderRepository.deleteById(id);
    }

    @Override
    public OrderResponseDTO updateStatus(Long id, OrderStatusUpdateDTO dto) {
        var order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFound(id));
        order.setStatus(dto.getStatus());
        if (dto.getStatus() == OrderStatus.CLOSED || dto.getStatus() == OrderStatus.VOIDED) {
            order.setClosedAt(LocalDateTime.now());
        }
        order = orderRepository.save(order);
        return orderMapper.toOrderResponse(order);
    }

    @Override
    public OrderResponseDTO closeOrder(Long id, String userEmail) {
        var order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFound(id));
        totalsService.recalculateTotals(order);
        boolean allDone = order.getLineItems().stream().allMatch(li ->
                li.getFulfillmentStatus() == FulfillmentStatus.SERVED
                        || li.getFulfillmentStatus() == FulfillmentStatus.VOIDED);
        if (!allDone) {
            throw new InvalidOrderStateException("Items not served/voided");
        }
        order.setStatus(OrderStatus.CLOSED);
        order.setClosedAt(LocalDateTime.now());
        order = orderRepository.save(order);
        orderEventService.logEvent(order, OrderEventType.CLOSED, userEmail, "Order closed");
        return orderMapper.toOrderResponse(order);
    }

    @Override
    public OrderResponseDTO voidOrder(Long id, String reason, String userEmail) {
        var order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFound(id));
        if (order.getStatus() == OrderStatus.CLOSED) {
            throw new InvalidOrderStateException("Closed orders cannot be voided");
        }
        order.setStatus(OrderStatus.VOIDED);
        order.setClosedAt(LocalDateTime.now());
        order = orderRepository.save(order);
        orderEventService.logEvent(order, OrderEventType.VOIDED, userEmail, reason != null ? reason : "Void");
        return orderMapper.toOrderResponse(order);
    }

    @Override
    public OrderResponseDTO serveAllItems(Long orderId, String userEmail) {
        var order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFound(orderId));

        order.getLineItems().forEach(li -> {
            li.setFulfillmentStatus(FulfillmentStatus.SERVED);
        });

        orderRepository.save(order);
        orderEventService.logEvent(order, OrderEventType.ITEM_UPDATED, userEmail, "All items served");

        return orderMapper.toOrderResponse(order);
    }

}
