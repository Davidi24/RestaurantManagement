package pos.pos.Service.Order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pos.pos.DTO.Mapper.OrderMapper.OrderSnapshotMapper;
import pos.pos.DTO.Order.OrderLineItemDTO.OrderLineItemCreateDTO;
import pos.pos.Exeption.*;
import pos.pos.Entity.Menu.ItemVariant;
import pos.pos.Entity.Menu.MenuItem;
import pos.pos.Entity.Menu.OptionGroup;
import pos.pos.Entity.Menu.OptionItem;
import pos.pos.Entity.Order.OrderLineItem;
import pos.pos.Entity.Order.OrderOptionSnapshot;
import pos.pos.Entity.Order.OrderVariantSnapshot;
import pos.pos.Repository.Menu.ItemVariantRepository;
import pos.pos.Repository.Menu.MenuItemRepository;
import pos.pos.Repository.Order.OptionItemRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderSnapshotBuilder {

    private final MenuItemRepository menuItemRepository;
    private final ItemVariantRepository itemVariantRepository;
    private final OptionItemRepository optionItemRepository;
    private final OrderSnapshotMapper mapper;

    public void enrichFromCatalog(OrderLineItem lineItem, OrderLineItemCreateDTO dto) {
        if (dto.getMenuItemPublicId() == null) {
            throw new MissingFieldException("menuItemPublicId");
        }

        MenuItem item = menuItemRepository.findByPublicId(dto.getMenuItemPublicId())
                .orElseThrow(() -> new MenuItemNotFoundException(dto.getMenuItemPublicId()));

        mapper.applyMenuItem(lineItem, item);

        UUID variantPid = dto.getVariantPublicId();
        if (variantPid != null) {
            ItemVariant variant = itemVariantRepository.findByPublicId(variantPid)
                    .orElseThrow(() -> new ItemVariantNotFoundException(variantPid));

            if (variant.getItem() == null || !variant.getItem().getId().equals(item.getId())) {
                throw new VariantMenuMismatchException();
            }

            OrderVariantSnapshot vs = mapper.buildVariantSnapshot(variant, lineItem);
            lineItem.setVariantSnapshot(vs);
        }

        if (dto.getOptionPublicIds() != null && !dto.getOptionPublicIds().isEmpty()) {
            for (UUID optionPid : dto.getOptionPublicIds()) {
                OptionItem opt = optionItemRepository.findByPublicId(optionPid)
                        .orElseThrow(() -> new OptionItemNotFoundExceptionUDD(optionPid));

                OptionGroup group = opt.getGroup();
                if (group == null || group.getItem() == null || !group.getItem().getId().equals(item.getId())) {
                    throw new OptionMenuMismatchException();
                }

                OrderOptionSnapshot os = mapper.buildOptionSnapshot(opt, lineItem);
                lineItem.getOptionSnapshots().add(os);
            }
        }
    }
}
