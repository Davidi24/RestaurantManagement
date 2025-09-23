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

class OrderLineItemFulfillmentStatusIT extends AbstractOrderIT {

    @Test
    @DisplayName("allowed_status_transitions_succeed")
    void allowed_status_transitions_succeed() throws Exception {
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
                        .with(jwtAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(add))
                .andExpect(status().isOk())
                .andReturn();

        long lineId = JsonPath.parse(r.getResponse().getContentAsString()).read("$.id", Long.class);

        mvc.perform(patch(ApiPaths.Order.LINE_ITEMS.replace("{orderId}", String.valueOf(orderId)) + "/" + lineId + "/status/FIRED")
                        .with(jwtAdmin()))
                .andExpect(status().isOk());

        mvc.perform(patch(ApiPaths.Order.LINE_ITEMS.replace("{orderId}", String.valueOf(orderId)) + "/" + lineId + "/status/READY")
                        .with(jwtAdmin()))
                .andExpect(status().isOk());

        mvc.perform(patch(ApiPaths.Order.LINE_ITEMS.replace("{orderId}", String.valueOf(orderId)) + "/" + lineId + "/status/SERVED")
                        .with(jwtAdmin()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("illegal_status_transition_rejected")
    void illegal_status_transition_rejected() throws Exception {
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
                        .with(jwtAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(add))
                .andExpect(status().isOk())
                .andReturn();

        long lineId = JsonPath.parse(r.getResponse().getContentAsString()).read("$.id", Long.class);

        mvc.perform(patch(ApiPaths.Order.LINE_ITEMS.replace("{orderId}", String.valueOf(orderId)) + "/" + lineId + "/status/READY")
                        .with(jwtAdmin()))
                .andExpect(status().isBadRequest());
    }
}
