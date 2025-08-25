package pos.pos.DTO;

import java.math.BigDecimal;
import java.util.List;

public record MenuTreeResponse(
        Long id,
        String name,
        String description,
        List<Section> sections
) {
    public record Section(Long id, String name, Integer sortOrder, List<Item> items) {}
    public record Item(Long id, String name, BigDecimal basePrice, boolean available, Integer sortOrder,
                       List<Variant> variants, List<Group> groups) {}
    public record Variant(Long id, String name, BigDecimal priceOverride, boolean isDefault, Integer sortOrder) {}
    public record Group(Long id, String name, String type, boolean required,
                        Integer minSelections, Integer maxSelections, Integer sortOrder,
                        List<Option> options) {}
    public record Option(Long id, String name, BigDecimal priceDelta, Integer sortOrder) {}
}
