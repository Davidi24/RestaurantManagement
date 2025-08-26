package pos.pos.Exeption;

import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler({AlreadyExistsException.class , })
  public ResponseEntity<?> handleEmail(AlreadyExistsException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(Map.of("error", ex.getMessage()));
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<?> handleAccessDenied(AccessDeniedException ex) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(Map.of("error", "You do not have permission to perform this action"));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();

    ex.getBindingResult().getAllErrors().forEach(err -> {
      String field = ((FieldError) err).getField();
      String message = err.getDefaultMessage();
      errors.put(field, message);
    });

    return ResponseEntity.badRequest().body(errors);
  }


  @ExceptionHandler({
          MenuNotFoundException.class,
          MenuSectionNotFound.class,
          MenuItemExeption.class,
  })
  public ResponseEntity<Map<String, String>> handleNotFound(RuntimeException ex) {
    Map<String, String> error = new HashMap<>();
    error.put("error", ex.getClass().getSimpleName().replace("Exception", ""));
    error.put("message", ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

}
