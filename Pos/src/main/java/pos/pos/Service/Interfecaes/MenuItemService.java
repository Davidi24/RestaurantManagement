package pos.pos.Service.Interfecaes;

import pos.pos.DTO.*;
import pos.pos.DTO.Menu.MenuItemCreateRequest;
import pos.pos.DTO.Menu.MenuItemResponse;
import pos.pos.DTO.Menu.MenuItemUpdateRequest;

import java.util.List;

public interface MenuItemService {

    List<MenuItemResponse> listItems(Long menuId, Long sectionId);

    MenuItemResponse createItem(Long menuId, Long sectionId, MenuItemCreateRequest req);

    MenuItemResponse updateItem(Long menuId, Long sectionId, Long itemId, MenuItemUpdateRequest req);

    void deleteItem(Long menuId, Long sectionId, Long itemId);

    MenuItemResponse moveItem(Long menuId, Long sectionId, Long itemId, int newSortOrder);
}
