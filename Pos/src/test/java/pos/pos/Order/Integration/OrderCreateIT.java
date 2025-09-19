package pos.pos.Order.Integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import pos.pos.Config.ApiPaths;
import pos.pos.Entity.Order.OrderStatus;
import pos.pos.Order.Integration.support.AbstractOrderIT;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Creation-focused integration tests.
 *
 * Covered cases:
 * 1) Happy path create (OPEN, retrievable)
 * 2) Same-table open-order constraint (second create → 409)
 * 3) Validation errors (e.g., numberOfGuests < 1 → 400)
 * 4) Unauthorized (no JWT) → 401
 * 5) Revoked token (Authorization header Bearer ... + mocked as revoked) → 401
 */
class OrderCreateIT extends AbstractOrderIT {

    /** Happy path: POST /orders creates an OPEN order retrievable by id. */
    @Test
    @DisplayName("test01_create_order_minimal: POST /orders creates an OPEN order retrievable by id")
    void test01_create_order_minimal() throws Exception {
        long orderId = createOrder(nextTableId(), 2);

        mvc.perform(get(ApiPaths.Order.BASE + "/" + orderId).with(jwtAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value((int) orderId))
                .andExpect(jsonPath("$.status").value(OrderStatus.OPEN.name()));
    }

    /** Business rule: only one OPEN order per table. Second create on same table must 409. */
    @Test
    @DisplayName("test02_same_table_open_order_conflict: second OPEN order on same table → 409")
    void test02_same_table_open_order_conflict() throws Exception {
        long tableId = nextTableId();

        // First create succeeds
        createOrder(tableId, 2);

        // Second create on SAME table should fail with 409 (OpenOrderExistsException)
        String body = """
            {
              "tableId": %d,
              "numberOfGuests": 3
            }
            """.formatted(tableId);

        mvc.perform(post(ApiPaths.Order.BASE)
                        .with(jwtAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict()) // 409
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.error", anyOf(
                        equalTo("OpenOrderExistsException"),
                        // tolerate different error mappers as long as message hints at "open"
                        containsStringIgnoringCase("open")
                )));
    }

    /** Bean validation: numberOfGuests and tableId must be >= 1. Expect 400 on invalid input. */
    @Test
    @DisplayName("test03_validation_errors: guests/tableId < 1 → 400 Bad Request")
    void test03_validation_errors() throws Exception {
        // guests = 0 violates @Min(1)
        String invalidGuests = """
            {
              "tableId": %d,
              "numberOfGuests": 0
            }
            """.formatted(nextTableId());

        mvc.perform(post(ApiPaths.Order.BASE)
                        .with(jwtAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidGuests))
                .andExpect(status().isBadRequest());

        // tableId = 0 violates @Min(1)
        String invalidTable = """
            {
              "tableId": 0,
              "numberOfGuests": 2
            }
            """;

        mvc.perform(post(ApiPaths.Order.BASE)
                        .with(jwtAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidTable))
                .andExpect(status().isBadRequest());
    }

    /** Security: endpoint requires authentication. No JWT → 401 Unauthorized. */
    @Test
    @DisplayName("test04_unauthorized_without_jwt: no auth → 401")
    void test04_unauthorized_without_jwt() throws Exception {
        String body = """
            {
              "tableId": %d,
              "numberOfGuests": 2
            }
            """.formatted(nextTableId());

        mvc.perform(post(ApiPaths.Order.BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Security filter: if an Authorization: Bearer token is present and is revoked,
     * RevokedTokenFilter must block with 401.
     * (We still attach a jwt() to satisfy method security; filter reads the header.)
     */
    @Test
    @DisplayName("test05_revoked_token_returns_401")
    void test05_revoked_token_returns_401() throws Exception {
        String revoked = "revoked-token-123";
        when(jwtService.isTokenRevoked(revoked)).thenReturn(true);

        String body = """
            {
              "tableId": %d,
              "numberOfGuests": 2
            }
            """.formatted(nextTableId());

        mvc.perform(post(ApiPaths.Order.BASE)
                        .with(jwtAdmin()) // method security passes
                        .header("Authorization", "Bearer " + revoked) // but filter blocks it
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.error", anyOf(
                        equalTo("Token revoked"),
                        containsStringIgnoringCase("revoked")
                )));
    }
}
