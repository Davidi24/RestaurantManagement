package pos.pos.Service.Interfecaes.Inventory;

public interface InventoryConsumptionService {
    void consumeForOrderLineItem(Long orderLineItemId);
    void consumeForOrder(Long orderId);
    void consumeForMenuItem(Long menuItemId, int quantity);
}
