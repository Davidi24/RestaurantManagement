package pos.pos.Service.Interfecaes.Menu;



import pos.pos.DTO.Menu.OptionDTO.OptionItemCreateRequest;
import pos.pos.DTO.Menu.OptionDTO.OptionItemResponse;
import pos.pos.DTO.Menu.OptionDTO.OptionItemUpdateRequest;

import java.util.List;

public interface OptionItemService {

    List<OptionItemResponse> list(Long menuId, Long sectionId, Long itemId, Long groupId);

    OptionItemResponse create(Long menuId, Long sectionId, Long itemId, Long groupId, OptionItemCreateRequest body);

    OptionItemResponse get(Long menuId, Long sectionId, Long itemId, Long groupId, Long optionId);

    OptionItemResponse patch(Long menuId, Long sectionId, Long itemId, Long groupId, Long optionId, OptionItemUpdateRequest body);

    void delete(Long menuId, Long sectionId, Long itemId, Long groupId, Long optionId);

    OptionItemResponse moveOne(Long menuId, Long sectionId, Long itemId, Long groupId, Long optionId, int direction);
}
