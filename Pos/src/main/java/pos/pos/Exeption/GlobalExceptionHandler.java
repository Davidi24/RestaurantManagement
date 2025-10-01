package pos.pos.Exeption;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import pos.pos.Exeption.General.AlreadyExistsException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // --- 404 Not Found: unknown route or missing static resource ----------------
    @ExceptionHandler({ NoHandlerFoundException.class, NoResourceFoundException.class })
    public ResponseEntity<Map<String, Object>> handleNoHandler(Exception ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "error", "NotFound",
                        "message", "API does not exist",
                        "path", req.getRequestURI()
                ));
    }

    // --- 409 Conflict -------------------------------------------------------
    @ExceptionHandler({
            AlreadyExistsException.class,
            InvalidOrderStateException.class,
            LineItemOrderMismatchException.class,
            OpenOrderExistsException.class,
    })
    public ResponseEntity<?> handleConflict(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "error", ex.getClass().getSimpleName(),
                        "message", ex.getMessage()
                ));
    }

    // --- 403 Forbidden (authorization) --------------------------------------
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of(
                        "error", "AccessDenied",
                        "message", "You do not have permission to perform this action"
                ));
    }

    // --- 401 Unauthorized (authentication) ----------------------------------
    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<?> handleAuthMissing(AuthenticationCredentialsNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "error", "Unauthorized",
                        "message", ex.getMessage()
                ));
    }

    // --- 400 Bad Request (bean validation @Valid) ---------------------------
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(err -> {
            String field = (err instanceof FieldError fe) ? fe.getField() : err.getObjectName();
            String message = err.getDefaultMessage();
            errors.put(field, message);
        });
        return ResponseEntity.badRequest().body(Map.of(
                "error", "ValidationFailed",
                "details", errors
        ));
    }

    // --- 400 Bad Request (enum / JSON binding) ------------------------------
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleEnumOrJsonBinding(HttpMessageNotReadableException ex) {
        if (ex.getCause() instanceof InvalidFormatException ife && ife.getTargetType().isEnum()) {
            String fieldName = ife.getPath().isEmpty() ? "unknown" : ife.getPath().getFirst().getFieldName();
            String allowed = String.join(",",
                    java.util.Arrays.stream(ife.getTargetType().getEnumConstants())
                            .map(Object::toString).toList());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "InvalidEnumValue",
                    "field", fieldName,
                    "allowed", allowed
            ));
        }
        ex.getMostSpecificCause();
        return ResponseEntity.badRequest().body(Map.of(
                "error", "BadRequest",
                "message", ex.getMostSpecificCause().getMessage()
        ));
    }

    // --- 400 Bad Request (type mismatch, e.g., invalid UUID in path/query) ---
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String name = ex.getName();
        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        return ResponseEntity.badRequest().body(Map.of(
                "error", "TypeMismatch",
                "field", name,
                "expected", requiredType,
                "message", ex.getMessage()
        ));
    }

    // --- 400 Bad Request (your domain validation / mismatches / missing fields)
    @ExceptionHandler({
            MissingFieldException.class,
            VariantMenuMismatchException.class,
            OptionMenuMismatchException.class,
            IllegalArgumentException.class // fallback for any remaining validation throws
    })
    public ResponseEntity<?> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "error", ex.getClass().getSimpleName(),
                "message", ex.getMessage()
        ));
    }

    // --- 404 Not Found -------------------------------------------------------
    @ExceptionHandler({
            MenuNotFoundException.class,
            MenuSectionNotFound.class,
            MenuItemException.class,
            ItemVariantNotFound.class,
            OrderNotFound.class,
            OrderItemNotFound.class,
            MenuItemNotFoundException.class,
            ItemVariantNotFoundException.class,
            OptionItemNotFoundExceptionUDD.class,
            OptionGroupNotFoundException.class
    })
    public ResponseEntity<Map<String, String>> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "error", ex.getClass().getSimpleName().replace("Exception", ""),
                        "message", ex.getMessage()
                ));
    }

    // --- 500 Internal Server Error (catch-all) -------------------------------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleUnexpected(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "error", "InternalServerError",
                        "message", "An unexpected error occurred"
                ));
    }
}
