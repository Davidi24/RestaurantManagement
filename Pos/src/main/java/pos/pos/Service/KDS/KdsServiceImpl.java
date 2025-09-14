package pos.pos.Service.KDS;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pos.pos.DTO.Mapper.KDS.KdsMapper;
import pos.pos.Entity.KDS.KdsTicket;
import pos.pos.Entity.Order.*;
import pos.pos.Entity.User.UserRole;
import pos.pos.Exeption.InvalidOrderStateException;
import pos.pos.Exeption.OrderItemNotFound;
import pos.pos.Exeption.OrderNotFound;
import pos.pos.Repository.Order.OrderLineItemRepository;
import pos.pos.Repository.Order.OrderRepository;
import pos.pos.Service.Interfecaes.KDS.KdsService;
import pos.pos.Service.Interfecaes.Order.OrderEventService;
import pos.pos.Util.NotificationSender;

@Service
@RequiredArgsConstructor
@Transactional
public class KdsServiceImpl implements KdsService {

    private final OrderRepository orderRepository;
    private final OrderLineItemRepository lineItemRepository;
    private final OrderEventService orderEventService;
    private final NotificationSender notificationSender;
    private final KdsMapper kdsMapper;

    @Override
    public void updateItemStatus(Long orderId, Long lineItemId, FulfillmentStatus status, String userEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFound(orderId));
        OrderLineItem item = lineItemRepository.findById(lineItemId)
                .orElseThrow(() -> new OrderItemNotFound(orderId, lineItemId));
        if (!item.getOrder().getId().equals(orderId)) {
            throw new InvalidOrderStateException("Item does not belong to this order");
        }
        item.setFulfillmentStatus(status);
        orderEventService.logEvent(order, OrderEventType.ITEM_UPDATED, userEmail,
                "Item " + item.getItemName() + " -> " + status);
        KdsTicket ticket = kdsMapper.toKdsTicket(order);
        notificationSender.sendMessage("KDS_ITEM_UPDATED", ticket, UserRole.KITCHEN.name());
    }

    @Override
    public void markTicketReady(Long orderId, String userEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFound(orderId));
        boolean allReady = order.getLineItems().stream()
                .allMatch(li -> li.getFulfillmentStatus() == FulfillmentStatus.READY);
        if (!allReady) {
            throw new InvalidOrderStateException("Not all items are ready");
        }
        order.setStatus(OrderStatus.READY);
        orderEventService.logEvent(order, OrderEventType.STATUS_CHANGED, userEmail, "Ticket marked READY");
        KdsTicket ticket = kdsMapper.toKdsTicket(order);
        notificationSender.sendMessage("KDS_TICKET_READY", ticket, UserRole.KITCHEN.name());
    }

    @Override
    public void bumpTicket(Long orderId, String userEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFound(orderId));
        orderEventService.logEvent(order, OrderEventType.ITEM_REMOVED, userEmail, "Ticket bumped");
        notificationSender.sendMessage("KDS_TICKET_BUMPED", orderId, UserRole.KITCHEN.name());
    }

    @Override
    public void recallTicket(Long orderId, String userEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFound(orderId));
        orderEventService.logEvent(order, OrderEventType.ITEM_ADDED, userEmail, "Ticket recalled");
        KdsTicket ticket = kdsMapper.toKdsTicket(order);
        notificationSender.sendMessage("KDS_TICKET_RECALLED", ticket, UserRole.KITCHEN.name());
    }
}
