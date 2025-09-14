package pos.pos.Menu.Services_Unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pos.pos.DTO.Mapper.MenuMapper.ItemVariantMapper;
import pos.pos.DTO.Menu.VariantDTO.ItemVariantCreateRequest;
import pos.pos.DTO.Menu.VariantDTO.ItemVariantResponse;
import pos.pos.DTO.Menu.VariantDTO.ItemVariantUpdateRequest;
import pos.pos.Entity.Menu.ItemVariant;
import pos.pos.Entity.Menu.Menu;
import pos.pos.Entity.Menu.MenuItem;
import pos.pos.Entity.Menu.MenuSection;
import pos.pos.Exeption.AlreadyExistsException;
import pos.pos.Exeption.ItemVariantNotFound;
import pos.pos.Exeption.MenuItemException;
import pos.pos.Exeption.MenuSectionNotFound;
import pos.pos.Repository.Menu.ItemVariantRepository;
import pos.pos.Repository.Menu.MenuItemRepository;
import pos.pos.Service.Menu.ItemVariantServiceImpl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemVariantServiceImplTest {

    @Mock MenuItemRepository itemRepository;
    @Mock ItemVariantRepository variantRepository;
    @Mock ItemVariantMapper mapper;

    @InjectMocks
    ItemVariantServiceImpl service;

    private MenuItem item(Long menuId, Long sectionId, Long itemId) {
        Menu menu = new Menu(); menu.setId(menuId);
        MenuSection section = new MenuSection(); section.setId(sectionId); section.setMenu(menu);
        MenuItem it = new MenuItem(); it.setId(itemId); it.setSection(section);
        return it;
    }

    @Test
    void listVariants_ok() {
        Long menuId=1L, sectionId=2L, itemId=3L;
        when(itemRepository.findByIdAndSection_Id(itemId, sectionId)).thenReturn(Optional.of(item(menuId, sectionId, itemId)));
        ItemVariant v1=new ItemVariant(), v2=new ItemVariant();
        ItemVariantResponse r1=new ItemVariantResponse(10L,"S",BigDecimal.ONE,false,1,itemId, UUID.randomUUID());
        ItemVariantResponse r2=new ItemVariantResponse(11L,"M",BigDecimal.TWO,true,2,itemId, UUID.randomUUID());
        when(variantRepository.findByItem_IdOrderBySortOrderAscIdAsc(itemId)).thenReturn(List.of(v1, v2));
        when(mapper.toResponse(v1)).thenReturn(r1);
        when(mapper.toResponse(v2)).thenReturn(r2);

        var out = service.listVariants(menuId, sectionId, itemId);

        assertEquals(List.of(r1, r2), out);
    }

    @Test
    void listVariants_itemMismatch_throws() {
        Long menuId=1L, sectionId=2L, itemId=3L;
        MenuItem bad = item(9L, 99L, itemId);
        when(itemRepository.findByIdAndSection_Id(itemId, sectionId)).thenReturn(Optional.of(bad));
        assertThrows(MenuItemException.class, () -> service.listVariants(menuId, sectionId, itemId));
    }

    @Test
    void getVariant_ok() {
        Long menuId=1L, sectionId=2L, itemId=3L, variantId=4L;
        when(itemRepository.findByIdAndSection_Id(itemId, sectionId)).thenReturn(Optional.of(item(menuId, sectionId, itemId)));
        ItemVariant v = new ItemVariant(); v.setId(variantId);
        ItemVariantResponse resp = new ItemVariantResponse(variantId,"S",BigDecimal.ONE,false,1,itemId, UUID.randomUUID());
        when(variantRepository.findByIdAndItem_Id(variantId, itemId)).thenReturn(Optional.of(v));
        when(mapper.toResponse(v)).thenReturn(resp);

        var out = service.getVariant(menuId, sectionId, itemId, variantId);

        assertEquals(resp, out);
    }

    @Test
    void getVariant_notFound() {
        Long menuId=1L, sectionId=2L, itemId=3L, variantId=4L;
        when(itemRepository.findByIdAndSection_Id(itemId, sectionId)).thenReturn(Optional.of(item(menuId, sectionId, itemId)));
        when(variantRepository.findByIdAndItem_Id(variantId, itemId)).thenReturn(Optional.empty());
        assertThrows(ItemVariantNotFound.class, () -> service.getVariant(menuId, sectionId, itemId, variantId));
    }

    @Test
    void createVariant_ok_defaultClearsOthers() {
        Long menuId=1L, sectionId=2L, itemId=3L;
        MenuItem theItem = item(menuId, sectionId, itemId);
        when(itemRepository.findByIdAndSection_Id(itemId, sectionId)).thenReturn(Optional.of(theItem));

        ItemVariantCreateRequest req = new ItemVariantCreateRequest("S", BigDecimal.ONE, true, null);
        when(variantRepository.existsByItem_IdAndNameIgnoreCase(itemId, "S")).thenReturn(false);
        when(variantRepository.countByItem_Id(itemId)).thenReturn(0L);

        ItemVariant entity = new ItemVariant();
        entity.setItem(theItem);
        entity.setDefault(true);

        when(mapper.toEntity(eq(req), eq(theItem))).thenReturn(entity);

        ItemVariant saved = new ItemVariant();
        saved.setId(10L);
        saved.setSortOrder(1);
        saved.setDefault(true);

        ItemVariantResponse resp = new ItemVariantResponse(10L,"S",BigDecimal.ONE,true,1,itemId, UUID.randomUUID());

        when(variantRepository.save(entity)).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(resp);

        var out = service.createVariant(menuId, sectionId, itemId, req);

        assertEquals(resp, out);
        verify(variantRepository).clearDefaultForItem(itemId);
        verify(variantRepository).shiftRightFrom(itemId, 1);
    }


    @Test
    void createVariant_duplicateName() {
        Long menuId=1L, sectionId=2L, itemId=3L;
        when(itemRepository.findByIdAndSection_Id(itemId, sectionId)).thenReturn(Optional.of(item(menuId, sectionId, itemId)));
        when(variantRepository.existsByItem_IdAndNameIgnoreCase(itemId, "S")).thenReturn(true);
        ItemVariantCreateRequest req = new ItemVariantCreateRequest("S", BigDecimal.ONE, false, null);
        assertThrows(AlreadyExistsException.class, () -> service.createVariant(menuId, sectionId, itemId, req));
        verify(variantRepository, never()).save(any());
    }

    @Test
    void updateVariant_ok_nameChange_and_setDefault() {
        Long menuId=1L, sectionId=2L, itemId=3L, variantId=4L;
        when(itemRepository.findByIdAndSection_Id(itemId, sectionId)).thenReturn(Optional.of(item(menuId, sectionId, itemId)));
        ItemVariant entity = new ItemVariant(); entity.setId(variantId); entity.setName("Old"); entity.setSortOrder(2);
        when(variantRepository.findByIdAndItem_Id(variantId, itemId)).thenReturn(Optional.of(entity));
        when(variantRepository.existsByItem_IdAndNameIgnoreCase(itemId, "New")).thenReturn(false);
        ItemVariantUpdateRequest req = new ItemVariantUpdateRequest("New", BigDecimal.TEN, true);
        doAnswer(a -> { entity.setName("New"); entity.setDefault(true); return null; }).when(mapper).updateEntity(entity, req);
        ItemVariant saved = entity;
        ItemVariantResponse resp = new ItemVariantResponse(variantId,"New",BigDecimal.TEN,true,2,itemId, UUID.randomUUID());
        when(variantRepository.save(entity)).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(resp);

        var out = service.updateVariant(menuId, sectionId, itemId, variantId, req);

        assertEquals(resp, out);
        verify(variantRepository).clearDefaultForItem(itemId);
    }

    @Test
    void updateVariant_nameConflict() {
        Long menuId=1L, sectionId=2L, itemId=3L, variantId=4L;
        when(itemRepository.findByIdAndSection_Id(itemId, sectionId)).thenReturn(Optional.of(item(menuId, sectionId, itemId)));
        ItemVariant entity = new ItemVariant(); entity.setId(variantId); entity.setName("Old");
        when(variantRepository.findByIdAndItem_Id(variantId, itemId)).thenReturn(Optional.of(entity));
        when(variantRepository.existsByItem_IdAndNameIgnoreCase(itemId, "Old2")).thenReturn(true);

        assertThrows(AlreadyExistsException.class,
                () -> service.updateVariant(menuId, sectionId, itemId, variantId, new ItemVariantUpdateRequest("Old2", null, null)));
    }

    @Test
    void deleteVariant_ok_shiftsLeft() {
        Long menuId=1L, sectionId=2L, itemId=3L, variantId=4L;
        when(itemRepository.findByIdAndSection_Id(itemId, sectionId)).thenReturn(Optional.of(item(menuId, sectionId, itemId)));
        ItemVariant entity = new ItemVariant(); entity.setId(variantId); entity.setSortOrder(3);
        when(variantRepository.findByIdAndItem_Id(variantId, itemId)).thenReturn(Optional.of(entity));

        service.deleteVariant(menuId, sectionId, itemId, variantId);

        verify(variantRepository).delete(entity);
        verify(variantRepository).shiftLeftAfter(itemId, 3);
    }

    @Test
    void deleteVariant_notFound() {
        Long menuId=1L, sectionId=2L, itemId=3L, variantId=4L;
        when(itemRepository.findByIdAndSection_Id(itemId, sectionId)).thenReturn(Optional.of(item(menuId, sectionId, itemId)));
        when(variantRepository.findByIdAndItem_Id(variantId, itemId)).thenReturn(Optional.empty());
        assertThrows(ItemVariantNotFound.class, () -> service.deleteVariant(menuId, sectionId, itemId, variantId));
    }

    @Test
    void moveOne_up_ok() {
        Long menuId=1L, sectionId=2L, itemId=3L, variantId=4L;
        when(itemRepository.findByIdAndSection_Id(itemId, sectionId)).thenReturn(Optional.of(item(menuId, sectionId, itemId)));
        ItemVariant v = new ItemVariant(); v.setId(variantId); v.setSortOrder(2);
        when(variantRepository.findByIdAndItem_Id(variantId, itemId)).thenReturn(Optional.of(v));
        when(variantRepository.countByItem_Id(itemId)).thenReturn(5L);
        ItemVariant neighbor = new ItemVariant(); neighbor.setId(99L); neighbor.setSortOrder(1);
        when(variantRepository.findByItem_IdAndSortOrder(itemId, 1)).thenReturn(Optional.of(neighbor));
        when(variantRepository.findByIdAndItem_Id(variantId, itemId)).thenReturn(Optional.of(v));
        ItemVariantResponse resp = new ItemVariantResponse(variantId,"S",BigDecimal.ONE,false,1,itemId, UUID.randomUUID());
        when(mapper.toResponse(v)).thenReturn(resp);

        var out = service.moveOne(menuId, sectionId, itemId, variantId, -1);

        assertEquals(resp, out);
        verify(variantRepository).updateSortOrder(itemId, variantId, -variantId.intValue());
        verify(variantRepository).updateSortOrder(itemId, neighbor.getId(), 2);
        verify(variantRepository).updateSortOrder(itemId, variantId, 1);
    }

    @Test
    void moveOne_outOfBounds_noop() {
        Long menuId=1L, sectionId=2L, itemId=3L, variantId=4L;
        when(itemRepository.findByIdAndSection_Id(itemId, sectionId)).thenReturn(Optional.of(item(menuId, sectionId, itemId)));
        ItemVariant v = new ItemVariant(); v.setId(variantId); v.setSortOrder(1);
        when(variantRepository.findByIdAndItem_Id(variantId, itemId)).thenReturn(Optional.of(v));
        when(variantRepository.countByItem_Id(itemId)).thenReturn(1L);
        ItemVariantResponse resp = new ItemVariantResponse(variantId,"S",BigDecimal.ONE,false,1,itemId, UUID.randomUUID());
        when(mapper.toResponse(v)).thenReturn(resp);

        var out = service.moveOne(menuId, sectionId, itemId, variantId, -1);

        assertEquals(resp, out);
        verify(variantRepository, never()).updateSortOrder(anyLong(), anyLong(), anyInt());
    }

    @Test
    void loadItemOrThrow_sectionMissing_throws() {
        when(itemRepository.findByIdAndSection_Id(3L, 2L)).thenReturn(Optional.empty());
        assertThrows(MenuSectionNotFound.class, () -> service.listVariants(1L, 2L, 3L));
    }

    @Test
    void moveOne_neighborMissing_noop() {
        Long menuId=1L, sectionId=2L, itemId=3L, variantId=4L;
        when(itemRepository.findByIdAndSection_Id(itemId, sectionId))
                .thenReturn(Optional.of(item(menuId, sectionId, itemId)));
        ItemVariant v = new ItemVariant(); v.setId(variantId); v.setSortOrder(2);
        when(variantRepository.findByIdAndItem_Id(variantId, itemId)).thenReturn(Optional.of(v));
        when(variantRepository.countByItem_Id(itemId)).thenReturn(5L);
        when(variantRepository.findByItem_IdAndSortOrder(itemId, 1)).thenReturn(Optional.empty());
        ItemVariantResponse resp = new ItemVariantResponse(variantId,"S",BigDecimal.ONE,false,2,itemId, UUID.randomUUID());
        when(mapper.toResponse(v)).thenReturn(resp);

        var out = service.moveOne(menuId, sectionId, itemId, variantId, -1);

        assertEquals(resp, out);
        verify(variantRepository, never()).updateSortOrder(anyLong(), anyLong(), anyInt());
    }

    @Test
    void createVariant_duplicateName_caseInsensitive() {
        Long menuId=1L, sectionId=2L, itemId=3L;
        when(itemRepository.findByIdAndSection_Id(itemId, sectionId))
                .thenReturn(Optional.of(item(menuId, sectionId, itemId)));
        when(variantRepository.existsByItem_IdAndNameIgnoreCase(itemId, "small")).thenReturn(true);

        ItemVariantCreateRequest req = new ItemVariantCreateRequest("Small", BigDecimal.ONE, false, null);

        assertThrows(AlreadyExistsException.class, () -> service.createVariant(menuId, sectionId, itemId, req));
        verify(variantRepository, never()).save(any());
    }

    @Test
    void updateVariant_setDefaultFalse_doesNotClearOthers() {
        Long menuId=1L, sectionId=2L, itemId=3L, variantId=4L;
        when(itemRepository.findByIdAndSection_Id(itemId, sectionId))
                .thenReturn(Optional.of(item(menuId, sectionId, itemId)));
        ItemVariant entity = new ItemVariant(); entity.setId(variantId); entity.setName("Old"); entity.setDefault(false); entity.setSortOrder(2);
        when(variantRepository.findByIdAndItem_Id(variantId, itemId)).thenReturn(Optional.of(entity));
        ItemVariantUpdateRequest req = new ItemVariantUpdateRequest("Old", BigDecimal.ONE, false);
        doAnswer(a -> null).when(mapper).updateEntity(entity, req);
        when(variantRepository.save(entity)).thenReturn(entity);
        ItemVariantResponse resp = new ItemVariantResponse(variantId,"Old",BigDecimal.ONE,false,2,itemId, UUID.randomUUID());
        when(mapper.toResponse(entity)).thenReturn(resp);

        var out = service.updateVariant(menuId, sectionId, itemId, variantId, req);

        assertEquals(resp, out);
        verify(variantRepository, never()).clearDefaultForItem(anyLong());
    }

}
