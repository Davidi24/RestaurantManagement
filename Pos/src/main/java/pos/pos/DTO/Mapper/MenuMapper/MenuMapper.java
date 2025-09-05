package pos.pos.DTO.Mapper.MenuMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pos.pos.DTO.Menu.MenuDTO.MenuRequest;
import pos.pos.DTO.Menu.MenuDTO.MenuResponse;
import pos.pos.DTO.Menu.MenuDTO.MenuTreeResponse;
import pos.pos.Entity.Menu.Menu;

@Component
@RequiredArgsConstructor
public class MenuMapper {

    private final MenuSectionMapper menuSectionMapper;

    public Menu toMenu(MenuRequest menuRequest) {
        return  Menu.builder()
                .name(menuRequest.name())
                .description(menuRequest.description())
                .build();
    }

    public MenuResponse toMenuResponse(Menu menu) {
        return new MenuResponse(
                menu.getId(),
                menu.getName(),
                menu.getDescription(),
                menu.getPublicId()
        );
    }

    public MenuTreeResponse toMenuTreeResponse(Menu menu) {
        return new MenuTreeResponse(
                menu.getId(),
                menu.getName(),
                menu.getDescription(),
                menu.getPublicId(),
                menuSectionMapper.toMenuSectionResponse(menu.getSections())
        );
    }
}
