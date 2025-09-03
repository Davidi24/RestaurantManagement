package pos.pos.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pos.pos.DTO.Order.OrderLineItemDTO.OrderLineItemCreateDTO;
import pos.pos.Entity.Menu.ItemVariant;
import pos.pos.Entity.Menu.MenuItem;
import pos.pos.Entity.Menu.OptionGroup;
import pos.pos.Entity.Menu.OptionItem;
import pos.pos.Entity.Order.OrderLineItem;
import pos.pos.Entity.Order.OrderOptionSnapshot;
import pos.pos.Entity.Order.OrderVariantSnapshot;
import pos.pos.Repository.ItemVariantRepository;
import pos.pos.Repository.MenuItemRepository;
import pos.pos.Repository.OptionItemRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderSnapshotBuilder {

    private final MenuItemRepository menuItemRepository;
    private final ItemVariantRepository itemVariantRepository;
    private final OptionItemRepository optionItemRepository;

    public void enrichFromCatalog(OrderLineItem lineItem, OrderLineItemCreateDTO dto) {
        if (dto.getMenuItemPublicId() == null) {
            throw new IllegalArgumentException("menuItemPublicId is required");
        }

        MenuItem item = menuItemRepository.findByPublicId(dto.getMenuItemPublicId())
                .orElseThrow(() -> new IllegalArgumentException("MenuItem not found for publicId=" + dto.getMenuItemPublicId()));

        lineItem.setMenuItemId(item.getId());
        lineItem.setItemName(item.getName());
        lineItem.setUnitPrice(item.getBasePrice() != null ? item.getBasePrice().doubleValue() : 0.0);

        UUID variantPid = dto.getVariantPublicId();
        if (variantPid != null) {
            ItemVariant variant = itemVariantRepository.findByPublicId(variantPid)
                    .orElseThrow(() -> new IllegalArgumentException("Variant not found for publicId=" + variantPid));

            if (variant.getItem() == null || !variant.getItem().getId().equals(item.getId())) {
                throw new IllegalArgumentException("Variant does not belong to the provided MenuItem");
            }

            OrderVariantSnapshot vs = OrderVariantSnapshot.builder()
                    .variantId(variant.getId())
                    .variantPublicId(variant.getPublicId())
                    .variantName(variant.getName())
                    .priceOverride(variant.getPriceOverride() != null ? variant.getPriceOverride().doubleValue() : null)
                    .build();

            vs.setLineItem(lineItem);
            lineItem.setVariantSnapshot(vs);
        }

        if (dto.getOptionPublicIds() != null && !dto.getOptionPublicIds().isEmpty()) {
            for (UUID optionPid : dto.getOptionPublicIds()) {
                OptionItem opt = optionItemRepository.findByPublicId(optionPid)
                        .orElseThrow(() -> new IllegalArgumentException("OptionItem not found for publicId=" + optionPid));

                OptionGroup group = opt.getGroup();
                if (group == null || group.getItem() == null || !group.getItem().getId().equals(item.getId())) {
                    throw new IllegalArgumentException("OptionItem does not belong to the provided MenuItem");
                }

                OrderOptionSnapshot os = OrderOptionSnapshot.builder()
                        .optionId(opt.getId())
                        .optionPublicId(opt.getPublicId())
                        .optionName(opt.getName())
                        .priceDelta(opt.getPriceDelta() != null ? opt.getPriceDelta().doubleValue() : 0.0)
                        .build();

                os.setLineItem(lineItem);
                lineItem.getOptionSnapshots().add(os);
            }
        }
    }
}
