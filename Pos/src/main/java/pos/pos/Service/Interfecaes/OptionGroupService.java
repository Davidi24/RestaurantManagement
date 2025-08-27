package pos.pos.Service.Interfecaes;

import pos.pos.DTO.Menu.OptionDTO.OptionGroupCreateRequest;
import pos.pos.DTO.Menu.OptionDTO.OptionGroupResponse;
import pos.pos.DTO.Menu.OptionDTO.OptionGroupUpdateRequest;

import java.util.List;

public interface OptionGroupService {
    List<OptionGroupResponse> listByItem(Long menuId, Long sectionId, Long itemId);
    OptionGroupResponse create(Long menuId, Long sectionId, Long itemId, OptionGroupCreateRequest body);
    OptionGroupResponse getById(Long menuId, Long sectionId, Long itemId, Long groupId);
    OptionGroupResponse patch(Long menuId, Long sectionId, Long itemId, Long groupId, OptionGroupUpdateRequest body);
    void delete(Long menuId, Long sectionId, Long itemId, Long groupId);
}

