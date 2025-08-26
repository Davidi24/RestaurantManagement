package pos.pos.DTO;

public record MenuItemRequest(
        String name,
        String description,
        Double price,
        Integer sortOrder,
        Long sectionId
) {}
