package pos.pos.Order.Integration.OrderItem;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import pos.pos.Config.ApiPaths;
import pos.pos.Order.Integration.support.AbstractOrderIT;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class OrderLineItemDeleteIT extends AbstractOrderIT {

    @Test
    @DisplayName("delete_removes_line_and_updates_totals")
    void delete_removes_line_and_updates_totals() throws Exception {
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

        var r = mvc.perform(post(ApiPaths.Order.LINE_ITEMS.replace("{orderId}", String.valueOf(orderId)))
                        .with(jwtAdmin()).contentType(MediaType.APPLICATION_JSON).content(add))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lineSubtotal").value(34.0))
                .andReturn();

        long lineId = JsonPath.parse(r.getResponse().getContentAsString()).read("$.id", Long.class);

        mvc.perform(delete(ApiPaths.Order.LINE_ITEMS.replace("{orderId}", String.valueOf(orderId)) + "/" + lineId)
                        .with(jwtAdmin()))
                .andExpect(status().isOk());

        mvc.perform(get(ApiPaths.Order.LINE_ITEMS.replace("{orderId}", String.valueOf(orderId))).with(jwtAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mvc.perform(get(ApiPaths.Order.BASE + "/" + orderId).with(jwtAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grandTotal").value(0.0))
                .andExpect(jsonPath("$.balanceDue").value(0.0));
    }

    @Test
    @DisplayName("delete_last_line_sets_totals_zero")
    void delete_last_line_sets_totals_zero() throws Exception {
        long orderId = createOrder(nextTableId(), 2);
        UUID[] pids = seedCatalog(9.0, 11.0, 1.0, 0.0);
        UUID itemPid = pids[0], variantPid = pids[1], optA = pids[2];

        String add = """
          {
            "menuItemPublicId":"%s",
            "quantity":1,
            "variantPublicId":"%s",
            "optionPublicIds":["%s"]
          }
        """.formatted(itemPid, variantPid, optA);

        var r = mvc.perform(post(ApiPaths.Order.LINE_ITEMS.replace("{orderId}", String.valueOf(orderId)))
                        .with(jwtAdmin()).contentType(MediaType.APPLICATION_JSON).content(add))
                .andExpect(status().isOk())
                .andReturn();

        long lineId = JsonPath.parse(r.getResponse().getContentAsString()).read("$.id", Long.class);

        mvc.perform(delete(ApiPaths.Order.LINE_ITEMS.replace("{orderId}", String.valueOf(orderId)) + "/" + lineId)
                        .with(jwtAdmin()))
                .andExpect(status().isOk());

        mvc.perform(get(ApiPaths.Order.BASE + "/" + orderId).with(jwtAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grandTotal").value(0.0))
                .andExpect(jsonPath("$.balanceDue").value(0.0));
    }

    @Test
    @DisplayName("get_after_delete_not_found")
    void get_after_delete_not_found() throws Exception {
        long orderId = createOrder(nextTableId(), 2);
        UUID[] pids = seedCatalog(10.0, 12.0, 2.0, 3.0);
        UUID itemPid = pids[0], variantPid = pids[1], optA = pids[2], optB = pids[3];

        String add = """
          {
            "menuItemPublicId":"%s",
            "quantity":1,
            "variantPublicId":"%s",
            "optionPublicIds":["%s","%s"]
          }
        """.formatted(itemPid, variantPid, optA, optB);

        var r = mvc.perform(post(ApiPaths.Order.LINE_ITEMS.replace("{orderId}", String.valueOf(orderId)))
                        .with(jwtAdmin()).contentType(MediaType.APPLICATION_JSON).content(add))
                .andExpect(status().isOk())
                .andReturn();

        long lineId = JsonPath.parse(r.getResponse().getContentAsString()).read("$.id", Long.class);

        mvc.perform(delete(ApiPaths.Order.LINE_ITEMS.replace("{orderId}", String.valueOf(orderId)) + "/" + lineId)
                        .with(jwtAdmin()))
                .andExpect(status().isOk());

        mvc.perform(get(ApiPaths.Order.LINE_ITEMS.replace("{orderId}", String.valueOf(orderId)) + "/" + lineId)
                        .with(jwtAdmin()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("delete_line_not_in_order_mismatch")
    void delete_line_not_in_order_mismatch() throws Exception {
        long orderA = createOrder(nextTableId(), 2);
        long orderB = createOrder(nextTableId(), 2);
        UUID[] pids = seedCatalog(10.0, 12.0, 1.0, 1.0);
        UUID itemPid = pids[0], variantPid = pids[1], optA = pids[2], optB = pids[3];

        String add = """
          {
            "menuItemPublicId":"%s",
            "quantity":1,
            "variantPublicId":"%s",
            "optionPublicIds":["%s","%s"]
          }
        """.formatted(itemPid, variantPid, optA, optB);

        var r = mvc.perform(post(ApiPaths.Order.LINE_ITEMS.replace("{orderId}", String.valueOf(orderA)))
                        .with(jwtAdmin()).contentType(MediaType.APPLICATION_JSON).content(add))
                .andExpect(status().isOk())
                .andReturn();

        long lineId = JsonPath.parse(r.getResponse().getContentAsString()).read("$.id", Long.class);

        mvc.perform(delete(ApiPaths.Order.LINE_ITEMS.replace("{orderId}", String.valueOf(orderB)) + "/" + lineId)
                        .with(jwtAdmin()))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("delete_nonexistent_line_not_found")
    void delete_nonexistent_line_not_found() throws Exception {
        long orderId = createOrder(nextTableId(), 2);
        mvc.perform(delete(ApiPaths.Order.LINE_ITEMS.replace("{orderId}", String.valueOf(orderId)) + "/999999")
                        .with(jwtAdmin()))
                .andExpect(status().isNotFound());
    }
}
