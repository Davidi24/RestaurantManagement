package pos.pos.Menu.Services_Unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pos.pos.DTO.Mapper.MenuMapper.MenuSectionMapper;
import pos.pos.DTO.Menu.MenuSectionDTO.MenuSectionCreateRequest;
import pos.pos.DTO.Menu.MenuSectionDTO.MenuSectionResponse;
import pos.pos.DTO.Menu.MenuSectionDTO.MenuSectionUpdateRequest;
import pos.pos.Entity.Menu.Menu;
import pos.pos.Entity.Menu.MenuSection;
import pos.pos.Exeption.General.AlreadyExistsException;
import pos.pos.Exeption.MenuNotFoundException;
import pos.pos.Exeption.MenuSectionNotFound;
import pos.pos.Repository.Menu.MenuRepository;
import pos.pos.Repository.Menu.MenuSectionRepository;
import pos.pos.Service.Menu.MenuSectionServiceImpl;
import pos.pos.Util.OrderingManger;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuSectionServiceImplTest {

    @Mock MenuRepository menuRepository;
    @Mock MenuSectionRepository sectionRepository;
    @Mock MenuSectionMapper mapper;

    @InjectMocks
    MenuSectionServiceImpl service;

    @Test
    void listSections_ok() {
        Long menuId = 1L;
        List<MenuSection> entities = List.of(new MenuSection(), new MenuSection());
        List<MenuSectionResponse> dtos = List.of(
                new MenuSectionResponse(1L,"Starters",1,new BigDecimal("1.0"), UUID.randomUUID(), List.of()),
                new MenuSectionResponse(2L,"Mains",2,new BigDecimal("2.0"), UUID.randomUUID(), List.of())
        );

        when(sectionRepository.findByMenu_IdOrderByOrderKeyAsc(menuId)).thenReturn(entities);
        when(mapper.toMenuSectionResponse(entities)).thenReturn(dtos);

        var out = service.listSections(menuId);

        assertEquals(dtos, out);
    }

    @Test
    void createSection_ok() {
        Long menuId = 1L;
        MenuSectionCreateRequest req = new MenuSectionCreateRequest("Starters", null);
        Menu menu = new Menu(); menu.setId(menuId);
        MenuSection entity = new MenuSection(); entity.setName("Starters"); entity.setMenu(menu);
        BigDecimal key = new BigDecimal("1.000000");
        MenuSectionResponse dto = new MenuSectionResponse(10L,"Starters",1,key,UUID.randomUUID(), List.of());

        when(menuRepository.findById(menuId)).thenReturn(Optional.of(menu));
        when(sectionRepository.existsByMenu_IdAndNameIgnoreCase(menuId, "Starters")).thenReturn(false);
        when(sectionRepository.countByMenu_Id(menuId)).thenReturn(0L);
        when(mapper.toMenuSection(req)).thenReturn(entity);
        when(sectionRepository.save(entity)).thenReturn(entity);
        when(sectionRepository.countBefore(menuId, key)).thenReturn(0L);
        when(mapper.toMenuSectionResponse(entity, 1)).thenReturn(dto);

        try (MockedStatic<OrderingManger> mocked = mockStatic(OrderingManger.class)) {
            mocked.when(() -> OrderingManger.computeInsertKeyDecimal(
                    anyInt(), anyLong(), any(), any(), any(), any()
            )).thenReturn(key);

            var out = service.createSection(menuId, req);

            assertEquals(dto, out);
            assertEquals(key, entity.getOrderKey());
        }
    }

    @Test
    void createSection_duplicateName() {
        Long menuId = 1L;
        MenuSectionCreateRequest req = new MenuSectionCreateRequest("Starters", null);
        Menu menu = new Menu(); menu.setId(menuId);

        when(menuRepository.findById(menuId)).thenReturn(Optional.of(menu));
        when(sectionRepository.existsByMenu_IdAndNameIgnoreCase(menuId, "Starters")).thenReturn(true);

        assertThrows(AlreadyExistsException.class, () -> service.createSection(menuId, req));
        verify(sectionRepository, never()).save(any());
    }

    @Test
    void createSection_menuNotFound() {
        when(menuRepository.findById(9L)).thenReturn(Optional.empty());
        assertThrows(MenuNotFoundException.class, () -> service.createSection(9L, new MenuSectionCreateRequest("x", null)));
    }

    @Test
    void updateSection_ok_nameChange() {
        Long menuId = 1L; Long sectionId = 2L;
        Menu menu = new Menu(); menu.setId(menuId);
        MenuSection section = new MenuSection(); section.setId(sectionId); section.setMenu(menu); section.setName("Old");
        MenuSectionUpdateRequest req = new MenuSectionUpdateRequest("New", null);
        BigDecimal key = new BigDecimal("1.0");
        section.setOrderKey(key);
        MenuSectionResponse dto = new MenuSectionResponse(sectionId,"New",1,key,UUID.randomUUID(), List.of());

        when(sectionRepository.findByIdAndMenu_Id(sectionId, menuId)).thenReturn(Optional.of(section));
        when(sectionRepository.existsByMenu_IdAndNameIgnoreCase(menuId, "New")).thenReturn(false);
        doAnswer(inv -> { section.setName("New"); return null; }).when(mapper).update(req, section);
        when(sectionRepository.countBefore(menuId, key)).thenReturn(0L);
        when(mapper.toMenuSectionResponse(section, 1)).thenReturn(dto);

        var out = service.updateSection(menuId, sectionId, req);

        assertEquals(dto, out);
        assertEquals("New", section.getName());
    }

    @Test
    void updateSection_conflictName() {
        Long menuId = 1L; Long sectionId = 2L;
        Menu menu = new Menu(); menu.setId(menuId);
        MenuSection section = new MenuSection(); section.setId(sectionId); section.setMenu(menu); section.setName("Old");
        MenuSectionUpdateRequest req = new MenuSectionUpdateRequest("New", null);

        when(sectionRepository.findByIdAndMenu_Id(sectionId, menuId)).thenReturn(Optional.of(section));
        when(sectionRepository.existsByMenu_IdAndNameIgnoreCase(menuId, "New")).thenReturn(true);

        assertThrows(AlreadyExistsException.class, () -> service.updateSection(menuId, sectionId, req));
    }

    @Test
    void updateSection_notFound() {
        when(sectionRepository.findByIdAndMenu_Id(2L, 1L)).thenReturn(Optional.empty());
        assertThrows(MenuSectionNotFound.class, () -> service.updateSection(1L, 2L, new MenuSectionUpdateRequest("x", null)));
    }

    @Test
    void deleteSection_ok() {
        Long menuId = 1L; Long sectionId = 2L;
        Menu menu = new Menu(); menu.setId(menuId);
        MenuSection section = new MenuSection(); section.setId(sectionId); section.setMenu(menu);

        when(sectionRepository.findByIdAndMenu_Id(sectionId, menuId)).thenReturn(Optional.of(section));

        service.deleteSection(menuId, sectionId);

        verify(sectionRepository).delete(section);
    }

    @Test
    void deleteSection_notFound() {
        when(sectionRepository.findByIdAndMenu_Id(2L, 1L)).thenReturn(Optional.empty());
        assertThrows(MenuSectionNotFound.class, () -> service.deleteSection(1L, 2L));
    }

    @Test
    void moveSection_ok() {
        Long menuId = 1L; Long sectionId = 2L;
        Menu menu = new Menu(); menu.setId(menuId);
        MenuSection section = new MenuSection(); section.setId(sectionId); section.setMenu(menu);
        section.setOrderKey(new BigDecimal("5.0"));
        BigDecimal newKey = new BigDecimal("2.5");
        MenuSectionResponse dto = new MenuSectionResponse(sectionId,"Name",1,newKey,UUID.randomUUID(), List.of());

        when(sectionRepository.findByIdAndMenu_Id(sectionId, menuId)).thenReturn(Optional.of(section));
        when(sectionRepository.countByMenu_Id(menuId)).thenReturn(3L);
        when(sectionRepository.countBefore(menuId, newKey)).thenReturn(0L);
        when(mapper.toMenuSectionResponse(section, 1)).thenReturn(dto);

        try (MockedStatic<OrderingManger> mocked = mockStatic(OrderingManger.class)) {
            mocked.when(() -> OrderingManger.computeMoveKeyDecimal(
                    anyInt(), anyLong(), any(), any(), any(), any(), eq(section.getOrderKey())
            )).thenReturn(newKey);

            var out = service.moveSection(menuId, sectionId, 1);

            assertEquals(dto, out);
            assertEquals(newKey, section.getOrderKey());
        }
    }

    @Test
    void moveSection_notFound() {
        when(sectionRepository.findByIdAndMenu_Id(2L, 1L)).thenReturn(Optional.empty());
        assertThrows(MenuSectionNotFound.class, () -> service.moveSection(1L, 2L, 1));
    }

    @Test
    void getSection_ok() {
        Long menuId = 1L; Long sectionId = 2L;
        Menu menu = new Menu(); menu.setId(menuId);
        MenuSection section = new MenuSection(); section.setId(sectionId); section.setMenu(menu);
        section.setOrderKey(new BigDecimal("1.0"));
        MenuSectionResponse dto = new MenuSectionResponse(sectionId,"Name",1,new BigDecimal("1.0"),UUID.randomUUID(), List.of());

        when(sectionRepository.findByIdAndMenu_Id(sectionId, menuId)).thenReturn(Optional.of(section));
        when(sectionRepository.countBefore(menuId, section.getOrderKey())).thenReturn(0L);
        when(mapper.toMenuSectionResponse(section, 1)).thenReturn(dto);

        var out = service.getSection(menuId, sectionId);

        assertEquals(dto, out);
    }

    @Test
    void getSection_notFound() {
        when(sectionRepository.findByIdAndMenu_Id(2L, 1L)).thenReturn(Optional.empty());
        assertThrows(MenuSectionNotFound.class, () -> service.getSection(1L, 2L));
    }


}
