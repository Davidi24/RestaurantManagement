package pos.pos.Service.Order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pos.pos.DTO.Order.OrderCollectorDTO.OrderCreateDTO;
import pos.pos.DTO.Order.OrderCollectorDTO.OrderResponseDTO;
import pos.pos.DTO.Order.OrderCollectorDTO.OrderStatusUpdateDTO;
import pos.pos.DTO.Order.OrderCollectorDTO.OrderUpdateDTO;
import pos.pos.Entity.Order.OrderEventType;
import pos.pos.Entity.Order.OrderStatus;
import pos.pos.Exeption.InvalidOrderStateException;
import pos.pos.Exeption.OrderNotFound;
import pos.pos.Repository.Order.OrderRepository;
import pos.pos.DTO.Mapper.OrderMapper.OrderMapper;
import pos.pos.Service.Interfecaes.OrderEventService;
import pos.pos.Service.Interfecaes.OrderService;


import java.time.LocalDateTime;
import java.util.List;

//TODO: Filter order by date and by pagination

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderEventService orderEventService;

    @Override
    public OrderResponseDTO createOrder(OrderCreateDTO dto, String userEmail) {
        var order = orderMapper.toOrder(dto);
        order = orderRepository.save(order);

        order.setOrderNumber("T" + order.getTableId() + "-" + order.getId());
        order.setOpenedAt(LocalDateTime.now());
        order = orderRepository.save(order);

        orderEventService.logEvent(
                order,
                OrderEventType.CREATED,
                userEmail,
                "Order created"
        );
        return orderMapper.toOrderResponse(order);
    }

    @Override
    public OrderResponseDTO updateOrder(Long orderId, OrderUpdateDTO dto) {
        var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFound(orderId));

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
        var order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFound(id));
        return orderMapper.toOrderResponse(order);
    }

    @Override
    public List<OrderResponseDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(orderMapper::toOrderResponse)
                .toList();
    }

    @Override
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new OrderNotFound(id);
        }
        orderRepository.deleteById(id);
    }

    public OrderResponseDTO updateStatus(Long id, OrderStatusUpdateDTO dto) {
        var order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFound(id));
        order.setStatus(dto.getStatus());
        if (dto.getStatus() == OrderStatus.CLOSED || dto.getStatus() == OrderStatus.VOIDED)
            order.setClosedAt(LocalDateTime.now());
        order = orderRepository.save(order);
        return orderMapper.toOrderResponse(order);
    }
}
