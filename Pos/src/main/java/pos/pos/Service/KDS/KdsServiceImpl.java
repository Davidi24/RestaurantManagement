package pos.pos.Service.KDS;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pos.pos.DTO.Mapper.KDS.KdsMapper;
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
    public void updateItemStatus(Long orderId, Long lineItemId, FulfillmentStatus to, String userEmail) {
        Order order = loadMutableOrderLocked(orderId);

        OrderLineItem item = lineItemRepository.findById(lineItemId)
                .orElseThrow(() -> new OrderItemNotFound(orderId, lineItemId));
        if (!item.getOrder().getId().equals(orderId)) {
            throw new InvalidOrderStateException("Item does not belong to this order");
        }

        FulfillmentStatus from = item.getFulfillmentStatus();
        if (!FulfillmentStatus.isValidTransition(from, to)) {
            throw new InvalidOrderStateException("Illegal transition " + from + " -> " + to);
        }

        item.setFulfillmentStatus(to);
        lineItemRepository.save(item);

        orderEventService.logEvent(order, OrderEventType.ITEM_UPDATED, userEmail,
                "Item " + item.getItemName() + " -> " + to);

        boolean allReady = order.getLineItems().stream().allMatch(li ->
                li.getFulfillmentStatus() == FulfillmentStatus.READY
                        || li.getFulfillmentStatus() == FulfillmentStatus.SERVED
                        || li.getFulfillmentStatus() == FulfillmentStatus.VOIDED);

        if (allReady && order.getStatus() == OrderStatus.SENT_TO_KITCHEN) {
            order.setStatus(OrderStatus.READY);
            orderRepository.save(order);
            orderEventService.logEvent(order, OrderEventType.STATUS_CHANGED, userEmail, "Ticket auto-marked READY");
            sendKds("KDS_TICKET_READY", kdsMapper.toKdsTicket(order));
        } else {
            sendKds("KDS_ITEM_UPDATED", kdsMapper.toKdsTicket(order));
        }
    }

    @Override
    public void markTicketReady(Long orderId, String userEmail) {
        Order order = loadMutableOrderLocked(orderId);

        boolean allReady = order.getLineItems().stream().allMatch(li ->
                li.getFulfillmentStatus() == FulfillmentStatus.READY
                        || li.getFulfillmentStatus() == FulfillmentStatus.SERVED
                        || li.getFulfillmentStatus() == FulfillmentStatus.VOIDED);

        if (!allReady) throw new InvalidOrderStateException("Not all items are READY/SERVED/VOIDED");

        order.setStatus(OrderStatus.READY);
        orderRepository.save(order);

        orderEventService.logEvent(order, OrderEventType.STATUS_CHANGED, userEmail, "Ticket marked READY");
        sendKds("KDS_TICKET_READY", kdsMapper.toKdsTicket(order));
    }

    @Override
    public void bumpTicket(Long orderId, String userEmail) {
        Order order = loadForUpdate(orderId);
        orderEventService.logEvent(order, OrderEventType.ITEM_REMOVED, userEmail, "Ticket bumped");
        sendKds("KDS_TICKET_BUMPED", orderId);
    }

    @Override
    public void recallTicket(Long orderId, String userEmail) {
        Order order = loadForUpdate(orderId);
        orderEventService.logEvent(order, OrderEventType.ITEM_ADDED, userEmail, "Ticket recalled");
        sendKds("KDS_TICKET_RECALLED", kdsMapper.toKdsTicket(order));
    }


    private @NotNull Order loadMutableOrderLocked(Long orderId) {
        Order o = loadForUpdate(orderId);
        if (o.getStatus() == OrderStatus.CLOSED || o.getStatus() == OrderStatus.VOIDED) {
            throw new InvalidOrderStateException("Order not editable");
        }
        return o;
    }

    private @NotNull Order loadForUpdate(Long orderId) {
        return orderRepository.findForUpdate(orderId)
                .orElseThrow(() -> new OrderNotFound(orderId));
    }

    private void sendKds(String topic, Object payload) {
        notificationSender.sendMessage(topic, payload, UserRole.KITCHEN.name());
    }
}
