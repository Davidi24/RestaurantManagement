package pos.pos.DTO.Mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pos.pos.DTO.Menu.MenuDTO.MenuResponse;
import pos.pos.DTO.Menu.MenuDTO.MenuTreeResponse;
import pos.pos.Entity.Menu.Menu;

@Component
@RequiredArgsConstructor
public class MenuMapper {

    private final MenuSectionMapper menuSectionMapper;

    public MenuResponse toResponse(Menu menu) {
        return new MenuResponse(menu.getId(), menu.getName(), menu.getDescription());
    }

    public MenuTreeResponse toTreeResponse(Menu menu) {
        return new MenuTreeResponse(
                menu.getId(),
                menu.getName(),
                menu.getDescription(),
                menuSectionMapper.toMenuSectionResponse(menu.getSections())
        );
    }
}
