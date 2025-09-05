package pos.pos.DTO.Mapper.MenuMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pos.pos.DTO.Menu.MenuItemDTO.MenuItemResponse;
import pos.pos.DTO.Menu.MenuSectionDTO.MenuSectionCreateRequest;
import pos.pos.DTO.Menu.MenuSectionDTO.MenuSectionResponse;
import pos.pos.DTO.Menu.MenuSectionDTO.MenuSectionUpdateRequest;
import pos.pos.Entity.Menu.MenuItem;
import pos.pos.Entity.Menu.MenuSection;
import java.util.List;
import java.util.stream.IntStream;

import static pos.pos.Util.MenuComparators.ITEM_ORDER;
import static pos.pos.Util.MenuComparators.SECTION_ORDER;

@Component
@RequiredArgsConstructor
public class MenuSectionMapper {

    private final MenuItemMapper menuItemMapper;

    public MenuSection toMenuSection(MenuSectionCreateRequest req) {
        return MenuSection.builder()
                .name(req.name())
                .sortOrder(req.sortOrder())
                .build();
    }

    public void update(MenuSectionUpdateRequest req, MenuSection s) {
        if (req.name() != null) s.setName(req.name());
        if (req.sortOrder() != null) s.setSortOrder(req.sortOrder());
    }

    public MenuSectionResponse toMenuSectionResponse(MenuSection s, int position1Based) {
        List<MenuItemResponse> items = (s.getItems() == null ? List.<MenuItem>of() : s.getItems())
                .stream()
                .sorted(ITEM_ORDER)
                .map(menuItemMapper::toMenuItemResponse)
                .toList();

        return new MenuSectionResponse(
                s.getId(),
                s.getName(),
                position1Based,
                s.getOrderKey(),
                s.getPublicId(),
                List.copyOf(items)
        );
    }

    public List<MenuSectionResponse> toMenuSectionResponse(List<MenuSection> sections) {
        final var sorted = (sections == null ? List.<MenuSection>of() : sections)
                .stream()
                .sorted(SECTION_ORDER)
                .toList();

        final int size = sorted.size();
        return IntStream.range(0, size)
                .mapToObj(i -> toMenuSectionResponse(sorted.get(i), i + 1))
                .toList();
    }
}
