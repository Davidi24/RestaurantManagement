package pos.pos.Order.Integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import pos.pos.Config.ApiPaths;
import pos.pos.Config.JWT.JwtService;
import pos.pos.DTO.Order.OrderLineItemDTO.OrderLineItemUpdateDTO;
import pos.pos.Entity.Menu.ItemVariant;
import pos.pos.Entity.Menu.MenuItem;
import pos.pos.Entity.Menu.OptionGroup;
import pos.pos.Entity.Menu.OptionGroupType;
import pos.pos.Entity.Menu.OptionItem;
import pos.pos.Entity.Order.FulfillmentStatus;
import pos.pos.Entity.Order.OrderStatus;
import pos.pos.Repository.Menu.ItemVariantRepository;
import pos.pos.Repository.Menu.MenuItemRepository;
import pos.pos.Repository.Order.OptionGroupRepository;
import pos.pos.Repository.Order.OptionItemRepository;
import pos.pos.Service.Notification.SseHub;
import pos.pos.Util.NotificationSender;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full-stack integration tests for the Order API.
 * Uses real HTTP endpoints with MockMvc and an in-memory DB.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.MethodName.class)
class OrderIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @Autowired MenuItemRepository menuItemRepo;
    @Autowired ItemVariantRepository variantRepo;
    @Autowired OptionGroupRepository groupRepo;
    @Autowired OptionItemRepository optionRepo;

    // Boot 3.4+ replacement for @MockBean
    @MockitoBean JwtService jwtService;                 // Used by RevokedTokenFilter
    @MockitoBean NotificationSender notificationSender; // Avoid SSE side-effects
    @MockitoBean SseHub sseHub;                         // Safety for NotificationSender

    private static final String USER_EMAIL = "david@example.com";

    // ----------------------------- helpers -----------------------------------

    /** Build a JWT with ADMIN role so AuthUtils gets a JwtAuthenticationToken subject=email and roles. */
    private static RequestPostProcessor jwtAdmin() {
        return jwt()
                .jwt(j -> j.subject(USER_EMAIL).claim("roles", Set.of("ADMIN")))
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    /** Extract a top-level "id" from JSON response. */
    private long idFrom(MvcResult res) throws Exception {
        JsonNode n = mapper.readTree(res.getResponse().getContentAsString());
        return n.get("id").asLong();
    }

    /**
     * Seed minimal catalog: one MenuItem with a Variant and two Options.
     * Returns [itemPid, variantPid, optPidA, optPidB].
     */
    private UUID[] seedCatalog(double basePrice, double variantOverride, double optA, double optB) {
        MenuItem item = menuItemRepo.save(MenuItem.builder()
                .name("Item-" + UUID.randomUUID().toString().substring(0, 8))
                .basePrice(BigDecimal.valueOf(basePrice))
                .build());

        ItemVariant variant = variantRepo.save(ItemVariant.builder()
                .item(item)
                .name("Large")
                .priceOverride(BigDecimal.valueOf(variantOverride))
                .isDefault(false)
                .build());

        OptionGroup group = groupRepo.save(OptionGroup.builder()
                .item(item)
                .name("Extras")
                .type(OptionGroupType.MULTI)
                .required(false)
                .build());

        OptionItem o1 = optionRepo.save(OptionItem.builder()
                .group(group)
                .name("A")
                .priceDelta(BigDecimal.valueOf(optA))
                .build());

        OptionItem o2 = optionRepo.save(OptionItem.builder()
                .group(group)
                .name("B")
                .priceDelta(BigDecimal.valueOf(optB))
                .build());

        return new UUID[] { item.getPublicId(), variant.getPublicId(), o1.getPublicId(), o2.getPublicId() };
    }

    /** Create an order and return its id. */
    private long createOrder(long tableId, long guests) throws Exception {
        when(jwtService.isTokenRevoked(anyString())).thenReturn(false);
        String body = """
            {
              "tableId": %d,
              "numberOfGuests": %d
            }
            """.formatted(tableId, guests);
        MvcResult res = mvc.perform(post(ApiPaths.Order.BASE)
                        .with(jwtAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();
        return idFrom(res);
    }

    // -------------------------------------------------------------------------
    //                                   TESTS
    // -------------------------------------------------------------------------

    /** E2E happy path: create → add/merge items (+variant/options) → update qty → discounts → totals → fulfillment → serve-all → close → events present. */
    @Test
    @DisplayName("E2E: full happy path with pricing, discounts, totals, fulfillment, closing, events")
    void test01_e2e_order_flow() throws Exception {
        when(jwtService.isTokenRevoked(anyString())).thenReturn(false);

        // Seed catalog
        UUID[] pids = seedCatalog(10.0, 12.0, 2.0, 3.0);
        UUID itemPid = pids[0], variantPid = pids[1], optA = pids[2], optB = pids[3];

        // Create order
        long orderId = createOrder(5, 2);

        System.out.println("OrderId: " + orderId);


        // Add line item (qty=2) with variant+two options: unit = 12 + (2+3) = 17; subtotal=34
        String addLine = """
            {
              "menuItemPublicId": "%s",
              "quantity": 2,
              "variantPublicId": "%s",
              "optionPublicIds": ["%s","%s"]
            }
            """.formatted(itemPid, variantPid, optA, optB);

        MvcResult li1Res = mvc.perform(post(ApiPaths.Order.LINE_ITEMS.replace("{orderId}", String.valueOf(orderId)))
                        .with(jwtAdmin()).contentType(MediaType.APPLICATION_JSON).content(addLine))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.unitPrice").value(12.0))
                .andExpect(jsonPath("$.lineSubtotal").value(34.0))
                .andReturn();
        long li1 = idFrom(li1Res);

        // Add same spec again (qty=2) → merges to qty=4; subtotal=68
        mvc.perform(post(ApiPaths.Order.LINE_ITEMS.replace("{orderId}", String.valueOf(orderId)))
                        .with(jwtAdmin()).contentType(MediaType.APPLICATION_JSON).content(addLine))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value((int) li1))
                .andExpect(jsonPath("$.quantity").value(4))
                .andExpect(jsonPath("$.lineSubtotal").value(68.0));

        // Add different spec (only one option) qty=1 → new line: unit = 12 + 2 = 14; subtotal=14
        String addLineDiff = """
            {
              "menuItemPublicId": "%s",
              "quantity": 1,
              "variantPublicId": "%s",
              "optionPublicIds": ["%s"]
            }
            """.formatted(itemPid, variantPid, optA);
        MvcResult li2Res = mvc.perform(post(ApiPaths.Order.LINE_ITEMS.replace("{orderId}", String.valueOf(orderId)))
                        .with(jwtAdmin()).contentType(MediaType.APPLICATION_JSON).content(addLineDiff))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(1))
                .andExpect(jsonPath("$.lineSubtotal").value(14.0))
                .andReturn();
        long li2 = idFrom(li2Res);

        // Update li2 to qty=500 (stress test)
        String updLine = mapper.writeValueAsString(
                OrderLineItemUpdateDTO.builder().id(li2).quantity(500).build()
        );
        mvc.perform(put(ApiPaths.Order.LINE_ITEMS.replace("{orderId}", String.valueOf(orderId)))
                        .with(jwtAdmin()).contentType(MediaType.APPLICATION_JSON).content(updLine))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value((int) li2))
                .andExpect(jsonPath("$.quantity").value(500))
                .andExpect(jsonPath("$.lineSubtotal").value(500 * 14.0)); // 7000

        // Items subtotal now: 68 + 7000 = 7068. Add order-level discount 5.5 and line discount 3.0 on li1.
        String orderDisc = """
            { "name":"Promo", "amount": 5.5, "orderLevel": true }
            """;
        mvc.perform(post(ApiPaths.Order.DISCOUNTS.replace("{orderId}", String.valueOf(orderId)))
                        .with(jwtAdmin()).contentType(MediaType.APPLICATION_JSON).content(orderDisc))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderLevel").value(true));

        String lineDisc = """
            { "name":"LineOff", "amount": 3.0, "orderLevel": false, "lineItemId": %d }
            """.formatted(li1);
        mvc.perform(post(ApiPaths.Order.DISCOUNTS.replace("{orderId}", String.valueOf(orderId)))
                        .with(jwtAdmin()).contentType(MediaType.APPLICATION_JSON).content(lineDisc))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderLevel").value(false))
                .andExpect(jsonPath("$.lineItemId").value((int) li1));

        // Totals = 7068 - (5.5 + 3.0) = 7059.5
        mvc.perform(get(ApiPaths.Order.BASE + "/" + orderId).with(jwtAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grandTotal").value(7059.5))
                .andExpect(jsonPath("$.balanceDue").value(7059.5));

        // Valid fulfillment transitions NEW->FIRED->READY->SERVED
        String fPath = ApiPaths.Order.LINE_ITEMS.replace("{orderId}", String.valueOf(orderId)) + "/" + li1 + "/fulfillment";
        mvc.perform(put(fPath).with(jwtAdmin()).param("status", FulfillmentStatus.FIRED.name()))
                .andExpect(status().isOk());
        mvc.perform(put(fPath).with(jwtAdmin()).param("status", FulfillmentStatus.READY.name()))
                .andExpect(status().isOk());
        mvc.perform(put(fPath).with(jwtAdmin()).param("status", FulfillmentStatus.SERVED.name()))
                .andExpect(status().isOk());

        // Invalid transition SERVED->READY must fail
        mvc.perform(put(fPath).with(jwtAdmin()).param("status", FulfillmentStatus.READY.name()))
                .andExpect(status().is4xxClientError());

        // Serve all remaining items
        mvc.perform(put(ApiPaths.Order.BASE + "/" + orderId + "/serve-all").with(jwtAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(OrderStatus.OPEN.name())); // status stays OPEN

        // Close order (allowed only if all items served/voided)
        String closeBody = """
            { "status": "CLOSED" }
            """;
        mvc.perform(put(ApiPaths.Order.BASE + "/" + orderId + "/status")
                        .with(jwtAdmin()).contentType(MediaType.APPLICATION_JSON).content(closeBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"))
                .andExpect(jsonPath("$.closedAt").isNotEmpty());

        // Events include key types
        mvc.perform(get(ApiPaths.Order.EVENTS.replace("{orderId}", String.valueOf(orderId))).with(jwtAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].type", hasItems(
                        "CREATED","ITEM_ADDED","ITEM_UPDATED","DISCOUNT_APPLIED","CLOSED"
                )));
    }


    /** Guard rails: once CLOSED/VOIDED, order is not editable (adding items must fail). Also cover "missing price" protection path indirectly. */
    @Test
    @DisplayName("Guard rails: CLOSED/VOIDED orders are immutable; adding after close fails")
    void test02_guard_rails() throws Exception {
        when(jwtService.isTokenRevoked(anyString())).thenReturn(false);

        long orderId = createOrder(7, 1);

        // Close immediately (no items)
        mvc.perform(put(ApiPaths.Order.BASE + "/" + orderId + "/status")
                        .with(jwtAdmin()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            { "status": "CLOSED" }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));

        // Try adding an item to CLOSED order → 4xx
        UUID[] pids = seedCatalog(9.0, 11.0, 1.0, 0.5);
        String addLine = """
            { "menuItemPublicId":"%s", "quantity":1, "variantPublicId":"%s" }
            """.formatted(pids[0], pids[1]);

        mvc.perform(post(ApiPaths.Order.LINE_ITEMS.replace("{orderId}", String.valueOf(orderId)))
                        .with(jwtAdmin()).contentType(MediaType.APPLICATION_JSON).content(addLine))
                .andExpect(status().is4xxClientError());
    }



    /** Metadata updates: notes / guests / table change; ensure open-order constraint across tables is enforced. */
    @Test
    @DisplayName("Update order metadata and enforce single OPEN order per table")
    void test03_update_metadata_and_open_order_constraints() throws Exception {
        when(jwtService.isTokenRevoked(anyString())).thenReturn(false);

        long orderA = createOrder(10, 2);
        long orderB = createOrder(11, 3);

        // Update orderA: change notes, guests
        mvc.perform(put(ApiPaths.Order.BASE + "/" + orderA)
                        .with(jwtAdmin()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            { "notes": "No onions", "numberOfGuests": 4 }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notes").value("No onions"))
                .andExpect(jsonPath("$.numberOfGuests").value(4));

        // Attempt to move orderA to table=11 while orderB is OPEN on 11 → must fail
        mvc.perform(put(ApiPaths.Order.BASE + "/" + orderA)
                        .with(jwtAdmin()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            { "tableId": 11 }
                            """))
                .andExpect(status().is4xxClientError());

        // Close orderB, then moving orderA to table 11 should succeed
        mvc.perform(put(ApiPaths.Order.BASE + "/" + orderB + "/status")
                        .with(jwtAdmin()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            { "status": "CLOSED" }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));

        mvc.perform(put(ApiPaths.Order.BASE + "/" + orderA)
                        .with(jwtAdmin()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            { "tableId": 11 }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tableId").value(11));
    }

    /** Deleting a line item recalculates totals and logs an event. */
    @Test
    @DisplayName("Delete line item → totals recalc and event logging")
    void test04_delete_line_item_and_recalc_totals() throws Exception {
        when(jwtService.isTokenRevoked(anyString())).thenReturn(false);

        long orderId = createOrder(20, 2);
        UUID[] pids = seedCatalog(8.0, 10.0, 1.0, 0.0);
        String addA = """
            { "menuItemPublicId":"%s", "quantity":2, "variantPublicId":"%s", "optionPublicIds":["%s"] }
            """.formatted(pids[0], pids[1], pids[2]);  // unit=10+1=11 => subtotal=22
        String addB = """
            { "menuItemPublicId":"%s", "quantity":3, "variantPublicId":"%s" }
            """.formatted(pids[0], pids[1]);          // unit=10 => subtotal=30

        MvcResult liARes = mvc.perform(post(ApiPaths.Order.LINE_ITEMS.replace("{orderId}", String.valueOf(orderId)))
                        .with(jwtAdmin()).contentType(MediaType.APPLICATION_JSON).content(addA))
                .andExpect(status().isOk()).andReturn();
        long liA = idFrom(liARes);

        mvc.perform(post(ApiPaths.Order.LINE_ITEMS.replace("{orderId}", String.valueOf(orderId)))
                        .with(jwtAdmin()).contentType(MediaType.APPLICATION_JSON).content(addB))
                .andExpect(status().isOk());

        // Check total: 22 + 30 = 52
        mvc.perform(get(ApiPaths.Order.BASE + "/" + orderId).with(jwtAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grandTotal").value(52.0));

        // Delete liA → total should be 30
        mvc.perform(delete(ApiPaths.Order.LINE_ITEMS.replace("{orderId}", String.valueOf(orderId)) + "/" + liA)
                        .with(jwtAdmin()))
                .andExpect(status().isNoContent());

        mvc.perform(get(ApiPaths.Order.BASE + "/" + orderId).with(jwtAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grandTotal").value(30.0));

        // Events should include ITEM_DELETED
        mvc.perform(get(ApiPaths.Order.EVENTS.replace("{orderId}", String.valueOf(orderId))).with(jwtAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].type", hasItem("ITEM_DELETED")));
    }

    /** Listing and retrieval: create multiple orders, verify GET /orders returns them and GET /orders/{id} matches. */
    @Test
    @DisplayName("List and get orders")
    void test05_list_and_get_orders() throws Exception {
        when(jwtService.isTokenRevoked(anyString())).thenReturn(false);

        long a = createOrder(30, 1);
        long b = createOrder(31, 2);

        // GET list has both ids
        mvc.perform(get(ApiPaths.Order.BASE).with(jwtAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItems((int) a, (int) b)));

        // GET single by id
        mvc.perform(get(ApiPaths.Order.BASE + "/" + b).with(jwtAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value((int) b))
                .andExpect(jsonPath("$.status").value(OrderStatus.OPEN.name()));
    }

    /** Status transitions: exercise illegal transitions and VOID flow; ensure CLOSED/VOIDED behave as designed. */
    @Test
    @DisplayName("Illegal transitions and VOID flow")
    void test06_illegal_status_transitions_and_void() throws Exception {
        when(jwtService.isTokenRevoked(anyString())).thenReturn(false);

        long orderId = createOrder(40, 2);

        // Illegal: OPEN -> READY is not allowed directly
        mvc.perform(put(ApiPaths.Order.BASE + "/" + orderId + "/status")
                        .with(jwtAdmin()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            { "status": "READY" }
                            """))
                .andExpect(status().is4xxClientError());

        // Allowed: OPEN -> SENT_TO_KITCHEN
        mvc.perform(put(ApiPaths.Order.BASE + "/" + orderId + "/status")
                        .with(jwtAdmin()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            { "status": "SENT_TO_KITCHEN" }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SENT_TO_KITCHEN"));

        // VOID order
        mvc.perform(put(ApiPaths.Order.BASE + "/" + orderId + "/status")
                        .with(jwtAdmin()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            { "status": "VOIDED" }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("VOIDED"))
                .andExpect(jsonPath("$.closedAt").isNotEmpty());

        // Trying to re-open after VOIDED is not allowed per ALLOWED map (VOIDED->VOIDED only)
        mvc.perform(put(ApiPaths.Order.BASE + "/" + orderId + "/status")
                        .with(jwtAdmin()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            { "status": "OPEN" }
                            """))
                .andExpect(status().is4xxClientError());
    }

    /** Serving a single item endpoint: ensure it marks only that item SERVED and constraints apply. */
    @Test
    @DisplayName("Serve one item endpoint")
    void test07_serve_one_item() throws Exception {
        when(jwtService.isTokenRevoked(anyString())).thenReturn(false);

        long orderId = createOrder(50, 2);
        UUID[] pids = seedCatalog(10.0, 12.0, 1.0, 0.0);

        String add1 = """
            { "menuItemPublicId":"%s", "quantity":1, "variantPublicId":"%s" }
            """.formatted(pids[0], pids[1]);
        String add2 = """
            { "menuItemPublicId":"%s", "quantity":2, "variantPublicId":"%s" }
            """.formatted(pids[0], pids[1]);

        MvcResult r1 = mvc.perform(post(ApiPaths.Order.LINE_ITEMS.replace("{orderId}", String.valueOf(orderId)))
                        .with(jwtAdmin()).contentType(MediaType.APPLICATION_JSON).content(add1))
                .andExpect(status().isOk()).andReturn();
        long l1 = idFrom(r1);

        MvcResult r2 = mvc.perform(post(ApiPaths.Order.LINE_ITEMS.replace("{orderId}", String.valueOf(orderId)))
                        .with(jwtAdmin()).contentType(MediaType.APPLICATION_JSON).content(add2))
                .andExpect(status().isOk()).andReturn();
        long l2 = idFrom(r2);

        // Serve only l1
        mvc.perform(put(ApiPaths.Order.BASE + "/" + orderId + "/items/" + l1 + "/serve").with(jwtAdmin()))
                .andExpect(status().isOk());

        // Verify events contain "Item ... served"
        mvc.perform(get(ApiPaths.Order.EVENTS.replace("{orderId}", String.valueOf(orderId))).with(jwtAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].metadata", hasItem(containsString("Item " + l1 + " served"))));

        // Try serving l1 again → should fail (already SERVED)
        mvc.perform(put(ApiPaths.Order.BASE + "/" + orderId + "/items/" + l1 + "/serve").with(jwtAdmin()))
                .andExpect(status().is4xxClientError());

        // Close should fail now (l2 not served yet)
        mvc.perform(put(ApiPaths.Order.BASE + "/" + orderId + "/status")
                        .with(jwtAdmin()).contentType(MediaType.APPLICATION_JSON).content("""
                            { "status": "CLOSED" }
                            """))
                .andExpect(status().is4xxClientError());

        // Serve all, then close
        mvc.perform(put(ApiPaths.Order.BASE + "/" + orderId + "/serve-all").with(jwtAdmin()))
                .andExpect(status().isOk());
        mvc.perform(put(ApiPaths.Order.BASE + "/" + orderId + "/status")
                        .with(jwtAdmin()).contentType(MediaType.APPLICATION_JSON).content("""
                            { "status": "CLOSED" }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));
    }

    /** Delete order endpoint: ensure only admins can delete (secured by @PreAuthorize) and the order disappears. */
    @Test
    @DisplayName("Delete order by id (admin-only)")
    void test08_delete_order() throws Exception {
        when(jwtService.isTokenRevoked(anyString())).thenReturn(false);

        long orderId = createOrder(60, 2);

        // Delete
        mvc.perform(delete(ApiPaths.Order.BASE + "/" + orderId).with(jwtAdmin()))
                .andExpect(status().isNoContent());

        // 404 on fetch afterwards
        mvc.perform(get(ApiPaths.Order.BASE + "/" + orderId).with(jwtAdmin()))
                .andExpect(status().is4xxClientError());
    }

    // (Optional future) Add pagination/filter tests when your endpoints exist.
}
