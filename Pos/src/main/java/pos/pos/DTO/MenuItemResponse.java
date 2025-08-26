package pos.pos.DTO;

public record MenuItemResponse(
        Long id,
        String name,
        String description,
        Double price,
        Integer sortOrder,
        Long sectionId
) {}
