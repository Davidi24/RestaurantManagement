package pos.pos.Menu.Services_Unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pos.pos.DTO.Mapper.MenuMapper.OptionGroupMapper;
import pos.pos.DTO.Menu.OptionDTO.OptionGroupCreateRequest;
import pos.pos.DTO.Menu.OptionDTO.OptionGroupResponse;
import pos.pos.DTO.Menu.OptionDTO.OptionGroupUpdateRequest;
import pos.pos.Entity.Menu.Menu;
import pos.pos.Entity.Menu.MenuItem;
import pos.pos.Entity.Menu.MenuSection;
import pos.pos.Entity.Menu.OptionGroup;
import pos.pos.Entity.Menu.OptionGroupType;
import pos.pos.Exeption.AlreadyExistsException;
import pos.pos.Exeption.MenuItemException;
import pos.pos.Exeption.MenuSectionNotFound;
import pos.pos.Exeption.OptionGroupNotFoundException;
import pos.pos.Repository.Menu.MenuItemRepository;
import pos.pos.Repository.Menu.MenuSectionRepository;
import pos.pos.Repository.Order.OptionGroupRepository;
import pos.pos.Service.Menu.OptionGroupServiceImpl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OptionGroupServiceImplTest {

    @Mock OptionGroupRepository groupRepo;
    @Mock MenuItemRepository itemRepo;
    @Mock MenuSectionRepository sectionRepo;
    @Mock OptionGroupMapper mapper;

    @InjectMocks
    OptionGroupServiceImpl service;

    private MenuItem item(Long menuId, Long sectionId, Long itemId) {
        Menu menu = new Menu(); menu.setId(menuId);
        MenuSection sec = new MenuSection(); sec.setId(sectionId); sec.setMenu(menu);
        MenuItem it = new MenuItem(); it.setId(itemId); it.setSection(sec);
        return it;
    }

    @Test
    void listByItem_ok() {
        Long menuId=1L, sectionId=2L, itemId=3L;
        MenuItem it = item(menuId, sectionId, itemId);
        when(sectionRepo.findByIdAndMenu_Id(sectionId, menuId)).thenReturn(Optional.of(it.getSection()));
        when(itemRepo.findByIdAndSection_Id(itemId, sectionId)).thenReturn(Optional.of(it));
        OptionGroup g1 = new OptionGroup(), g2 = new OptionGroup();
        OptionGroupResponse r1 = new OptionGroupResponse(10L,"Sauces", OptionGroupType.MULTI,false,null,null,1,itemId, UUID.randomUUID(), List.of());
        OptionGroupResponse r2 = new OptionGroupResponse(11L,"Cheese", OptionGroupType.SINGLE,false,null,null,2,itemId, UUID.randomUUID(), List.of());
        when(groupRepo.findByItem_IdOrderBySortOrderAscIdAsc(itemId)).thenReturn(List.of(g1, g2));
        when(mapper.toResponse(g1)).thenReturn(r1);
        when(mapper.toResponse(g2)).thenReturn(r2);

        var out = service.listByItem(menuId, sectionId, itemId);

        assertEquals(List.of(r1, r2), out);
    }

    @Test
    void listByItem_notFound() {
        when(sectionRepo.findByIdAndMenu_Id(2L, 1L)).thenReturn(Optional.empty());
        assertThrows(MenuSectionNotFound.class, () -> service.listByItem(1L,2L,3L));
    }

    @Test
    void create_ok() {
        Long menuId=1L, sectionId=2L, itemId=3L;
        MenuItem it = item(menuId, sectionId, itemId);
        when(sectionRepo.findByIdAndMenu_Id(sectionId, menuId)).thenReturn(Optional.of(it.getSection()));
        when(itemRepo.findByIdAndSection_Id(itemId, sectionId)).thenReturn(Optional.of(it));
        OptionGroupCreateRequest body = new OptionGroupCreateRequest("Sauces", OptionGroupType.MULTI,false,null,null,null);
        when(groupRepo.existsByItem_IdAndNameIgnoreCase(itemId, "Sauces")).thenReturn(false);
        when(groupRepo.countByItem_Id(itemId)).thenReturn(0L);
        OptionGroup entity = new OptionGroup(); entity.setItem(it); entity.setSortOrder(1);
        when(mapper.toEntity(eq(body), eq(it))).thenReturn(entity);
        OptionGroup saved = entity; saved.setId(10L);
        OptionGroupResponse resp = new OptionGroupResponse(10L,"Sauces", OptionGroupType.MULTI,false,null,null,1,itemId, UUID.randomUUID(), List.of());
        when(groupRepo.save(entity)).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(resp);

        var out = service.create(menuId, sectionId, itemId, body);

        assertEquals(resp, out);
        verify(groupRepo).shiftRightFrom(itemId, 1);
    }

    @Test
    void create_duplicate() {
        Long menuId=1L, sectionId=2L, itemId=3L;
        MenuItem it = item(menuId, sectionId, itemId);
        when(sectionRepo.findByIdAndMenu_Id(sectionId, menuId)).thenReturn(Optional.of(it.getSection()));
        when(itemRepo.findByIdAndSection_Id(itemId, sectionId)).thenReturn(Optional.of(it));
        OptionGroupCreateRequest body = new OptionGroupCreateRequest("Sauces", OptionGroupType.SINGLE,false,null,null,null);
        when(groupRepo.existsByItem_IdAndNameIgnoreCase(itemId, "Sauces")).thenReturn(true);
        assertThrows(AlreadyExistsException.class, () -> service.create(menuId, sectionId, itemId, body));
        verify(groupRepo, never()).save(any());
    }

    @Test
    void getById_ok() {
        Long menuId=1L, sectionId=2L, itemId=3L, groupId=4L;
        MenuItem it = item(menuId, sectionId, itemId);
        when(sectionRepo.findByIdAndMenu_Id(sectionId, menuId)).thenReturn(Optional.of(it.getSection()));
        when(itemRepo.findByIdAndSection_Id(itemId, sectionId)).thenReturn(Optional.of(it));
        OptionGroup g = new OptionGroup(); g.setId(groupId); g.setItem(it);
        OptionGroupResponse resp = new OptionGroupResponse(groupId,"Sauces", OptionGroupType.MULTI,false,null,null,1,itemId, UUID.randomUUID(), List.of());
        when(groupRepo.findByIdAndItem_Id(groupId, itemId)).thenReturn(Optional.of(g));
        when(mapper.toResponse(g)).thenReturn(resp);

        var out = service.getById(menuId, sectionId, itemId, groupId);

        assertEquals(resp, out);
    }

    @Test
    void getById_notFound() {
        Long menuId=1L, sectionId=2L, itemId=3L, groupId=4L;
        MenuItem it = item(menuId, sectionId, itemId);
        when(sectionRepo.findByIdAndMenu_Id(sectionId, menuId)).thenReturn(Optional.of(it.getSection()));
        when(itemRepo.findByIdAndSection_Id(itemId, sectionId)).thenReturn(Optional.of(it));
        when(groupRepo.findByIdAndItem_Id(groupId, itemId)).thenReturn(Optional.empty());
        assertThrows(OptionGroupNotFoundException.class, () -> service.getById(menuId, sectionId, itemId, groupId));
    }

    @Test
    void patch_ok() {
        Long menuId=1L, sectionId=2L, itemId=3L, groupId=4L;
        MenuItem it = item(menuId, sectionId, itemId);
        when(sectionRepo.findByIdAndMenu_Id(sectionId, menuId)).thenReturn(Optional.of(it.getSection()));
        when(itemRepo.findByIdAndSection_Id(itemId, sectionId)).thenReturn(Optional.of(it));
        OptionGroup g = new OptionGroup(); g.setId(groupId); g.setItem(it); g.setName("Old"); g.setRequired(false);
        when(groupRepo.findByIdAndItem_Id(groupId, itemId)).thenReturn(Optional.of(g));
        when(groupRepo.existsByItem_IdAndNameIgnoreCase(itemId, "New")).thenReturn(false);
        OptionGroupUpdateRequest body = new OptionGroupUpdateRequest("New", OptionGroupType.SINGLE, true, 0, 2, 7);
        doAnswer(a -> { g.setName("New"); g.setRequired(true); g.setMinSelections(0); g.setMaxSelections(2); g.setSortOrder(7); return null; }).when(mapper).apply(body, g);
        when(groupRepo.save(g)).thenReturn(g);
        OptionGroupResponse resp = new OptionGroupResponse(groupId,"New", OptionGroupType.SINGLE,true,0,2,7,itemId, UUID.randomUUID(), List.of());
        when(mapper.toResponse(g)).thenReturn(resp);

        var out = service.patch(menuId, sectionId, itemId, groupId, body);

        assertEquals(resp, out);
    }

    @Test
    void patch_nameConflict() {
        Long menuId=1L, sectionId=2L, itemId=3L, groupId=4L;
        MenuItem it = item(menuId, sectionId, itemId);
        when(sectionRepo.findByIdAndMenu_Id(sectionId, menuId)).thenReturn(Optional.of(it.getSection()));
        when(itemRepo.findByIdAndSection_Id(itemId, sectionId)).thenReturn(Optional.of(it));
        OptionGroup g = new OptionGroup(); g.setId(groupId); g.setItem(it); g.setName("Old");
        when(groupRepo.findByIdAndItem_Id(groupId, itemId)).thenReturn(Optional.of(g));
        when(groupRepo.existsByItem_IdAndNameIgnoreCase(itemId, "New")).thenReturn(true);
        OptionGroupUpdateRequest body = new OptionGroupUpdateRequest("New", OptionGroupType.MULTI, false, null, null, null);

        assertThrows(AlreadyExistsException.class, () -> service.patch(menuId, sectionId, itemId, groupId, body));
    }

    @Test
    void patch_requiredMinGtMax_throws() {
        Long menuId=1L, sectionId=2L, itemId=3L, groupId=4L;
        MenuItem it = item(menuId, sectionId, itemId);
        when(sectionRepo.findByIdAndMenu_Id(sectionId, menuId)).thenReturn(Optional.of(it.getSection()));
        when(itemRepo.findByIdAndSection_Id(itemId, sectionId)).thenReturn(Optional.of(it));
        OptionGroup g = new OptionGroup(); g.setId(groupId); g.setItem(it); g.setName("Old");
        when(groupRepo.findByIdAndItem_Id(groupId, itemId)).thenReturn(Optional.of(g));
        OptionGroupUpdateRequest body = new OptionGroupUpdateRequest("Old", OptionGroupType.SINGLE, true, 3, 1, null);

        assertThrows(IllegalArgumentException.class, () -> service.patch(menuId, sectionId, itemId, groupId, body));
    }

    @Test
    void delete_ok() {
        Long menuId=1L, sectionId=2L, itemId=3L, groupId=4L;
        MenuItem it = item(menuId, sectionId, itemId);
        when(sectionRepo.findByIdAndMenu_Id(sectionId, menuId)).thenReturn(Optional.of(it.getSection()));
        when(itemRepo.findByIdAndSection_Id(itemId, sectionId)).thenReturn(Optional.of(it));
        OptionGroup g = new OptionGroup(); g.setId(groupId); g.setItem(it); g.setSortOrder(5);
        when(groupRepo.findByIdAndItem_Id(groupId, itemId)).thenReturn(Optional.of(g));

        service.delete(menuId, sectionId, itemId, groupId);

        verify(groupRepo).delete(g);
        verify(groupRepo).shiftLeftAfter(itemId, 5);
    }

    @Test
    void moveOne_ok_up() {
        Long menuId=1L, sectionId=2L, itemId=3L, groupId=4L;
        MenuItem it = item(menuId, sectionId, itemId);
        when(sectionRepo.findByIdAndMenu_Id(sectionId, menuId)).thenReturn(Optional.of(it.getSection()));
        when(itemRepo.findByIdAndSection_Id(itemId, sectionId)).thenReturn(Optional.of(it));
        OptionGroup g = new OptionGroup(); g.setId(groupId); g.setItem(it); g.setSortOrder(2);
        when(groupRepo.findByIdAndItem_Id(groupId, itemId)).thenReturn(Optional.of(g));
        when(groupRepo.countByItem_Id(itemId)).thenReturn(5L);
        OptionGroup neighbor = new OptionGroup(); neighbor.setId(99L); neighbor.setItem(it); neighbor.setSortOrder(1);
        when(groupRepo.findByItem_IdAndSortOrder(itemId, 1)).thenReturn(Optional.of(neighbor));
        when(groupRepo.findByIdAndItem_Id(groupId, itemId)).thenReturn(Optional.of(g));
        OptionGroupResponse resp = new OptionGroupResponse(groupId,"X", OptionGroupType.SINGLE,false,null,null,1,itemId, UUID.randomUUID(), List.of());
        when(mapper.toResponse(g)).thenReturn(resp);

        var out = service.moveOne(menuId, sectionId, itemId, groupId, -1);

        assertEquals(resp, out);
        verify(groupRepo).updateSortOrder(itemId, groupId, -groupId.intValue());
        verify(groupRepo).updateSortOrder(itemId, neighbor.getId(), 2);
        verify(groupRepo).updateSortOrder(itemId, groupId, 1);
    }

    @Test
    void moveOne_outOfBounds_noop() {
        Long menuId=1L, sectionId=2L, itemId=3L, groupId=4L;
        MenuItem it = item(menuId, sectionId, itemId);
        when(sectionRepo.findByIdAndMenu_Id(sectionId, menuId)).thenReturn(Optional.of(it.getSection()));
        when(itemRepo.findByIdAndSection_Id(itemId, sectionId)).thenReturn(Optional.of(it));
        OptionGroup g = new OptionGroup(); g.setId(groupId); g.setItem(it); g.setSortOrder(1);
        when(groupRepo.findByIdAndItem_Id(groupId, itemId)).thenReturn(Optional.of(g));
        when(groupRepo.countByItem_Id(itemId)).thenReturn(1L);
        OptionGroupResponse resp = new OptionGroupResponse(groupId,"X", OptionGroupType.SINGLE,false,null,null,1,itemId, UUID.randomUUID(), List.of());
        when(mapper.toResponse(g)).thenReturn(resp);

        var out = service.moveOne(menuId, sectionId, itemId, groupId, -1);

        assertEquals(resp, out);
        verify(groupRepo, never()).updateSortOrder(anyLong(), anyLong(), anyInt());
    }

    @Test
    void requireItem_sectionMissing() {
        when(sectionRepo.findByIdAndMenu_Id(2L, 1L)).thenReturn(Optional.empty());
        assertThrows(MenuSectionNotFound.class, () -> service.listByItem(1L,2L,3L));
    }

    @Test
    void requireItem_itemMissing() {
        MenuSection sec = new MenuSection(); sec.setId(2L); Menu m = new Menu(); m.setId(1L); sec.setMenu(m);
        when(sectionRepo.findByIdAndMenu_Id(2L, 1L)).thenReturn(Optional.of(sec));
        when(itemRepo.findByIdAndSection_Id(3L, 2L)).thenReturn(Optional.empty());
        assertThrows(MenuItemException.class, () -> service.listByItem(1L,2L,3L));
    }

    @Test
    void create_insertAtMiddle_shiftsRightFromRequestedPos() {
        Long menuId=1L, sectionId=2L, itemId=3L;
        MenuItem it = item(menuId, sectionId, itemId);
        when(sectionRepo.findByIdAndMenu_Id(sectionId, menuId)).thenReturn(Optional.of(it.getSection()));
        when(itemRepo.findByIdAndSection_Id(itemId, sectionId)).thenReturn(Optional.of(it));

        OptionGroupCreateRequest body = new OptionGroupCreateRequest("Extras", OptionGroupType.MULTI, false, null, null, 5);
        when(groupRepo.existsByItem_IdAndNameIgnoreCase(itemId, "Extras")).thenReturn(false);
        when(groupRepo.countByItem_Id(itemId)).thenReturn(10L);

        OptionGroup entity = new OptionGroup(); entity.setItem(it); entity.setSortOrder(5);
        when(mapper.toEntity(eq(body), eq(it))).thenReturn(entity);

        OptionGroup saved = new OptionGroup(); saved.setId(77L); saved.setItem(it); saved.setSortOrder(5);
        OptionGroupResponse resp = new OptionGroupResponse(77L,"Extras", OptionGroupType.MULTI,false,null,null,5,itemId, UUID.randomUUID(), List.of());

        when(groupRepo.save(entity)).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(resp);

        var out = service.create(menuId, sectionId, itemId, body);

        assertEquals(resp, out);
        verify(groupRepo).shiftRightFrom(itemId, 5);
    }

    @Test
    void moveOne_neighborMissing_noop() {
        Long menuId=1L, sectionId=2L, itemId=3L, groupId=4L;
        MenuItem it = item(menuId, sectionId, itemId);
        when(sectionRepo.findByIdAndMenu_Id(sectionId, menuId)).thenReturn(Optional.of(it.getSection()));
        when(itemRepo.findByIdAndSection_Id(itemId, sectionId)).thenReturn(Optional.of(it));
        OptionGroup g = new OptionGroup(); g.setId(groupId); g.setItem(it); g.setSortOrder(2);
        when(groupRepo.findByIdAndItem_Id(groupId, itemId)).thenReturn(Optional.of(g));
        when(groupRepo.countByItem_Id(itemId)).thenReturn(5L);
        when(groupRepo.findByItem_IdAndSortOrder(itemId, 1)).thenReturn(Optional.empty());
        OptionGroupResponse resp = new OptionGroupResponse(groupId,"X", OptionGroupType.SINGLE,false,null,null,2,itemId, UUID.randomUUID(), List.of());
        when(mapper.toResponse(g)).thenReturn(resp);

        var out = service.moveOne(menuId, sectionId, itemId, groupId, -1);

        assertEquals(resp, out);
        verify(groupRepo, never()).updateSortOrder(anyLong(), anyLong(), anyInt());
    }

    @Test
    void create_duplicate_caseInsensitive() {
        Long menuId=1L, sectionId=2L, itemId=3L;
        MenuItem it = item(menuId, sectionId, itemId);
        when(sectionRepo.findByIdAndMenu_Id(sectionId, menuId)).thenReturn(Optional.of(it.getSection()));
        when(itemRepo.findByIdAndSection_Id(itemId, sectionId)).thenReturn(Optional.of(it));
        when(groupRepo.existsByItem_IdAndNameIgnoreCase(itemId, "sauces")).thenReturn(true);

        OptionGroupCreateRequest body = new OptionGroupCreateRequest("Sauces", OptionGroupType.MULTI, false, null, null, null);

        assertThrows(AlreadyExistsException.class, () -> service.create(menuId, sectionId, itemId, body));
        verify(groupRepo, never()).save(any());
    }

}
