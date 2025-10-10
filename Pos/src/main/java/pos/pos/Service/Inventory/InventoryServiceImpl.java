package pos.pos.Service.Inventory;

import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pos.pos.DTO.Inventory.InventoryItemRequest;
import pos.pos.DTO.Inventory.InventoryItemResponse;
import pos.pos.DTO.Inventory.MovementRequest;
import pos.pos.DTO.Inventory.MovementResponse;
import pos.pos.DTO.Mapper.Inventory.InventoryMapper;
import pos.pos.Entity.Inventory.InventoryItem;
import pos.pos.Entity.Inventory.MovementType;
import pos.pos.Entity.Inventory.StockMovement;
import pos.pos.Exeption.General.AlreadyExistsException;
import pos.pos.Exeption.General.NotFoundException;
import pos.pos.Repository.Inventory.InventoryItemRepository;
import pos.pos.Repository.Inventory.StockMovementRepository;
import pos.pos.Service.Interfecaes.Inventory.InventoryService;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryServiceImpl implements InventoryService {

    private final InventoryItemRepository itemRepo;
    private final StockMovementRepository moveRepo;
    private final InventoryMapper mapper;

    @Override
    @Transactional
    public InventoryItemResponse createItem(InventoryItemRequest request) {
        if (itemRepo.existsByIngredient_Id(request.ingredientId()))
            throw new AlreadyExistsException("InventoryItem for ingredient", String.valueOf(request.ingredientId()));
        InventoryItem saved = itemRepo.save(mapper.toEntity(request));
        BigDecimal qty = moveRepo.computeCurrentQuantity(saved.getId());
        return mapper.toResponse(saved, qty);
    }

    @Override
    @Transactional
    public InventoryItemResponse updateItem(Long id, InventoryItemRequest request) {
        InventoryItem existing = itemRepo.findById(id).orElseThrow(() -> new NotFoundException("InventoryItem not found"));
        mapper.updateEntity(existing, request);
        InventoryItem saved = itemRepo.save(existing);
        BigDecimal qty = moveRepo.computeCurrentQuantity(saved.getId());
        return mapper.toResponse(saved, qty);
    }

    @Override
    @Transactional
    public void deleteItem(Long id) {
        if (!itemRepo.existsById(id)) throw new NotFoundException("InventoryItem not found");
        itemRepo.deleteById(id);
    }

    @Override
    public InventoryItemResponse getItem(Long id) {
        InventoryItem item = itemRepo.findById(id).orElseThrow(() -> new NotFoundException("InventoryItem not found"));
        BigDecimal qty = moveRepo.computeCurrentQuantity(id);
        return mapper.toResponse(item, qty);
    }

    @Override
    public Page<InventoryItemResponse> listItems(String q, Pageable pageable) {
        Page<InventoryItem> page = itemRepo.findAll(pageable);
        return page.map(i -> mapper.toResponse(i, moveRepo.computeCurrentQuantity(i.getId())));
    }

    @Override
    @Transactional
    public MovementResponse move(Long itemId, MovementRequest request, String createdBy) throws BadRequestException {
        InventoryItem item = itemRepo.findById(itemId).orElseThrow(() -> new NotFoundException("InventoryItem not found"));
        if (request.type() == MovementType.OUT) {
            BigDecimal current = moveRepo.computeCurrentQuantity(itemId);
            if (current.subtract(request.quantity()).compareTo(BigDecimal.ZERO) < 0)
                throw new BadRequestException("Insufficient stock");
        }
        StockMovement saved = moveRepo.save(mapper.toMovement(item, request, createdBy));
        return mapper.toResponse(saved);
    }

    @Override
    public Page<MovementResponse> listMovements(Long itemId, OffsetDateTime from, OffsetDateTime to, Pageable pageable) {
        if (!itemRepo.existsById(itemId)) throw new NotFoundException("InventoryItem not found");
        return (from != null && to != null)
                ? moveRepo.findByInventoryItem_IdAndOccurredAtBetween(itemId, from, to, pageable).map(mapper::toResponse)
                : moveRepo.findByInventoryItem_Id(itemId, pageable).map(mapper::toResponse);
    }
}
