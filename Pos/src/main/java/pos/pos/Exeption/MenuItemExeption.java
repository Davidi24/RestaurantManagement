package pos.pos.Exeption;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class MenuItemExeption extends RuntimeException {
    public MenuItemExeption(Long menuId, Long sectionId, Long menuItemID) {
        super("Menu Item with id"+menuItemID +" in menu with id"+ menuId +"in section "+ sectionId + " not found");
    }
}
