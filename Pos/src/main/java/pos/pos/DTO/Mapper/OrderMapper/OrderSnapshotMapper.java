package pos.pos.DTO.Mapper.OrderMapper;

import org.springframework.stereotype.Component;
import pos.pos.Entity.Menu.ItemVariant;
import pos.pos.Entity.Menu.MenuItem;
import pos.pos.Entity.Menu.OptionItem;
import pos.pos.Entity.Order.OrderLineItem;
import pos.pos.Entity.Order.OrderOptionSnapshot;
import pos.pos.Entity.Order.OrderVariantSnapshot;

@Component
public class OrderSnapshotMapper {

    public void applyMenuItem(OrderLineItem lineItem, MenuItem item) {
        lineItem.setMenuItemId(item.getId());
        lineItem.setItemName(item.getName());
        lineItem.setUnitPrice(item.getBasePrice() != null ? item.getBasePrice().doubleValue() : 0.0);
    }

    public OrderVariantSnapshot buildVariantSnapshot(ItemVariant variant, OrderLineItem lineItem) {
        OrderVariantSnapshot vs = OrderVariantSnapshot.builder()
                .variantId(variant.getId())
                .variantPublicId(variant.getPublicId())
                .variantName(variant.getName())
                .priceOverride(variant.getPriceOverride() != null ? variant.getPriceOverride().doubleValue() : null)
                .build();
        vs.setLineItem(lineItem);
        return vs;
    }

    public OrderOptionSnapshot buildOptionSnapshot(OptionItem opt, OrderLineItem lineItem) {
        OrderOptionSnapshot os = OrderOptionSnapshot.builder()
                .optionId(opt.getId())
                .optionPublicId(opt.getPublicId())
                .optionName(opt.getName())
                .priceDelta(opt.getPriceDelta() != null ? opt.getPriceDelta().doubleValue() : 0.0)
                .build();
        os.setLineItem(lineItem);
        return os;
    }
}
