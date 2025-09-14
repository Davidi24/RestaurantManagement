package pos.pos.Menu.Services_Unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pos.pos.DTO.Mapper.MenuMapper.MenuItemMapper;
import pos.pos.DTO.Menu.MenuItemDTO.MenuItemCreateRequest;
import pos.pos.DTO.Menu.MenuItemDTO.MenuItemResponse;
import pos.pos.DTO.Menu.MenuItemDTO.MenuItemUpdateRequest;
import pos.pos.Entity.Menu.MenuItem;
import pos.pos.Entity.Menu.MenuSection;
import pos.pos.Exeption.AlreadyExistsException;
import pos.pos.Exeption.MenuItemException;
import pos.pos.Exeption.MenuSectionNotFound;
import pos.pos.Repository.Menu.MenuItemRepository;
import pos.pos.Repository.Menu.MenuSectionRepository;
import pos.pos.Service.Menu.MenuItemServiceImpl;
import pos.pos.Util.OrderingManger;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuItemServiceImplTest {

    @Mock MenuSectionRepository sectionRepository;
    @Mock MenuItemRepository itemRepository;
    @Mock MenuItemMapper mapper;

    @InjectMocks
    MenuItemServiceImpl service;

    @Test
    void listItems_ok() {
        Long menuId = 1L, sectionId = 2L;
        when(sectionRepository.findByIdAndMenu_Id(sectionId, menuId)).thenReturn(Optional.of(new MenuSection()));
        MenuItem i1 = new MenuItem(), i2 = new MenuItem();
        MenuItemResponse r1 = new MenuItemResponse(1L, "A", BigDecimal.ZERO, true, 1, UUID.randomUUID(), List.of(), List.of());
        MenuItemResponse r2 = new MenuItemResponse(2L, "B", BigDecimal.ZERO, true, 2, UUID.randomUUID(), List.of(), List.of());
        when(itemRepository.findBySection_IdOrderBySortOrderAscIdAsc(sectionId)).thenReturn(List.of(i1, i2));
        when(mapper.toMenuItemResponse(i1)).thenReturn(r1);
        when(mapper.toMenuItemResponse(i2)).thenReturn(r2);

        var out = service.listItems(menuId, sectionId);

        assertEquals(List.of(r1, r2), out);
    }

    @Test
    void listItems_sectionNotFound() {
        when(sectionRepository.findByIdAndMenu_Id(2L, 1L)).thenReturn(Optional.empty());
        assertThrows(MenuSectionNotFound.class, () -> service.listItems(1L, 2L));
    }

    @Test
    void createItem_ok_autoAppend() {
        Long menuId = 1L, sectionId = 2L;
        MenuSection section = new MenuSection(); section.setId(sectionId);
        MenuItemCreateRequest req = new MenuItemCreateRequest("New", BigDecimal.ONE, true, null);
        MenuItem entity = new MenuItem(); entity.setId(10L);
        MenuItem saved = new MenuItem(); saved.setId(10L); saved.setSortOrder(1); saved.setSection(section);
        MenuItemResponse resp = new MenuItemResponse(10L, "New", BigDecimal.ONE, true, 1, UUID.randomUUID(), List.of(), List.of());

        when(sectionRepository.findByIdAndMenu_Id(sectionId, menuId)).thenReturn(Optional.of(section));
        when(itemRepository.existsBySection_IdAndNameIgnoreCase(sectionId, "New")).thenReturn(false);
        when(itemRepository.countBySection_Id(sectionId)).thenReturn(0L);
        try (MockedStatic<OrderingManger> mocked = mockStatic(OrderingManger.class)) {
            mocked.when(() -> OrderingManger.clamp(1, 1, 1)).thenReturn(1);
            when(mapper.toMenuItem(req)).thenReturn(entity);
            when(itemRepository.save(entity)).then(inv -> {
                entity.setId(10L);
                return saved;
            });
            when(mapper.toMenuItemResponse(saved)).thenReturn(resp);

            var out = service.createItem(menuId, sectionId, req);

            assertEquals(resp, out);
            verify(itemRepository).shiftRightFrom(sectionId, 1);
            assertEquals(1, entity.getSortOrder());
            assertEquals(section, entity.getSection());
        }
    }

    @Test
    void createItem_duplicateName() {
        Long menuId = 1L, sectionId = 2L;
        when(sectionRepository.findByIdAndMenu_Id(sectionId, menuId)).thenReturn(Optional.of(new MenuSection()));
        when(itemRepository.existsBySection_IdAndNameIgnoreCase(sectionId, "New")).thenReturn(true);
        assertThrows(AlreadyExistsException.class,
                () -> service.createItem(menuId, sectionId, new MenuItemCreateRequest("New", BigDecimal.ONE, true, null)));
        verify(itemRepository, never()).save(any());
    }

    @Test
    void createItem_sectionNotFound() {
        when(sectionRepository.findByIdAndMenu_Id(2L, 1L)).thenReturn(Optional.empty());
        assertThrows(MenuSectionNotFound.class,
                () -> service.createItem(1L, 2L, new MenuItemCreateRequest("X", BigDecimal.ZERO, true, null)));
    }

    @Test
    void updateItem_ok_preservesPosition() {
        Long menuId = 1L, sectionId = 2L, itemId = 3L;
        when(sectionRepository.findByIdAndMenu_Id(sectionId, menuId)).thenReturn(Optional.of(new MenuSection()));
        MenuItem item = new MenuItem(); item.setId(itemId); item.setName("Old"); item.setSortOrder(5);
        when(itemRepository.findByIdAndSection_Id(itemId, sectionId)).thenReturn(Optional.of(item));
        when(itemRepository.existsBySection_IdAndNameIgnoreCase(sectionId, "New")).thenReturn(false);
        MenuItemUpdateRequest req = new MenuItemUpdateRequest("New", BigDecimal.TEN, true, 9);
        doAnswer(a -> { item.setName("New"); item.setSortOrder(9); return null; }).when(mapper).apply(req, item);
        when(itemRepository.save(item)).thenReturn(item);
        when(itemRepository.findByIdAndSection_Id(itemId, sectionId)).thenReturn(Optional.of(item));
        MenuItemResponse resp = new MenuItemResponse(itemId, "New", BigDecimal.TEN, true, 5, UUID.randomUUID(), List.of(), List.of());
        when(mapper.toMenuItemResponse(item)).thenReturn(resp);

        var out = service.updateItem(menuId, sectionId, itemId, req);

        assertEquals(resp, out);
        assertEquals(5, item.getSortOrder());
        assertEquals("New", item.getName());
    }

    @Test
    void updateItem_nameConflict() {
        Long menuId = 1L, sectionId = 2L, itemId = 3L;
        when(sectionRepository.findByIdAndMenu_Id(sectionId, menuId)).thenReturn(Optional.of(new MenuSection()));
        MenuItem item = new MenuItem(); item.setId(itemId); item.setName("Old"); item.setSortOrder(1);
        when(itemRepository.findByIdAndSection_Id(itemId, sectionId)).thenReturn(Optional.of(item));
        when(itemRepository.existsBySection_IdAndNameIgnoreCase(sectionId, "Old2")).thenReturn(true);

        assertThrows(AlreadyExistsException.class,
                () -> service.updateItem(menuId, sectionId, itemId, new MenuItemUpdateRequest("Old2", null, null, null)));
    }

    @Test
    void updateItem_notFound() {
        when(sectionRepository.findByIdAndMenu_Id(2L, 1L)).thenReturn(Optional.of(new MenuSection()));
        when(itemRepository.findByIdAndSection_Id(3L, 2L)).thenReturn(Optional.empty());
        assertThrows(MenuItemException.class,
                () -> service.updateItem(1L, 2L, 3L, new MenuItemUpdateRequest(null, null, null, null)));
    }

    @Test
    void deleteItem_ok_shiftsLeft() {
        Long menuId = 1L, sectionId = 2L, itemId = 3L;
        when(sectionRepository.findByIdAndMenu_Id(sectionId, menuId)).thenReturn(Optional.of(new MenuSection()));
        MenuItem item = new MenuItem(); item.setId(itemId); item.setSortOrder(4);
        when(itemRepository.findByIdAndSection_Id(itemId, sectionId)).thenReturn(Optional.of(item));

        service.deleteItem(menuId, sectionId, itemId);

        verify(itemRepository).delete(item);
        verify(itemRepository).shiftLeftAfter(sectionId, 4);
    }

    @Test
    void deleteItem_notFound() {
        when(sectionRepository.findByIdAndMenu_Id(2L, 1L)).thenReturn(Optional.of(new MenuSection()));
        when(itemRepository.findByIdAndSection_Id(3L, 2L)).thenReturn(Optional.empty());
        assertThrows(MenuItemException.class, () -> service.deleteItem(1L, 2L, 3L));
    }

    @Test
    void moveOne_up_ok() {
        Long menuId = 1L, sectionId = 2L, itemId = 3L;
        when(sectionRepository.findByIdAndMenu_Id(sectionId, menuId)).thenReturn(Optional.of(new MenuSection()));
        MenuItem item = new MenuItem(); item.setId(itemId); item.setSortOrder(3);
        when(itemRepository.findByIdAndSection_Id(itemId, sectionId)).thenReturn(Optional.of(item));
        when(itemRepository.countBySection_Id(sectionId)).thenReturn(5L);
        MenuItem neighbor = new MenuItem(); neighbor.setId(99L); neighbor.setSortOrder(2);
        when(itemRepository.findBySection_IdAndSortOrder(sectionId, 2)).thenReturn(Optional.of(neighbor));
        when(itemRepository.findByIdAndSection_Id(itemId, sectionId)).thenReturn(Optional.of(item));
        MenuItemResponse resp = new MenuItemResponse(itemId, "X", BigDecimal.ZERO, true, 2, UUID.randomUUID(), List.of(), List.of());
        when(mapper.toMenuItemResponse(item)).thenReturn(resp);

        var out = service.moveOne(menuId, sectionId, itemId, -1);

        assertEquals(resp, out);
        verify(itemRepository).updateSortOrder(sectionId, itemId, -itemId.intValue());
        verify(itemRepository).updateSortOrder(sectionId, neighbor.getId(), 3);
        verify(itemRepository).updateSortOrder(sectionId, itemId, 2);
    }

    @Test
    void moveOne_outOfBounds_noop() {
        Long menuId = 1L, sectionId = 2L, itemId = 3L;
        when(sectionRepository.findByIdAndMenu_Id(sectionId, menuId)).thenReturn(Optional.of(new MenuSection()));
        MenuItem item = new MenuItem(); item.setId(itemId); item.setSortOrder(1);
        when(itemRepository.findByIdAndSection_Id(itemId, sectionId)).thenReturn(Optional.of(item));
        when(itemRepository.countBySection_Id(sectionId)).thenReturn(1L);
        MenuItemResponse resp = new MenuItemResponse(itemId, "X", BigDecimal.ZERO, true, 1, UUID.randomUUID(), List.of(), List.of());
        when(mapper.toMenuItemResponse(item)).thenReturn(resp);

        var out = service.moveOne(menuId, sectionId, itemId, -1);

        assertEquals(resp, out);
        verify(itemRepository, never()).updateSortOrder(anyLong(), anyLong(), anyInt());
    }

    @Test
    void getItems_ok() {
        Long menuId = 1L, sectionId = 2L, itemId = 3L;
        when(sectionRepository.findByIdAndMenu_Id(sectionId, menuId)).thenReturn(Optional.of(new MenuSection()));
        MenuItem item = new MenuItem(); item.setId(itemId);
        when(itemRepository.findByIdAndSection_Id(itemId, sectionId)).thenReturn(Optional.of(item));
        MenuItemResponse resp = new MenuItemResponse(itemId, "X", BigDecimal.TEN, true, 1, UUID.randomUUID(), List.of(), List.of());
        when(mapper.toMenuItemResponse(item)).thenReturn(resp);

        var out = service.getItems(menuId, sectionId, itemId);

        assertEquals(resp, out);
    }

    @Test
    void getItems_notFound() {
        when(sectionRepository.findByIdAndMenu_Id(2L, 1L)).thenReturn(Optional.of(new MenuSection()));
        when(itemRepository.findByIdAndSection_Id(3L, 2L)).thenReturn(Optional.empty());
        assertThrows(MenuItemException.class, () -> service.getItems(1L, 2L, 3L));
    }


    @Test
    void createItem_insertAtMiddle_clampsAndShifts() {
        Long menuId = 1L, sectionId = 2L;
        MenuSection section = new MenuSection(); section.setId(sectionId);
        MenuItemCreateRequest req = new MenuItemCreateRequest("Mid", BigDecimal.ONE, true, 5);

        when(sectionRepository.findByIdAndMenu_Id(sectionId, menuId)).thenReturn(Optional.of(section));
        when(itemRepository.existsBySection_IdAndNameIgnoreCase(sectionId, "Mid")).thenReturn(false);
        when(itemRepository.countBySection_Id(sectionId)).thenReturn(10L);

        MenuItem entity = new MenuItem();
        when(mapper.toMenuItem(req)).thenReturn(entity);

        MenuItem saved = new MenuItem(); saved.setId(42L); saved.setSortOrder(5); saved.setSection(section);
        when(itemRepository.save(entity)).thenAnswer(inv -> { entity.setId(42L); entity.setSortOrder(5); entity.setSection(section); return saved; });

        MenuItemResponse resp = new MenuItemResponse(42L, "Mid", BigDecimal.ONE, true, 5, UUID.randomUUID(), List.of(), List.of());
        when(mapper.toMenuItemResponse(saved)).thenReturn(resp);

        var out = service.createItem(menuId, sectionId, req);

        assertEquals(resp, out);
        verify(itemRepository).shiftRightFrom(sectionId, 5);
        assertEquals(5, entity.getSortOrder());
        assertEquals(section, entity.getSection());
    }

    @Test
    void moveOne_neighborMissing_noop() {
        Long menuId = 1L, sectionId = 2L, itemId = 3L;
        when(sectionRepository.findByIdAndMenu_Id(sectionId, menuId)).thenReturn(Optional.of(new MenuSection()));
        MenuItem item = new MenuItem(); item.setId(itemId); item.setSortOrder(3);
        when(itemRepository.findByIdAndSection_Id(itemId, sectionId)).thenReturn(Optional.of(item));
        when(itemRepository.countBySection_Id(sectionId)).thenReturn(5L);
        when(itemRepository.findBySection_IdAndSortOrder(sectionId, 2)).thenReturn(Optional.empty());

        var out = service.moveOne(menuId, sectionId, itemId, -1);

        assertNotNull(out);
        verify(itemRepository, never()).updateSortOrder(anyLong(), anyLong(), anyInt());
    }

    @Test
    void createItem_duplicateName_caseInsensitive() {
        Long menuId = 1L, sectionId = 2L;
        when(sectionRepository.findByIdAndMenu_Id(sectionId, menuId)).thenReturn(Optional.of(new MenuSection()));
        when(itemRepository.existsBySection_IdAndNameIgnoreCase(sectionId, "new")).thenReturn(true);

        assertThrows(AlreadyExistsException.class,
                () -> service.createItem(menuId, sectionId, new MenuItemCreateRequest("New", BigDecimal.ONE, true, null)));

        verify(itemRepository, never()).save(any());
    }

}
