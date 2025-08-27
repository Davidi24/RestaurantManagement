package pos.pos.DTO.Menu.MenuItemDTO;

public record MenuItemRequest(
        String name,
        String description,
        Double price,
        Integer sortOrder,
        Long sectionId
) {}
