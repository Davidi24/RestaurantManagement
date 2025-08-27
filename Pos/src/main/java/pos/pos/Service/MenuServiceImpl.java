package pos.pos.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pos.pos.DTO.Mapper.MenuMapper;
import pos.pos.DTO.Menu.MenuDTO.MenuRequest;
import pos.pos.DTO.Menu.MenuDTO.MenuResponse;
import pos.pos.DTO.Menu.MenuDTO.MenuTreeResponse;
import pos.pos.Entity.Menu.Menu;
import pos.pos.Exeption.MenuNotFoundException;
import pos.pos.Repository.MenuRepository;
import pos.pos.Service.Interfecaes.MenuService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MenuServiceImpl implements MenuService {

    private final MenuRepository menuRepo;
    private final MenuMapper menuMapper;

    @Override
    public MenuResponse create(MenuRequest body) {
        var menu = Menu.builder()
                .name(body.name())
                .description(body.description())
                .build();
        return menuMapper.toResponse(menuRepo.save(menu));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuResponse> list() {
        return menuRepo.findAll()
                .stream()
                .map(menuMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MenuResponse get(Long id) {
        var menu = menuRepo.findById(id)
                .orElseThrow(() -> new MenuNotFoundException(id));
        return menuMapper.toResponse(menu);
    }

    @Override
    public MenuResponse patch(Long id, MenuRequest body) {
        var menu = menuRepo.findById(id)
                .orElseThrow(() -> new MenuNotFoundException(id));

        if (body.name() != null) {
            menu.setName(body.name());
        }
        if (body.description() != null) {
            menu.setDescription(body.description());
        }

        return menuMapper.toResponse(menuRepo.save(menu));
    }

    @Override
    public void delete(Long id) {
        if (!menuRepo.existsById(id)) {
            throw new MenuNotFoundException(id);
        }
        menuRepo.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public MenuTreeResponse tree(Long id) {
        var menu = menuRepo.findWithTreeById(id)
                .orElseThrow(() -> new MenuNotFoundException(id));
        return menuMapper.toTreeResponse(menu);
    }
}
