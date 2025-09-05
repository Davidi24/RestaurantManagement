package pos.pos.Service.Interfecaes;

import pos.pos.DTO.Menu.MenuItemDTO.MenuItemCreateRequest;
import pos.pos.DTO.Menu.MenuItemDTO.MenuItemResponse;
import pos.pos.DTO.Menu.MenuItemDTO.MenuItemUpdateRequest;

import java.util.List;

public interface MenuItemService {

    List<MenuItemResponse> listItems(Long menuId, Long sectionId);

    MenuItemResponse createItem(Long menuId, Long sectionId, MenuItemCreateRequest req);

    MenuItemResponse updateItem(Long menuId, Long sectionId, Long itemId, MenuItemUpdateRequest req);

    void deleteItem(Long menuId, Long sectionId, Long itemId);

    MenuItemResponse moveOne(Long menuId, Long sectionId, Long itemId, int direction);

    MenuItemResponse getItems(Long menuId, Long sectionId, Long itemId);
}
