package pos.pos.DTO.Menu;

public record MenuItemRequest(
        String name,
        String description,
        Double price,
        Integer sortOrder,
        Long sectionId
) {}
