package pos.pos.Menu.Intgration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import pos.pos.Entity.Menu.Menu;
import pos.pos.Repository.Menu.MenuRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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

    @Test
    @WithMockUser(roles = "ADMIN")
    void createAndGetMenu() throws Exception {
        String body = """
            { "name": "Integration Menu", "description": "full stack test" }
            """;

        mvc.perform(post("/api/menus")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/menus")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Integration Menu"));

        assertThat(menuRepository.findAll())
                .extracting(Menu::getName)
                .contains("Integration Menu");
    }
}
