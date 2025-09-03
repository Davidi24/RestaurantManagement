package pos.pos.Exeption;

public class MissingFieldException extends RuntimeException {
    public MissingFieldException(String field) {
        super(field + " is required");
    }
}
