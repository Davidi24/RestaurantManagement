package pos.pos.Service.Interfecaes.Menu;

import pos.pos.DTO.Menu.VariantDTO.ItemVariantCreateRequest;
import pos.pos.DTO.Menu.VariantDTO.ItemVariantResponse;
import pos.pos.DTO.Menu.VariantDTO.ItemVariantUpdateRequest;

import java.util.List;

public interface ItemVariantService {

    List<ItemVariantResponse> listVariants(Long menuId, Long sectionId, Long itemId);

    ItemVariantResponse getVariant(Long menuId, Long sectionId, Long itemId, Long variantId);

    ItemVariantResponse createVariant(Long menuId, Long sectionId, Long itemId, ItemVariantCreateRequest request);

    ItemVariantResponse updateVariant(Long menuId, Long sectionId, Long itemId, Long variantId, ItemVariantUpdateRequest request);

    void deleteVariant(Long menuId, Long sectionId, Long itemId, Long variantId);

    ItemVariantResponse moveOne(Long menuId, Long sectionId, Long itemId, Long variantId, int direction);

}
