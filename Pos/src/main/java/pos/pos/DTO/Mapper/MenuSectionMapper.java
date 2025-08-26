package pos.pos.DTO.Mapper;

import org.springframework.stereotype.Component;
import pos.pos.DTO.Menu.MenuSectionCreateRequest;
import pos.pos.DTO.Menu.MenuSectionResponse;
import pos.pos.DTO.Menu.MenuSectionUpdateRequest;
import pos.pos.Entity.Menu.MenuItem;
import pos.pos.Entity.Menu.MenuSection;

import java.util.Comparator;
import java.util.List;

@Component
public class MenuSectionMapper {

    public MenuSection toMenuSection(MenuSectionCreateRequest req) {
        MenuSection s = new MenuSection();
        s.setName(req.name());
        s.setSortOrder(null);
        return s;
    }

    public void apply(MenuSectionUpdateRequest req, MenuSection s) {
        s.setName(req.name());
    }

    public MenuSectionResponse toMenuSectionResponse(MenuSection s, int position1Based) {
        List<MenuSectionResponse.MenuItemSummary> items = s.getItems().stream()
                .sorted(Comparator
                        .comparing(MenuItem::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(MenuItem::getId))
                .map(i -> new MenuSectionResponse.MenuItemSummary(i.getId(), i.getName(), i.getSortOrder()))
                .toList();

        return new MenuSectionResponse(
                s.getId(),
                s.getName(),
                position1Based,
                s.getOrderKey(),
                items
        );
    }
}
