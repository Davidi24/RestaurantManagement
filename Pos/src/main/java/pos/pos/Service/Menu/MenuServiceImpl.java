package pos.pos.Service.Menu;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pos.pos.DTO.Mapper.MenuMapper.MenuMapper;
import pos.pos.DTO.Menu.MenuDTO.MenuRequest;
import pos.pos.DTO.Menu.MenuDTO.MenuResponse;
import pos.pos.DTO.Menu.MenuDTO.MenuTreeResponse;
import pos.pos.Exeption.General.AlreadyExistsException;
import pos.pos.Exeption.MenuNotFoundException;
import pos.pos.Repository.Menu.MenuRepository;
import pos.pos.Service.Interfecaes.Menu.MenuService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MenuServiceImpl implements MenuService {

    private final MenuRepository menuRepo;
    private final MenuMapper menuMapper;

    @Override
    public MenuResponse create(MenuRequest menuRequest) {
        if (menuRepo.existsByName(menuRequest.name())) {
            throw new AlreadyExistsException("Menu: ", menuRequest.name());
        }
        var menu = menuMapper.toMenu(menuRequest);
        return menuMapper.toMenuResponse(menuRepo.save(menu));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuResponse> list() {
        return menuRepo.findAll()
                .stream()
                .map(menuMapper::toMenuResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MenuResponse get(Long id) {
        var menu = menuRepo.findById(id)
                .orElseThrow(() -> new MenuNotFoundException(id));
        return menuMapper.toMenuResponse(menu);
    }

    @Override
    public MenuResponse patch(Long id, MenuRequest body) {
        var menu = menuRepo.findById(id).orElseThrow(() -> new MenuNotFoundException(id));

        if (body.name() != null) {
            var name = body.name().trim();
            if (!name.isEmpty()) menu.setName(name);
        }
        if (body.description() != null) {
            // allows empty description but not null
            var desc = body.description().trim();
            menu.setDescription(desc.isEmpty() ? null : desc);
        }
        return menuMapper.toMenuResponse(menuRepo.save(menu));
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
        return menuMapper.toMenuTreeResponse(menu);
    }
}