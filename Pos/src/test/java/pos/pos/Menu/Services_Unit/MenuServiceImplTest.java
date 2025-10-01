package pos.pos.Menu.Services_Unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pos.pos.DTO.Mapper.MenuMapper.MenuMapper;
import pos.pos.DTO.Menu.MenuDTO.MenuRequest;
import pos.pos.DTO.Menu.MenuDTO.MenuResponse;
import pos.pos.DTO.Menu.MenuDTO.MenuTreeResponse;
import pos.pos.DTO.Menu.MenuSectionDTO.MenuSectionResponse;
import pos.pos.Entity.Menu.Menu;
import pos.pos.Exeption.General.AlreadyExistsException;
import pos.pos.Exeption.MenuNotFoundException;
import pos.pos.Repository.Menu.MenuRepository;
import pos.pos.Service.Menu.MenuServiceImpl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuServiceImplTest {

    @Mock MenuRepository menuRepo;
    @Mock MenuMapper menuMapper;

    @InjectMocks
    MenuServiceImpl service;

    @Test
    void create_ok() {
        MenuRequest req = new MenuRequest("Main", "Desc");
        Menu entity = new Menu();
        MenuResponse resp = new MenuResponse(1L, "Main", "Desc", UUID.randomUUID());

        when(menuRepo.existsByName("Main")).thenReturn(false);
        when(menuMapper.toMenu(req)).thenReturn(entity);
        when(menuRepo.save(entity)).thenReturn(entity);
        when(menuMapper.toMenuResponse(entity)).thenReturn(resp);

        MenuResponse out = service.create(req);

        assertEquals(resp, out);
        verify(menuRepo).existsByName("Main");
        verify(menuRepo).save(entity);
    }

    @Test
    void create_alreadyExists() {
        MenuRequest req = new MenuRequest("Main", "Desc");
        when(menuRepo.existsByName("Main")).thenReturn(true);
        assertThrows(AlreadyExistsException.class, () -> service.create(req));
        verify(menuRepo, never()).save(any());
    }

    @Test
    void list_ok() {
        Menu m1 = new Menu(); Menu m2 = new Menu();
        MenuResponse r1 = new MenuResponse(1L, "A", "a", UUID.randomUUID());
        MenuResponse r2 = new MenuResponse(2L, "B", "b", UUID.randomUUID());

        when(menuRepo.findAll()).thenReturn(List.of(m1, m2));
        when(menuMapper.toMenuResponse(m1)).thenReturn(r1);
        when(menuMapper.toMenuResponse(m2)).thenReturn(r2);

        var out = service.list();

        assertEquals(List.of(r1, r2), out);
    }

    @Test
    void get_ok() {
        Menu m = new Menu();
        MenuResponse r = new MenuResponse(1L, "A", "a", UUID.randomUUID());
        when(menuRepo.findById(1L)).thenReturn(Optional.of(m));
        when(menuMapper.toMenuResponse(m)).thenReturn(r);

        var out = service.get(1L);

        assertEquals(r, out);
    }

    @Test
    void get_notFound() {
        when(menuRepo.findById(9L)).thenReturn(Optional.empty());
        assertThrows(MenuNotFoundException.class, () -> service.get(9L));
    }

    @Test
    void patch_updatesNameAndDescription() {
        Menu m = new Menu();
        MenuRequest body = new MenuRequest("  NewName  ", "  NewDesc  ");
        MenuResponse r = new MenuResponse(1L, "NewName", "NewDesc", UUID.randomUUID());

        when(menuRepo.findById(1L)).thenReturn(Optional.of(m));
        when(menuRepo.save(m)).thenReturn(m);
        when(menuMapper.toMenuResponse(m)).thenReturn(r);

        var out = service.patch(1L, body);

        assertEquals(r, out);
        verify(menuRepo).save(m);
    }

    @Test
    void patch_handlesEmptyDescription() {
        Menu m = new Menu();
        MenuRequest body = new MenuRequest("Name", "  ");
        MenuResponse r = new MenuResponse(1L, "Name", null, UUID.randomUUID());

        when(menuRepo.findById(1L)).thenReturn(Optional.of(m));
        when(menuRepo.save(m)).thenReturn(m);
        when(menuMapper.toMenuResponse(m)).thenReturn(r);

        var out = service.patch(1L, body);

        assertEquals(r, out);
    }

    @Test
    void patch_notFound() {
        when(menuRepo.findById(7L)).thenReturn(Optional.empty());
        assertThrows(MenuNotFoundException.class, () -> service.patch(7L, new MenuRequest("X","Y")));
    }

    @Test
    void delete_ok() {
        when(menuRepo.existsById(3L)).thenReturn(true);
        service.delete(3L);
        verify(menuRepo).deleteById(3L);
    }

    @Test
    void delete_notFound() {
        when(menuRepo.existsById(3L)).thenReturn(false);
        assertThrows(MenuNotFoundException.class, () -> service.delete(3L));
        verify(menuRepo, never()).deleteById(anyLong());
    }

    @Test
    void tree_ok() {
        Menu m = new Menu();
        MenuTreeResponse tr = new MenuTreeResponse(1L, "Tree", "desc", UUID.randomUUID(), List.<MenuSectionResponse>of());
        when(menuRepo.findWithTreeById(2L)).thenReturn(Optional.of(m));
        when(menuMapper.toMenuTreeResponse(m)).thenReturn(tr);

        var out = service.tree(2L);

        assertEquals(tr, out);
    }

    @Test
    void tree_notFound() {
        when(menuRepo.findWithTreeById(2L)).thenReturn(Optional.empty());
        assertThrows(MenuNotFoundException.class, () -> service.tree(2L));
    }

}
