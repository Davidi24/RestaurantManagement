package pos.pos.DTO.Mapper;

import org.springframework.stereotype.Component;

import pos.pos.DTO.Menu.MenuItemCreateRequest;
import pos.pos.DTO.Menu.MenuItemResponse;
import pos.pos.DTO.Menu.MenuItemUpdateRequest;
import pos.pos.Entity.Menu.MenuItem;

@Component
public class MenuItemMapper {

    public MenuItem toMenuItem(MenuItemCreateRequest req) {
        MenuItem item = new MenuItem();
        item.setName(req.name());
        item.setBasePrice(req.basePrice());
        item.setAvailable(req.available());
        item.setSortOrder(req.sortOrder());
        // TODO: map variants & option groups when implemented
        return item;
    }

    public void apply(MenuItemUpdateRequest req, MenuItem item) {
        item.setName(req.name());
        item.setBasePrice(req.basePrice());
        item.setAvailable(req.available());
        item.setSortOrder(req.sortOrder());
        // TODO: update variants & option groups when implemented
    }

    public MenuItemResponse toMenuItemResponse(MenuItem item) {
        return new MenuItemResponse(
                item.getId(),
                item.getName(),
                item.getBasePrice(),
                item.isAvailable(),
                item.getSortOrder()
                // TODO: include variants & option groups when implemented
        );
    }
}
