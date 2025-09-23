package pos.pos.Order.Integration.OrderItem;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import pos.pos.Config.ApiPaths;
import pos.pos.Order.Integration.support.AbstractOrderIT;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class OrderLineItemCreateGetIT extends AbstractOrderIT {

    @Test
    @DisplayName("create_line_item_and_get_it")
    void create_line_item_and_get_it() throws Exception {
        long orderId = createOrder(nextTableId(), 2);
        UUID[] pids = seedCatalog(10.0, 12.0, 2.0, 3.0);
        UUID itemPid = pids[0], variantPid = pids[1], optA = pids[2], optB = pids[3];

        String add = """
          {
            "menuItemPublicId":"%s",
            "quantity":2,
            "variantPublicId":"%s",
            "optionPublicIds":["%s","%s"],
            "notes":"  extra spicy  "
          }
        """.formatted(itemPid, variantPid, optA, optB);

        var r = mvc.perform(post(ApiPaths.Order.LINE_ITEMS.replace("{orderId}", String.valueOf(orderId)))
                        .with(jwtAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(add))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unitPrice").value(12.0))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.lineSubtotal").value(34.0))
                .andExpect(jsonPath("$.notes").value("extra spicy"))
                .andReturn();

        long lineId = JsonPath.parse(r.getResponse().getContentAsString()).read("$.id", Long.class);

        mvc.perform(get(ApiPaths.Order.LINE_ITEMS.replace("{orderId}", String.valueOf(orderId)))
                        .with(jwtAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(lineId));

        mvc.perform(get(ApiPaths.Order.LINE_ITEMS.replace("{orderId}", String.valueOf(orderId)) + "/" + lineId)
                        .with(jwtAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(lineId))
                .andExpect(jsonPath("$.notes").value("extra spicy"));
    }

    @Test
    @DisplayName("create_same_spec_merges_quantity")
    void create_same_spec_merges_quantity() throws Exception {
        long orderId = createOrder(nextTableId(), 2);
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

        var r1 = mvc.perform(post(ApiPaths.Order.LINE_ITEMS.replace("{orderId}", String.valueOf(orderId)))
                        .with(jwtAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(add))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(2))
                .andReturn();

        long lineId = JsonPath.parse(r1.getResponse().getContentAsString()).read("$.id", Long.class);

        mvc.perform(post(ApiPaths.Order.LINE_ITEMS.replace("{orderId}", String.valueOf(orderId)))
                        .with(jwtAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(add))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(lineId))
                .andExpect(jsonPath("$.quantity").value(4));
    }

    @Test
    @DisplayName("create_different_spec_creates_new_line")
    void create_different_spec_creates_new_line() throws Exception {
        long orderId = createOrder(nextTableId(), 2);
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

        mvc.perform(post(ApiPaths.Order.LINE_ITEMS.replace("{orderId}", String.valueOf(orderId)))
                        .with(jwtAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addAB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lineSubtotal").value(17.0));

        mvc.perform(post(ApiPaths.Order.LINE_ITEMS.replace("{orderId}", String.valueOf(orderId)))
                        .with(jwtAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addAonly))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lineSubtotal").value(14.0));

        mvc.perform(get(ApiPaths.Order.LINE_ITEMS.replace("{orderId}", String.valueOf(orderId)))
                        .with(jwtAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }
}
