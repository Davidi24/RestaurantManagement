// RESPONSE — includes variants list
package pos.pos.DTO.Menu.MenuItemDTO;

import pos.pos.DTO.Menu.OptionDTO.OptionGroupResponse;
import pos.pos.DTO.Menu.VariantDTO.ItemVariantResponse;

import java.math.BigDecimal;
import java.util.List;

public record MenuItemResponse(
        Long id,
        String name,
        BigDecimal basePrice,
        boolean available,
        Integer sortOrder,
        List<ItemVariantResponse> itemVariants,
        List<OptionGroupResponse> optionGroup
) {}
