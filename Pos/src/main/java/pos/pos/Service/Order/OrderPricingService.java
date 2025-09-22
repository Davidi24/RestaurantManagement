package pos.pos.Service.Order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pos.pos.Entity.Order.OrderLineItem;

@Service
@RequiredArgsConstructor
public class OrderPricingService {

    //take the price of item, if it has any variant then make that the price
    //then take the price of option. now add these two and multiply them by quantity
    //see if it has any discount. Add full price and the price after discount to orderLineItem
    public void priceLineItem(OrderLineItem li) {
        double base = li.getUnitPrice() != null ? li.getUnitPrice() : 0.0;

        if (li.getVariantSnapshot() != null && li.getVariantSnapshot().getPriceOverride() != null) {
            base = li.getVariantSnapshot().getPriceOverride();
        }

        li.setUnitPrice(base);

        double options = 0.0;
        if (li.getOptionSnapshots() != null) {
            for (var os : li.getOptionSnapshots()) {
                if (os.getPriceDelta() != null) {
                    options += os.getPriceDelta();
                }
            }
        }

        int qty = li.getQuantity() != null ? li.getQuantity() : 0;
        double subtotal = (base + options) * qty;

        if (li.getLineDiscount() == null) {
            li.setLineDiscount(0.0);
        }

        li.setLineSubtotal(subtotal);
        li.setLineGrandTotal(subtotal - li.getLineDiscount());
    }
}
