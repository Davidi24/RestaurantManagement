package pos.pos.Exeption.Recipe;

public class IngridientAlreadyExist extends RuntimeException {
    public IngridientAlreadyExist(Long id) {
      super("Ingridient with id " + id + " already exist");
    }
}
