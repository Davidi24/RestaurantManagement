package pos.pos.Exeption.Inventory;

public class InventoryNotFoundExeption extends RuntimeException {
    public InventoryNotFoundExeption(Long id) {
        super("Ingridient with id " + id + " not found");
    }
}
