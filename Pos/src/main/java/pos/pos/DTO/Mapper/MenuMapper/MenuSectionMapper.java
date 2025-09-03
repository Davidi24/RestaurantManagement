package pos.pos.DTO.Mapper.MenuMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pos.pos.DTO.Menu.MenuItemDTO.MenuItemResponse;
import pos.pos.DTO.Menu.MenuSectionDTO.MenuSectionCreateRequest;
import pos.pos.DTO.Menu.MenuSectionDTO.MenuSectionResponse;
import pos.pos.DTO.Menu.MenuSectionDTO.MenuSectionUpdateRequest;
import pos.pos.Entity.Menu.MenuItem;
import pos.pos.Entity.Menu.MenuSection;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class MenuSectionMapper {

    private final MenuItemMapper menuItemMapper;

    public MenuSection toMenuSection(MenuSectionCreateRequest req) {
        MenuSection s = new MenuSection();
        s.setName(req.name());
        s.setSortOrder(req.sortOrder());
        return s;
    }

    public void apply(MenuSectionUpdateRequest req, MenuSection s) {
        s.setName(req.name());
        s.setSortOrder(req.sortOrder());
    }

    public MenuSectionResponse toMenuSectionResponse(MenuSection s, int position1Based) {
        List<MenuItemResponse> items = s.getItems().stream()
                .sorted(Comparator
                        .comparing(MenuItem::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(MenuItem::getId))
                .map(menuItemMapper::toMenuItemResponse)
                .toList();

        return new MenuSectionResponse(
                s.getId(),
                s.getName(),
                position1Based,
                s.getOrderKey(),
                s.getPublicId(),
                items
        );
    }


    public List<MenuSectionResponse> toMenuSectionResponse(List<MenuSection> sections) {
        List<MenuSection> sorted = sections.stream()
                .sorted(Comparator
                        .comparing(MenuSection::getOrderKey, Comparator.nullsLast(BigDecimal::compareTo))
                        .thenComparing(MenuSection::getId))
                .toList();

        return IntStream.range(0, sorted.size())
                .mapToObj(i -> toMenuSectionResponse(sorted.get(i), i + 1))
                .toList();
    }
}
