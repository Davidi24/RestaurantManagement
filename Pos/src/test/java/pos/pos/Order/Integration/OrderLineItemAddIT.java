package pos.pos.Order.Integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import pos.pos.Config.ApiPaths;
import pos.pos.Order.Integration.support.AbstractOrderIT;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Line-item add/merge behavior:
 * 1) Add a line with variant + options → correct unit/subtotal
 * 2) Add the same spec again → merges and increases quantity, not a new line
 * 3) Add a different spec (different options set) → creates a new line
 */
class OrderLineItemAddIT extends AbstractOrderIT {

    /**
     * Adds a line with variant + two options.
     * Verifies unit = variantOverride (not base) + options; subtotal = unit * qty; totals reflect it.
     */
    @Test
    @DisplayName("test01_add_line_item_variant_options: correct unit/subtotal and totals")
    void test01_add_line_item_variant_options() throws Exception {
        long orderId = createOrder(nextTableId(), 2);

        // Seed: base=10, variant=12, options=+2 and +3 → unit should be 12 + (2+3) = 17
        UUID[] pids = seedCatalog(10.0, 12.0, 2.0, 3.0);
        UUID itemPid = pids[0], variantPid = pids[1], optA = pids[2], optB = pids[3];

        String add = """
            { 
              "menuItemPublicId":"%s",
              "quantity":2,
              "variantPublicId":"%s",
              "optionPublicIds":["%s","%s"]
            }
            """.formatted(itemPid, variantPid, optA, optB);

        mvc.perform(post(ApiPaths.Order.LINE_ITEMS.replace("{orderId}", String.valueOf(orderId)))
                        .with(jwtAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(add))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unitPrice").value(12.0))  // variant override
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.lineSubtotal").value(34.0)); // (12+(2+3))*2 = 34

        // Order totals should reflect the single line
        mvc.perform(get(ApiPaths.Order.BASE + "/" + orderId).with(jwtAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grandTotal").value(34.0))
                .andExpect(jsonPath("$.balanceDue").value(34.0));
    }

    /**
     * Adding the exact same spec again merges into the same line (quantity increases).
     */
    @Test
    @DisplayName("test02_merge_same_spec_increases_qty: adding same spec merges instead of new line")
    void test02_merge_same_spec_increases_qty() throws Exception {
        long orderId = createOrder(nextTableId(), 2);

        // Seed: base=9, variant=11, options=+1 and +0.5 → unit = 11 + 1 + 0.5 = 12.5
        UUID[] pids = seedCatalog(9.0, 11.0, 1.0, 0.5);
        UUID itemPid = pids[0], variantPid = pids[1], optA = pids[2], optB = pids[3];

        String add = """
            {
              "menuItemPublicId":"%s",
              "quantity":2,
              "variantPublicId":"%s",
              "optionPublicIds":["%s","%s"]
            }
            """.formatted(itemPid, variantPid, optA, optB);

        // First add → qty 2, subtotal = 25.0
        var r1 = mvc.perform(post(ApiPaths.Order.LINE_ITEMS.replace("{orderId}", String.valueOf(orderId)))
                        .with(jwtAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(add))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.lineSubtotal").value(25.0)) // 12.5 * 2
                .andReturn();

        // Second add of SAME spec → should MERGE: qty 4, subtotal = 50.0 (id stays same)
        mvc.perform(post(ApiPaths.Order.LINE_ITEMS.replace("{orderId}", String.valueOf(orderId)))
                        .with(jwtAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(add))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(4))
                .andExpect(jsonPath("$.lineSubtotal").value(50.0));

        // Totals reflect merged line
        mvc.perform(get(ApiPaths.Order.BASE + "/" + orderId).with(jwtAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grandTotal").value(50.0))
                .andExpect(jsonPath("$.balanceDue").value(50.0));
    }

    /**
     * Same item/variant but a different options set → new line (no merge).
     */
    @Test
    @DisplayName("test03_add_different_spec_creates_new_line: different options produce a distinct line")
    void test03_add_different_spec_creates_new_line() throws Exception {
        long orderId = createOrder(nextTableId(), 2);

        // Seed: base=10, variant=12, options=+2 and +3
        UUID[] pids = seedCatalog(10.0, 12.0, 2.0, 3.0);
        UUID itemPid = pids[0], variantPid = pids[1], optA = pids[2], optB = pids[3];

        String addAB = """
            {
              "menuItemPublicId":"%s",
              "quantity":1,
              "variantPublicId":"%s",
              "optionPublicIds":["%s","%s"]
            }
            """.formatted(itemPid, variantPid, optA, optB);

        String addAonly = """
            {
              "menuItemPublicId":"%s",
              "quantity":1,
              "variantPublicId":"%s",
              "optionPublicIds":["%s"]
            }
            """.formatted(itemPid, variantPid, optA);

        // First line with A+B → unit 17, subtotal 17
        mvc.perform(post(ApiPaths.Order.LINE_ITEMS.replace("{orderId}", String.valueOf(orderId)))
                        .with(jwtAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addAB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lineSubtotal").value(17.0));

        // Second line with only A → unit 14, subtotal 14 → NEW LINE, totals 31
        mvc.perform(post(ApiPaths.Order.LINE_ITEMS.replace("{orderId}", String.valueOf(orderId)))
                        .with(jwtAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addAonly))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lineSubtotal").value(14.0));

        mvc.perform(get(ApiPaths.Order.BASE + "/" + orderId).with(jwtAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grandTotal").value(31.0))
                .andExpect(jsonPath("$.balanceDue").value(31.0));
    }
}
