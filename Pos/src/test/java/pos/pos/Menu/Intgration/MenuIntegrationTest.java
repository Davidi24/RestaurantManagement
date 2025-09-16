package pos.pos.Menu.Intgration;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import pos.pos.Config.ApiPaths;
import pos.pos.Entity.Menu.Menu;
import pos.pos.Repository.Menu.MenuRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MenuIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    MenuRepository menuRepository;

    private static final String TEST_NAME = "Integration Menuuuuuu";
    private static final String TEST_DESCRIPTION = "full stack test";

    @Test
    @WithMockUser(roles = "ADMIN")
    @Transactional
    void createAndGetMenu() throws Exception {

        String body = """
            { "name": "%s", "description": "%s" }
            """.formatted(TEST_NAME, TEST_DESCRIPTION);

        mvc.perform(post(ApiPaths.Menu.BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mvc.perform(get(ApiPaths.Menu.BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name").value(hasItem(TEST_NAME)));

        assertThat(menuRepository.findAll())
                .extracting(Menu::getName)
                .contains(TEST_NAME);
    }
}
