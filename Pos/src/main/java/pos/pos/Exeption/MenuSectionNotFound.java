package pos.pos.Exeption;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class MenuSectionNotFound extends RuntimeException {
    public MenuSectionNotFound(Long menuId, Long sectionId ) {
        super("Menu Section with "+ sectionId + " not found in Menu with id " + menuId);
    }
}
