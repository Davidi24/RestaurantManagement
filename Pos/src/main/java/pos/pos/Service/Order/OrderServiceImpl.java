package pos.pos.Service.Order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pos.pos.DTO.Mapper.KDS.KdsMapper;
import pos.pos.DTO.Mapper.OrderMapper.OrderMapper;
import pos.pos.DTO.Order.OrderCollectorDTO.OrderCreateDTO;
import pos.pos.DTO.Order.OrderCollectorDTO.OrderResponseDTO;
import pos.pos.DTO.Order.OrderCollectorDTO.OrderStatusUpdateDTO;
import pos.pos.DTO.Order.OrderCollectorDTO.OrderUpdateDTO;
import pos.pos.Entity.Order.*;
import pos.pos.Entity.User.UserRole;
import pos.pos.Exeption.InvalidOrderStateException;
import pos.pos.Exeption.OpenOrderExistsException;
import pos.pos.Exeption.OrderNotFound;
import pos.pos.Repository.Order.OrderRepository;
import pos.pos.Service.Interfecaes.Order.OrderEventService;
import pos.pos.Service.Interfecaes.Order.OrderService;
import pos.pos.Service.Interfecaes.Order.TotalsService;
import pos.pos.Entity.KDS.KdsTicket;

import pos.pos.Util.NotificationSender;
import pos.pos.Util.OrderNumberFormatter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private final NotificationSender notificationSender;
    private final KdsMapper kdsMapper;


    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED = Map.of(
            OrderStatus.OPEN, Set.of(OrderStatus.ON_HOLD, OrderStatus.SENT_TO_KITCHEN, OrderStatus.VOIDED, OrderStatus.CLOSED, OrderStatus.PARTIALLY_PAID, OrderStatus.PAID),
            OrderStatus.ON_HOLD, Set.of(OrderStatus.OPEN, OrderStatus.SENT_TO_KITCHEN, OrderStatus.VOIDED),
            OrderStatus.SENT_TO_KITCHEN, Set.of(OrderStatus.READY, OrderStatus.VOIDED),
            OrderStatus.READY, Set.of(OrderStatus.OPEN, OrderStatus.PARTIALLY_PAID, OrderStatus.PAID, OrderStatus.VOIDED, OrderStatus.CLOSED),
            OrderStatus.PARTIALLY_PAID, Set.of(OrderStatus.PAID, OrderStatus.CLOSED, OrderStatus.VOIDED),
            OrderStatus.PAID, Set.of(OrderStatus.CLOSED, OrderStatus.VOIDED),
            OrderStatus.VOIDED, Set.of(OrderStatus.VOIDED),
            OrderStatus.CLOSED, Set.of(OrderStatus.CLOSED)
    );


    @Override
    @Transactional
    public OrderResponseDTO createOrder(OrderCreateDTO dto, String userEmail) {
        Long tableId = dto.getTableId();

        orderRepository.findFirstByTableIdAndStatus(tableId, OrderStatus.OPEN)
                .ifPresent(existing -> {
                    throw new OpenOrderExistsException(tableId, existing.getId(), existing.getOrderNumber());
                });

        var order = orderMapper.toOrder(dto);
        order.setUserEmail(userEmail);

        var today = LocalDate.now();
        long seq = orderNumberService.nextFor(today, tableId);
        String orderNumber = orderNumberFormatter.format(tableId, today, seq);
        order.setOrderNumber(orderNumber);

        order = orderRepository.save(order);
        orderEventService.logEvent(order, OrderEventType.CREATED, userEmail, "Order created");

        var orderResponse = orderMapper.toOrderResponse(order);
        notificationSender.sendMessage(
                OrderEventType.CREATED.name(),
                orderResponse,
                UserRole.SUPERADMIN.name(),
                UserRole.KITCHEN.name(),
                UserRole.ADMIN.name()
                );

        return orderResponse;
    }

    @Override
    @Transactional
    public OrderResponseDTO updateOrder(Long orderId, OrderUpdateDTO dto) {
        var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFound(orderId));

        if (order.getStatus() == OrderStatus.CLOSED || order.getStatus() == OrderStatus.VOIDED)
            throw new InvalidOrderStateException("Cannot update a closed or voided order");

        if (dto.getNotes() != null) order.setNotes(dto.getNotes());

        if (dto.getNumberOfGuests() != null && dto.getNumberOfGuests() >= 0)
            order.setNumberOfGuests(dto.getNumberOfGuests());

        if (dto.getTableId() != null && !dto.getTableId().equals(order.getTableId())) {
            orderRepository.findFirstByTableIdAndStatus(dto.getTableId(), OrderStatus.OPEN)
                    .ifPresent(existing -> { if (!existing.getId().equals(order.getId()))
                        throw new OpenOrderExistsException(dto.getTableId(), existing.getId(), existing.getOrderNumber());
                    });
            order.setTableId(dto.getTableId());
        }
        return orderMapper.toOrderResponse(orderRepository.save(order));
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

    @Transactional
    public OrderResponseDTO updateStatus(Long id, OrderStatusUpdateDTO dto, String userEmail) {
        var order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFound(id));
        applyStatusTransition(order, dto.getStatus(), userEmail); // reason = null
        return orderMapper.toOrderResponse(orderRepository.save(order));
    }

    private void applyStatusTransition(Order order, OrderStatus target, String userEmail) {
        if (target == null) {
            throw new InvalidOrderStateException("Target status is required");
        }

        var from = order.getStatus();

        if (!ALLOWED.getOrDefault(from, Set.of()).contains(target)) {
            throw new InvalidOrderStateException("Illegal status transition: " + from + " → " + target);
        }

        if (target == OrderStatus.CLOSED) {
            totalsService.recalculateTotals(order);
            boolean allDone = order.getLineItems().stream().allMatch(li ->
                    li.getFulfillmentStatus() == FulfillmentStatus.SERVED ||
                            li.getFulfillmentStatus() == FulfillmentStatus.VOIDED);
            if (!allDone) throw new InvalidOrderStateException("Items not served/voided");
            order.setClosedAt(LocalDateTime.now());
            order.setStatus(OrderStatus.CLOSED);
            orderEventService.logEvent(order, OrderEventType.CLOSED, userEmail, "Order closed");
            return;
        }

        if (target == OrderStatus.VOIDED) {
            order.setClosedAt(LocalDateTime.now());
            order.setStatus(OrderStatus.VOIDED);
            orderEventService.logEvent(order, OrderEventType.VOIDED, userEmail, "Order voided");
            return;
        }

        order.setStatus(target);
        if (target == OrderStatus.OPEN) order.setClosedAt(null);

        if (target == OrderStatus.SENT_TO_KITCHEN) {
            KdsTicket kdsTicket = kdsMapper.toKdsTicket(order); // build KdsTicket DTO
            notificationSender.sendMessage(
                    "KDS_TICKET_CREATED",
                    kdsTicket,
                    UserRole.KITCHEN.name()
            );
        }

        orderEventService.logEvent(order, OrderEventType.STATUS_CHANGED, userEmail, "Status: " + from + " → " + target);
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

    @Override
    public OrderResponseDTO serveOneItem(Long orderId, Long lineItemId, String userEmail) {
        var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFound(orderId));

        var lineItem = order.getLineItems().stream()
                .filter(li -> li.getId().equals(lineItemId))
                .findFirst()
                .orElseThrow(() -> new InvalidOrderStateException("Line item not found in order " + orderId));

        if (lineItem.getFulfillmentStatus() == FulfillmentStatus.SERVED) {
            throw new InvalidOrderStateException("Line item is already served");
        }
        if (order.getStatus() == OrderStatus.CLOSED || order.getStatus() == OrderStatus.VOIDED) {
            throw new InvalidOrderStateException("Cannot serve items on a closed or voided order");
        }

        lineItem.setFulfillmentStatus(FulfillmentStatus.SERVED);

        orderRepository.save(order);

        orderEventService.logEvent(order, OrderEventType.ITEM_UPDATED, userEmail,
                "Item " + lineItemId + " served");

        return orderMapper.toOrderResponse(order);
    }

}
