package pos.pos.DTO.Mapper.KDS;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pos.pos.Entity.Order.Order;
import pos.pos.Entity.Order.OrderLineItem;
import pos.pos.Entity.KDS.KdsTicket;
import pos.pos.Entity.KDS.KdsTicketItem;

@Component
@RequiredArgsConstructor
public class KdsMapper {

    public KdsTicket toKdsTicket(Order order) {
        return KdsTicket.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .tableId(order.getTableId())
                .notes(order.getNotes())
                .status(order.getStatus())
                .openedAt(order.getOpenedAt())
                .items(
                        order.getLineItems()
                             .stream()
                             .map(this::toKdsTicketItem)
                             .toList()
                )
                .build();
    }

    private KdsTicketItem toKdsTicketItem(OrderLineItem li) {
        return KdsTicketItem.builder()
                .lineItemId(li.getId())
                .itemName(li.getItemName())
                .quantity(li.getQuantity())
                .fulfillmentStatus(li.getFulfillmentStatus())
                .firedAt(null)   // fill when you track fired time
                .readyAt(null)   // fill when you track ready time
                .build();
    }
}
