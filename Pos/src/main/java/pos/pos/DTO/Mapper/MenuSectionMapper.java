// src/main/java/pos/pos/mapper/MenuSectionMapper.java
package pos.pos.DTO.Mapper;

import org.springframework.stereotype.Component;
import pos.pos.DTO.MenuSectionCreateRequest;
import pos.pos.DTO.MenuSectionResponse;
import pos.pos.DTO.MenuSectionUpdateRequest;
import pos.pos.Entity.Menu.MenuItem;
import pos.pos.Entity.Menu.MenuSection;


import java.util.Comparator;
import java.util.List;

@Component
public class MenuSectionMapper {

    public MenuSection toMenuSection(MenuSectionCreateRequest req) {
        MenuSection s = new MenuSection();
        s.setName(req.name());
        if (req.sortOrder() != null) s.setSortOrder(req.sortOrder());
        return s;
    }

    public void apply(MenuSectionUpdateRequest req, MenuSection s) {
        s.setName(req.name());
        if (req.sortOrder() != null) s.setSortOrder(req.sortOrder());
    }

    public MenuSectionResponse toMenuSectionResponse(MenuSection s) {
        List<MenuSectionResponse.MenuItemSummary> items = s.getItems().stream()
                .sorted(Comparator.comparing(MenuItem::getSortOrder).thenComparing(MenuItem::getId))
                .map(i -> new MenuSectionResponse.MenuItemSummary(i.getId(), i.getName(), i.getSortOrder()))
                .toList();

        return new MenuSectionResponse(
                s.getId(),
                s.getName(),
                s.getSortOrder(),
                items
        );
    }
}
