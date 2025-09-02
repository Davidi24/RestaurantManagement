package pos.pos.Exeption;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler({AlreadyExistsException.class, InvalidOrderStateException.class})
  public ResponseEntity<?> handleConflict(RuntimeException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(Map.of("error", ex.getClass().getSimpleName(), "message", ex.getMessage()));
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



  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<?> handleEnumBindingError(HttpMessageNotReadableException ex) {
    if (ex.getCause() instanceof InvalidFormatException ife
            && ife.getTargetType().isEnum()) {
      String fieldName = ife.getPath().isEmpty() ? "unknown" : ife.getPath().getFirst().getFieldName();
      return ResponseEntity.badRequest().body(
              Map.of(
                      "error", fieldName + " is not supported"
              )
      );
    }
    return ResponseEntity.badRequest().body(
            Map.of("error", "BadRequest", "message", ex.getMessage()));
  }


  @ExceptionHandler({
          MenuNotFoundException.class,
          MenuSectionNotFound.class,
          MenuItemException.class,
          ItemVariantNotFound.class,
          OrderNotFound.class
  })
  public ResponseEntity<Map<String, String>> handleNotFound(RuntimeException ex) {
    Map<String, String> error = new HashMap<>();
    error.put("error", ex.getClass().getSimpleName().replace("Exception", ""));
    error.put("message", ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

}
