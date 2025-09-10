package pos.pos.Order;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerAuthIT {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    String token = "eyJraWQiOiJrZXktMSIsImFsZyI6IlJTMjU2In0.eyJpc3MiOiJodHRwczovL3lvdXItYXBwLmV4YW1wbGUiLCJzdWIiOiJrZWNpZGF2aWQyMkBnbWFpbC5jb20iLCJleHAiOjE3NTc1NTA5NDQsInB1cnBvc2UiOiJBQ0NFU1MiLCJpYXQiOjE3NTc1MDc3NDQsInJvbGVzIjpbIlNVUEVSQURNSU4iXX0.GRA0r1VMtAh5Mi9E53NtKb7eO_xdHxEPTfJMF-RW0JEERa2JLzqEGaxzfWDwZvdulrIvdL3hoCmdR2Mcp8vFejKl4cXx3I1ereHfHunFXVf8hswxTHB2Io1cQeYJ_kSmQanknoVLXdNC0dhBpJp237YL9cbhrDxjVgaXygTE3oDwL6eKv9gybSFWvlRenGM0cbKXNPulxzddUuYa_teN1X5j0wG_n3lOi1F9cerOGVflJAF-ydQ8-zbgpOqBA4v0ajkfzREyyL4hgFL95A2ZWlibQ588Gu4NjWUpeu301whzERLBjMOemuP-PuTB5JUrZWUMIbDIBGpB1irEpviLwg";

    Long orderId;

    @BeforeEach
    void createOrderAndCaptureId() throws Exception {
        var res = mvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"tableId":1,"notes":"Test order","numberOfGuests":2}
                        """))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode node = om.readTree(res.getResponse().getContentAsString());
        orderId = node.get("id").asLong();
    }

    @Test
    void createOrder_thenGetById_200() throws Exception {
        mvc.perform(get("/api/v1/orders/{id}", orderId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void getAllOrders_200() throws Exception {
        mvc.perform(get("/api/v1/orders")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void updateOrder_200() throws Exception {
        mvc.perform(put("/api/v1/orders/{id}", orderId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"notes":"Updated note","numberOfGuests":3}
                        """))
                .andExpect(status().isOk());
    }

    @Test
    void updateStatus_200() throws Exception {
        mvc.perform(put("/api/v1/orders/{id}/status", orderId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"status":"ON_HOLD"}
                        """))
                .andExpect(status().isOk());
    }

    @Test
    void deleteOrder_204_withSuperadminToken() throws Exception {
        mvc.perform(delete("/api/v1/orders/{id}", orderId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }
}
