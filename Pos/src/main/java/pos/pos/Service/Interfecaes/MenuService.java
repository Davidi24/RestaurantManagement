package pos.pos.Service.Interfecaes;

import pos.pos.DTO.Menu.MenuDTO.MenuRequest;
import pos.pos.DTO.Menu.MenuDTO.MenuResponse;
import pos.pos.DTO.Menu.MenuDTO.MenuTreeResponse;

import java.util.List;

public interface MenuService {
    MenuResponse create(MenuRequest body);
    List<MenuResponse> list();
    MenuResponse get(Long id);
    MenuResponse patch(Long id, MenuRequest body);
    void delete(Long id);
    MenuTreeResponse tree(Long id);
}
