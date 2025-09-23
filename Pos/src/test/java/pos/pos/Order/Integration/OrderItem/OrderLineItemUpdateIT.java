package pos.pos.Order.Integration.OrderItem;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import pos.pos.Config.ApiPaths;
import pos.pos.Order.Integration.support.AbstractOrderIT;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class OrderLineItemUpdateIT extends AbstractOrderIT {

    @Test
    @DisplayName("update_quantity_recalculates_totals")
    void update_quantity_recalculates_totals() throws Exception {
        long orderId = createOrder(nextTableId(), 2);
        UUID[] pids = seedCatalog(10.0, 12.0, 2.0, 3.0);
        UUID itemPid = pids[0], variantPid = pids[1], optA = pids[2], optB = pids[3];

        String add = """
          {
            "menuItemPublicId":"%s",
            "quantity":2,
            "variantPublicId":"%s",
            "optionPublicIds":["%s","%s"],
            "notes":"hot"
          }
        """.formatted(itemPid, variantPid, optA, optB);

        var r1 = mvc.perform(post(ApiPaths.Order.LINE_ITEMS.replace("{orderId}", String.valueOf(orderId)))
                        .with(jwtAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(add))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lineSubtotal").value(34.0))
                .andReturn();

        long lineId = JsonPath.parse(r1.getResponse().getContentAsString()).read("$.id", Long.class);

        String patch = """
          {
            "id": %d,
            "quantity": 3
          }
        """.formatted(lineId);

        mvc.perform(patch(ApiPaths.Order.LINE_ITEMS.replace("{orderId}", String.valueOf(orderId)))
                        .with(jwtAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patch))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(3))
                .andExpect(jsonPath("$.lineSubtotal").value(51.0));

        mvc.perform(get(ApiPaths.Order.BASE + "/" + orderId).with(jwtAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grandTotal").value(51.0));
    }

    @Test
    @DisplayName("update_notes_only_does_not_change_totals")
    void update_notes_only_does_not_change_totals() throws Exception {
        long orderId = createOrder(nextTableId(), 2);
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

        var r1 = mvc.perform(post(ApiPaths.Order.LINE_ITEMS.replace("{orderId}", String.valueOf(orderId)))
                        .with(jwtAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(add))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lineSubtotal").value(34.0))
                .andReturn();

        long lineId = JsonPath.parse(r1.getResponse().getContentAsString()).read("$.id", Long.class);

        String patch = """
          {
            "id": %d,
            "quantity": 2,
            "notes": "no onions"
          }
        """.formatted(lineId);

        mvc.perform(patch(ApiPaths.Order.LINE_ITEMS.replace("{orderId}", String.valueOf(orderId)))
                        .with(jwtAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patch))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notes").value("no onions"))
                .andExpect(jsonPath("$.lineSubtotal").value(34.0));

        mvc.perform(get(ApiPaths.Order.BASE + "/" + orderId).with(jwtAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grandTotal").value(34.0));
    }

    @Test
    @DisplayName("update_no_change_returns_same")
    void update_no_change_returns_same() throws Exception {
        long orderId = createOrder(nextTableId(), 2);
        UUID[] pids = seedCatalog(10.0, 12.0, 2.0, 3.0);
        UUID itemPid = pids[0], variantPid = pids[1], optA = pids[2], optB = pids[3];

        String add = """
          {
            "menuItemPublicId":"%s",
            "quantity":2,
            "variantPublicId":"%s",
            "optionPublicIds":["%s","%s"],
            "notes":"keep same"
          }
        """.formatted(itemPid, variantPid, optA, optB);

        var r1 = mvc.perform(post(ApiPaths.Order.LINE_ITEMS.replace("{orderId}", String.valueOf(orderId)))
                        .with(jwtAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(add))
                .andExpect(status().isOk())
                .andReturn();

        long lineId = JsonPath.parse(r1.getResponse().getContentAsString()).read("$.id", Long.class);

        String patch = """
          {
            "id": %d,
            "quantity": 2,
            "notes":"keep same"
          }
        """.formatted(lineId);

        mvc.perform(patch(ApiPaths.Order.LINE_ITEMS.replace("{orderId}", String.valueOf(orderId)))
                        .with(jwtAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patch))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notes").value("keep same"))
                .andExpect(jsonPath("$.quantity").value(2));
    }

    @Test
    @DisplayName("update_invalid_quantity_rejected")
    void update_invalid_quantity_rejected() throws Exception {
        long orderId = createOrder(nextTableId(), 2);
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

        var r1 = mvc.perform(post(ApiPaths.Order.LINE_ITEMS.replace("{orderId}", String.valueOf(orderId)))
                        .with(jwtAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(add))
                .andExpect(status().isOk())
                .andReturn();

        long lineId = JsonPath.parse(r1.getResponse().getContentAsString()).read("$.id", Long.class);

        String patch = """
          {
            "id": %d,
            "quantity": 0
          }
        """.formatted(lineId);

        mvc.perform(patch(ApiPaths.Order.LINE_ITEMS.replace("{orderId}", String.valueOf(orderId)))
                        .with(jwtAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patch))
                .andExpect(status().isBadRequest());
    }
}
