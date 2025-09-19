package pos.pos.Order.Integration.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;
import pos.pos.Config.ApiPaths;
import pos.pos.Config.JWT.JwtService;
import pos.pos.Entity.Menu.ItemVariant;
import pos.pos.Entity.Menu.MenuItem;
import pos.pos.Entity.Menu.OptionGroup;
import pos.pos.Entity.Menu.OptionGroupType;
import pos.pos.Entity.Menu.OptionItem;
import pos.pos.Repository.Menu.ItemVariantRepository;
import pos.pos.Repository.Menu.MenuItemRepository;
import pos.pos.Repository.Order.OptionGroupRepository;
import pos.pos.Repository.Order.OptionItemRepository;
import pos.pos.Service.Notification.SseHub;
import pos.pos.Util.NotificationSender;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Shared test base:
 * - Boots the full Spring context with MockMvc.
 * - Wraps each test in a transaction (rollback for isolation).
 * - Provides helpers for seeding menu, creating orders, parsing ids, and role-based JWTs.
 * - Stubs JwtService + NotificationSender/SseHub to avoid external side effects.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestMethodOrder(MethodOrderer.MethodName.class)
public abstract class AbstractOrderIT {

    @Autowired protected MockMvc mvc;
    @Autowired protected ObjectMapper mapper;

    @Autowired protected MenuItemRepository menuItemRepo;
    @Autowired protected ItemVariantRepository variantRepo;
    @Autowired protected OptionGroupRepository groupRepo;
    @Autowired protected OptionItemRepository optionRepo;

    @MockitoBean protected JwtService jwtService;
    @MockitoBean protected NotificationSender notificationSender;
    @MockitoBean protected SseHub sseHub;

    private static final String USER_EMAIL = "david@example.com";
    private static final AtomicLong TABLE_SEQ = new AtomicLong(100);

    /**
     * Build a JWT for a specific role (e.g., "ADMIN", "WAITER", "USER").
     * Usage: with(jwtRole("ADMIN"))
     */
    protected RequestPostProcessor jwtRole(String role) {
        String springRole = "ROLE_" + role.toUpperCase();
        return jwt()
                .jwt(j -> j.subject(USER_EMAIL).claim("roles", Set.of(role.toUpperCase())))
                .authorities(new SimpleGrantedAuthority(springRole));
    }

    /** Convenience: ADMIN JWT. */
    protected RequestPostProcessor jwtAdmin() {
        return jwtRole("ADMIN");
    }

    /** Return a unique table id per call to avoid OPEN-order collisions across tests. */
    protected long nextTableId() { return TABLE_SEQ.incrementAndGet(); }

    /** Extract a top-level "id" from a JSON response body. */
    protected long idFrom(MvcResult res) throws Exception {
        JsonNode n = mapper.readTree(res.getResponse().getContentAsString());
        return n.get("id").asLong();
    }

    /**
     * Seed a minimal catalog: 1 item, 1 variant, 2 options.
     * @return [itemPid, variantPid, optPidA, optPidB]
     */
    protected UUID[] seedCatalog(double basePrice, double variantOverride, double optA, double optB) {
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

        return new UUID[]{ item.getPublicId(), variant.getPublicId(), o1.getPublicId(), o2.getPublicId() };
    }

    /**
     * Create an order via API and return its id.
     * Always stubs the revoked-token check to pass.
     */
    protected long createOrder(long tableId, long guests) throws Exception {
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
}
