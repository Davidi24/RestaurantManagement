package pos.pos.Menu.Services_Unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pos.pos.DTO.Mapper.MenuMapper.OptionItemMapper;
import pos.pos.DTO.Menu.OptionDTO.OptionItemCreateRequest;
import pos.pos.DTO.Menu.OptionDTO.OptionItemResponse;
import pos.pos.DTO.Menu.OptionDTO.OptionItemUpdateRequest;
import pos.pos.Entity.Menu.Menu;
import pos.pos.Entity.Menu.MenuItem;
import pos.pos.Entity.Menu.MenuSection;
import pos.pos.Entity.Menu.OptionGroup;
import pos.pos.Entity.Menu.OptionItem;
import pos.pos.Exeption.AlreadyExistsException;
import pos.pos.Exeption.MenuItemException;
import pos.pos.Exeption.MenuSectionNotFound;
import pos.pos.Exeption.OptionGroupNotFoundException;
import pos.pos.Exeption.OptionItemNotFoundException;
import pos.pos.Repository.Menu.MenuItemRepository;
import pos.pos.Repository.Menu.MenuSectionRepository;
import pos.pos.Repository.Order.OptionGroupRepository;
import pos.pos.Repository.Order.OptionItemRepository;
import pos.pos.Service.Menu.OptionItemServiceImpl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OptionItemServiceImplTest {

    @Mock OptionItemRepository optionRepo;
    @Mock OptionGroupRepository groupRepo;
    @Mock MenuItemRepository itemRepo;
    @Mock MenuSectionRepository sectionRepo;
    @Mock OptionItemMapper mapper;

    @InjectMocks
    OptionItemServiceImpl service;

    private OptionGroup group(Long menuId, Long sectionId, Long itemId, Long groupId) {
        Menu m = new Menu(); m.setId(menuId);
        MenuSection s = new MenuSection(); s.setId(sectionId); s.setMenu(m);
        MenuItem it = new MenuItem(); it.setId(itemId); it.setSection(s);
        OptionGroup g = new OptionGroup(); g.setId(groupId); g.setItem(it);
        return g;
    }

    @Test
    void list_ok() {
        Long menuId=1L, sectionId=2L, itemId=3L, groupId=4L;
        OptionGroup g = group(menuId, sectionId, itemId, groupId);
        when(sectionRepo.findByIdAndMenu_Id(sectionId, menuId)).thenReturn(Optional.of(g.getItem().getSection()));
        when(itemRepo.findByIdAndSection_Id(itemId, sectionId)).thenReturn(Optional.of(g.getItem()));
        when(groupRepo.findByIdAndItem_Id(groupId, itemId)).thenReturn(Optional.of(g));
        OptionItem oi1 = new OptionItem(), oi2 = new OptionItem();
        OptionItemResponse r1 = new OptionItemResponse(10L,"Ketchup", BigDecimal.ZERO,1,groupId, UUID.randomUUID());
        OptionItemResponse r2 = new OptionItemResponse(11L,"BBQ", BigDecimal.ZERO,2,groupId, UUID.randomUUID());
        when(optionRepo.findByGroup_IdOrderBySortOrderAscIdAsc(groupId)).thenReturn(List.of(oi1, oi2));
        when(mapper.toResponse(oi1)).thenReturn(r1);
        when(mapper.toResponse(oi2)).thenReturn(r2);

        var out = service.list(menuId, sectionId, itemId, groupId);

        assertEquals(List.of(r1, r2), out);
    }

    @Test
    void list_groupMissing() {
        when(sectionRepo.findByIdAndMenu_Id(2L,1L)).thenReturn(Optional.empty());
        assertThrows(MenuSectionNotFound.class, () -> service.list(1L,2L,3L,4L));
    }

    @Test
    void create_ok() {
        Long menuId=1L, sectionId=2L, itemId=3L, groupId=4L;
        OptionGroup g = group(menuId, sectionId, itemId, groupId);
        when(sectionRepo.findByIdAndMenu_Id(sectionId, menuId)).thenReturn(Optional.of(g.getItem().getSection()));
        when(itemRepo.findByIdAndSection_Id(itemId, sectionId)).thenReturn(Optional.of(g.getItem()));
        when(groupRepo.findByIdAndItem_Id(groupId, itemId)).thenReturn(Optional.of(g));
        OptionItemCreateRequest body = new OptionItemCreateRequest("Ketchup", BigDecimal.ZERO, null);
        when(optionRepo.existsByGroup_IdAndNameIgnoreCase(groupId, "Ketchup")).thenReturn(false);
        when(optionRepo.countByGroup_Id(groupId)).thenReturn(0L);
        OptionItem entity = new OptionItem(); entity.setGroup(g); entity.setSortOrder(1);
        when(mapper.toEntity(eq(body), eq(g))).thenReturn(entity);
        OptionItem saved = entity; saved.setId(10L);
        OptionItemResponse resp = new OptionItemResponse(10L,"Ketchup", BigDecimal.ZERO,1,groupId, UUID.randomUUID());
        when(optionRepo.save(entity)).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(resp);

        var out = service.create(menuId, sectionId, itemId, groupId, body);

        assertEquals(resp, out);
        verify(optionRepo).shiftRightFrom(groupId, 1);
    }

    @Test
    void create_duplicate() {
        Long menuId=1L, sectionId=2L, itemId=3L, groupId=4L;
        OptionGroup g = group(menuId, sectionId, itemId, groupId);
        when(sectionRepo.findByIdAndMenu_Id(sectionId, menuId)).thenReturn(Optional.of(g.getItem().getSection()));
        when(itemRepo.findByIdAndSection_Id(itemId, sectionId)).thenReturn(Optional.of(g.getItem()));
        when(groupRepo.findByIdAndItem_Id(groupId, itemId)).thenReturn(Optional.of(g));
        when(optionRepo.existsByGroup_IdAndNameIgnoreCase(groupId, "Ketchup")).thenReturn(true);

        assertThrows(AlreadyExistsException.class,
                () -> service.create(menuId, sectionId, itemId, groupId, new OptionItemCreateRequest("Ketchup", BigDecimal.ZERO, 1)));
    }

    @Test
    void get_ok() {
        Long menuId=1L, sectionId=2L, itemId=3L, groupId=4L, optionId=5L;
        OptionGroup g = group(menuId, sectionId, itemId, groupId);
        when(sectionRepo.findByIdAndMenu_Id(sectionId, menuId)).thenReturn(Optional.of(g.getItem().getSection()));
        when(itemRepo.findByIdAndSection_Id(itemId, sectionId)).thenReturn(Optional.of(g.getItem()));
        when(groupRepo.findByIdAndItem_Id(groupId, itemId)).thenReturn(Optional.of(g));
        OptionItem oi = new OptionItem(); oi.setId(optionId); oi.setGroup(g);
        OptionItemResponse resp = new OptionItemResponse(optionId,"Ketchup", BigDecimal.ZERO,1,groupId, UUID.randomUUID());
        when(optionRepo.findByIdAndGroup_Id(optionId, groupId)).thenReturn(Optional.of(oi));
        when(mapper.toResponse(oi)).thenReturn(resp);

        var out = service.get(menuId, sectionId, itemId, groupId, optionId);

        assertEquals(resp, out);
    }

    @Test
    void get_notFound() {
        Long menuId=1L, sectionId=2L, itemId=3L, groupId=4L, optionId=5L;
        OptionGroup g = group(menuId, sectionId, itemId, groupId);
        when(sectionRepo.findByIdAndMenu_Id(sectionId, menuId)).thenReturn(Optional.of(g.getItem().getSection()));
        when(itemRepo.findByIdAndSection_Id(itemId, sectionId)).thenReturn(Optional.of(g.getItem()));
        when(groupRepo.findByIdAndItem_Id(groupId, itemId)).thenReturn(Optional.of(g));
        when(optionRepo.findByIdAndGroup_Id(optionId, groupId)).thenReturn(Optional.empty());
        assertThrows(OptionItemNotFoundException.class, () -> service.get(menuId, sectionId, itemId, groupId, optionId));
    }

    @Test
    void patch_ok() {
        Long menuId=1L, sectionId=2L, itemId=3L, groupId=4L, optionId=5L;
        OptionGroup g = group(menuId, sectionId, itemId, groupId);
        when(sectionRepo.findByIdAndMenu_Id(sectionId, menuId)).thenReturn(Optional.of(g.getItem().getSection()));
        when(itemRepo.findByIdAndSection_Id(itemId, sectionId)).thenReturn(Optional.of(g.getItem()));
        when(groupRepo.findByIdAndItem_Id(groupId, itemId)).thenReturn(Optional.of(g));
        OptionItem oi = new OptionItem(); oi.setId(optionId); oi.setGroup(g); oi.setName("Old");
        when(optionRepo.findByIdAndGroup_Id(optionId, groupId)).thenReturn(Optional.of(oi));
        when(optionRepo.existsByGroup_IdAndNameIgnoreCase(groupId, "New")).thenReturn(false);
        OptionItemUpdateRequest body = new OptionItemUpdateRequest("New", new BigDecimal("0.50"), 7);
        doAnswer(a -> { oi.setName("New"); oi.setPriceDelta(new BigDecimal("0.50")); oi.setSortOrder(7); return null; }).when(mapper).apply(body, oi);
        when(optionRepo.save(oi)).thenReturn(oi);
        OptionItemResponse resp = new OptionItemResponse(optionId,"New", new BigDecimal("0.50"),7,groupId, UUID.randomUUID());
        when(mapper.toResponse(oi)).thenReturn(resp);

        var out = service.patch(menuId, sectionId, itemId, groupId, optionId, body);

        assertEquals(resp, out);
    }

    @Test
    void patch_nameConflict() {
        Long menuId=1L, sectionId=2L, itemId=3L, groupId=4L, optionId=5L;
        OptionGroup g = group(menuId, sectionId, itemId, groupId);
        when(sectionRepo.findByIdAndMenu_Id(sectionId, menuId)).thenReturn(Optional.of(g.getItem().getSection()));
        when(itemRepo.findByIdAndSection_Id(itemId, sectionId)).thenReturn(Optional.of(g.getItem()));
        when(groupRepo.findByIdAndItem_Id(groupId, itemId)).thenReturn(Optional.of(g));
        OptionItem oi = new OptionItem(); oi.setId(optionId); oi.setGroup(g); oi.setName("Old");
        when(optionRepo.findByIdAndGroup_Id(optionId, groupId)).thenReturn(Optional.of(oi));
        when(optionRepo.existsByGroup_IdAndNameIgnoreCase(groupId, "New")).thenReturn(true);

        assertThrows(AlreadyExistsException.class,
                () -> service.patch(menuId, sectionId, itemId, groupId, optionId, new OptionItemUpdateRequest("New", null, null)));
    }

    @Test
    void delete_ok() {
        Long menuId=1L, sectionId=2L, itemId=3L, groupId=4L, optionId=5L;
        OptionGroup g = group(menuId, sectionId, itemId, groupId);
        when(sectionRepo.findByIdAndMenu_Id(sectionId, menuId)).thenReturn(Optional.of(g.getItem().getSection()));
        when(itemRepo.findByIdAndSection_Id(itemId, sectionId)).thenReturn(Optional.of(g.getItem()));
        when(groupRepo.findByIdAndItem_Id(groupId, itemId)).thenReturn(Optional.of(g));
        OptionItem oi = new OptionItem(); oi.setId(optionId); oi.setGroup(g); oi.setSortOrder(3);
        when(optionRepo.findByIdAndGroup_Id(optionId, groupId)).thenReturn(Optional.of(oi));

        service.delete(menuId, sectionId, itemId, groupId, optionId);

        verify(optionRepo).delete(oi);
        verify(optionRepo).shiftLeftAfter(groupId, 3);
    }

    @Test
    void moveOne_ok_down() {
        Long menuId=1L, sectionId=2L, itemId=3L, groupId=4L, optionId=5L;
        OptionGroup g = group(menuId, sectionId, itemId, groupId);
        when(sectionRepo.findByIdAndMenu_Id(sectionId, menuId)).thenReturn(Optional.of(g.getItem().getSection()));
        when(itemRepo.findByIdAndSection_Id(itemId, sectionId)).thenReturn(Optional.of(g.getItem()));
        when(groupRepo.findByIdAndItem_Id(groupId, itemId)).thenReturn(Optional.of(g));
        OptionItem oi = new OptionItem(); oi.setId(optionId); oi.setGroup(g); oi.setSortOrder(1);
        when(optionRepo.findByIdAndGroup_Id(optionId, groupId)).thenReturn(Optional.of(oi));
        when(optionRepo.countByGroup_Id(groupId)).thenReturn(3L);
        OptionItem neighbor = new OptionItem(); neighbor.setId(99L); neighbor.setGroup(g); neighbor.setSortOrder(2);
        when(optionRepo.findByGroup_IdAndSortOrder(groupId, 2)).thenReturn(Optional.of(neighbor));
        when(optionRepo.findByIdAndGroup_Id(optionId, groupId)).thenReturn(Optional.of(oi));
        OptionItemResponse resp = new OptionItemResponse(optionId,"Ketchup", BigDecimal.ZERO,2,groupId, UUID.randomUUID());
        when(mapper.toResponse(oi)).thenReturn(resp);

        var out = service.moveOne(menuId, sectionId, itemId, groupId, optionId, 1);

        assertEquals(resp, out);
        verify(optionRepo).updateSortOrder(groupId, optionId, -optionId.intValue());
        verify(optionRepo).updateSortOrder(groupId, neighbor.getId(), 1);
        verify(optionRepo).updateSortOrder(groupId, optionId, 2);
    }

    @Test
    void moveOne_outOfBounds_noop() {
        Long menuId=1L, sectionId=2L, itemId=3L, groupId=4L, optionId=5L;
        OptionGroup g = group(menuId, sectionId, itemId, groupId);
        when(sectionRepo.findByIdAndMenu_Id(sectionId, menuId)).thenReturn(Optional.of(g.getItem().getSection()));
        when(itemRepo.findByIdAndSection_Id(itemId, sectionId)).thenReturn(Optional.of(g.getItem()));
        when(groupRepo.findByIdAndItem_Id(groupId, itemId)).thenReturn(Optional.of(g));
        OptionItem oi = new OptionItem(); oi.setId(optionId); oi.setGroup(g); oi.setSortOrder(1);
        when(optionRepo.findByIdAndGroup_Id(optionId, groupId)).thenReturn(Optional.of(oi));
        when(optionRepo.countByGroup_Id(groupId)).thenReturn(1L);
        OptionItemResponse resp = new OptionItemResponse(optionId,"Ketchup", BigDecimal.ZERO,1,groupId, UUID.randomUUID());
        when(mapper.toResponse(oi)).thenReturn(resp);

        var out = service.moveOne(menuId, sectionId, itemId, groupId, optionId, -1);

        assertEquals(resp, out);
        verify(optionRepo, never()).updateSortOrder(anyLong(), anyLong(), anyInt());
    }

    @Test
    void requireGroup_sectionMissing() {
        when(sectionRepo.findByIdAndMenu_Id(2L,1L)).thenReturn(Optional.empty());
        assertThrows(MenuSectionNotFound.class, () -> service.list(1L,2L,3L,4L));
    }

    @Test
    void requireGroup_itemMissing() {
        MenuSection s = new MenuSection(); s.setId(2L); Menu m = new Menu(); m.setId(1L); s.setMenu(m);
        when(sectionRepo.findByIdAndMenu_Id(2L,1L)).thenReturn(Optional.of(s));
        when(itemRepo.findByIdAndSection_Id(3L,2L)).thenReturn(Optional.empty());
        assertThrows(MenuItemException.class, () -> service.list(1L,2L,3L,4L));
    }

    @Test
    void requireGroup_groupMissing() {
        Menu m = new Menu(); m.setId(1L);
        MenuSection s = new MenuSection(); s.setId(2L); s.setMenu(m);
        MenuItem it = new MenuItem(); it.setId(3L); it.setSection(s);
        when(sectionRepo.findByIdAndMenu_Id(2L,1L)).thenReturn(Optional.of(s));
        when(itemRepo.findByIdAndSection_Id(3L,2L)).thenReturn(Optional.of(it));
        when(groupRepo.findByIdAndItem_Id(4L,3L)).thenReturn(Optional.empty());
        assertThrows(OptionGroupNotFoundException.class, () -> service.list(1L,2L,3L,4L));
    }

    @Test
    void create_insertAtMiddle_shiftsRightFromRequestedPos() {
        Long menuId=1L, sectionId=2L, itemId=3L, groupId=4L;
        OptionGroup g = group(menuId, sectionId, itemId, groupId);
        when(sectionRepo.findByIdAndMenu_Id(sectionId, menuId)).thenReturn(Optional.of(g.getItem().getSection()));
        when(itemRepo.findByIdAndSection_Id(itemId, sectionId)).thenReturn(Optional.of(g.getItem()));
        when(groupRepo.findByIdAndItem_Id(groupId, itemId)).thenReturn(Optional.of(g));

        OptionItemCreateRequest body = new OptionItemCreateRequest("Pickles", new BigDecimal("0.20"), 5);
        when(optionRepo.existsByGroup_IdAndNameIgnoreCase(groupId, "Pickles")).thenReturn(false);
        when(optionRepo.countByGroup_Id(groupId)).thenReturn(10L);

        OptionItem entity = new OptionItem(); entity.setGroup(g); entity.setSortOrder(5);
        when(mapper.toEntity(eq(body), eq(g))).thenReturn(entity);

        OptionItem saved = new OptionItem(); saved.setId(55L); saved.setGroup(g); saved.setSortOrder(5);
        OptionItemResponse resp = new OptionItemResponse(55L,"Pickles", new BigDecimal("0.20"),5,groupId, UUID.randomUUID());
        when(optionRepo.save(entity)).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(resp);

        var out = service.create(menuId, sectionId, itemId, groupId, body);

        assertEquals(resp, out);
        verify(optionRepo).shiftRightFrom(groupId, 5);
    }

    @Test
    void moveOne_neighborMissing_noop() {
        Long menuId=1L, sectionId=2L, itemId=3L, groupId=4L, optionId=5L;
        OptionGroup g = group(menuId, sectionId, itemId, groupId);
        when(sectionRepo.findByIdAndMenu_Id(sectionId, menuId)).thenReturn(Optional.of(g.getItem().getSection()));
        when(itemRepo.findByIdAndSection_Id(itemId, sectionId)).thenReturn(Optional.of(g.getItem()));
        when(groupRepo.findByIdAndItem_Id(groupId, itemId)).thenReturn(Optional.of(g));
        OptionItem oi = new OptionItem(); oi.setId(optionId); oi.setGroup(g); oi.setSortOrder(2);
        when(optionRepo.findByIdAndGroup_Id(optionId, groupId)).thenReturn(Optional.of(oi));
        when(optionRepo.countByGroup_Id(groupId)).thenReturn(5L);
        when(optionRepo.findByGroup_IdAndSortOrder(groupId, 1)).thenReturn(Optional.empty());
        OptionItemResponse resp = new OptionItemResponse(optionId,"Ketchup", BigDecimal.ZERO,2,groupId, UUID.randomUUID());
        when(mapper.toResponse(oi)).thenReturn(resp);

        var out = service.moveOne(menuId, sectionId, itemId, groupId, optionId, -1);

        assertEquals(resp, out);
        verify(optionRepo, never()).updateSortOrder(anyLong(), anyLong(), anyInt());
    }

    @Test
    void create_duplicate_caseInsensitive() {
        Long menuId=1L, sectionId=2L, itemId=3L, groupId=4L;
        OptionGroup g = group(menuId, sectionId, itemId, groupId);
        when(sectionRepo.findByIdAndMenu_Id(sectionId, menuId)).thenReturn(Optional.of(g.getItem().getSection()));
        when(itemRepo.findByIdAndSection_Id(itemId, sectionId)).thenReturn(Optional.of(g.getItem()));
        when(groupRepo.findByIdAndItem_Id(groupId, itemId)).thenReturn(Optional.of(g));

        when(optionRepo.existsByGroup_IdAndNameIgnoreCase(groupId, "ketchup")).thenReturn(true);

        assertThrows(AlreadyExistsException.class,
                () -> service.create(menuId, sectionId, itemId, groupId, new OptionItemCreateRequest("Ketchup", BigDecimal.ZERO, null)));

        verify(optionRepo, never()).save(any());
    }

}
