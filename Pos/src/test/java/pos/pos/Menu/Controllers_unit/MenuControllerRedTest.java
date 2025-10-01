package pos.pos.Menu.Controllers_unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import pos.pos.Controller.Menu.MenuController;
import pos.pos.DTO.Menu.MenuDTO.MenuRequest;
import pos.pos.DTO.Menu.MenuDTO.MenuResponse;
import pos.pos.Exeption.General.AlreadyExistsException;
import pos.pos.Exeption.MenuNotFoundException;
import pos.pos.Service.Interfecaes.Menu.MenuService;
import pos.pos.Config.Security.RevokedTokenFilter;
import pos.pos.Config.JWT.JwtService;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = MenuController.class)
@AutoConfigureMockMvc
class MenuControllerRedTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockBean MenuService menuService;
    @MockBean RevokedTokenFilter revokedTokenFilter;
    @MockBean JwtService jwtService;

    @Test
    void list_unauthenticated_should401() throws Exception {
        mvc.perform(get("/api/menus"))
                .andExpect(status().isUnauthorized());
    }

    @Test @WithMockUser(roles = "USER")
    void create_forbidden_should403() throws Exception {
        var body = new MenuRequest("Main", "Desc");
        mvc.perform(post("/api/menus")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }

    @Test @WithMockUser(roles = {"ADMIN"})
    void create_duplicate_should409_withErrorBody() throws Exception {
        var body = new MenuRequest("Main", "Desc");
        when(menuService.create(body))
                .thenThrow(new AlreadyExistsException("Menu with that name already exists"));

        mvc.perform(post("/api/menus")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body)))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Menu with that name already exists"));
    }

    @Test @WithMockUser(roles = {"MANAGER"})
    void create_validation_should400_fieldErrors() throws Exception {
        var invalid = new MenuRequest("  ", "Desc");

        mvc.perform(post("/api/menus")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test @WithMockUser(roles = {"ADMIN"})
    void get_notFound_should404() throws Exception {
        when(menuService.get(99L)).thenThrow(new MenuNotFoundException(99L));

        mvc.perform(get("/api/menus/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test @WithMockUser(roles = {"ADMIN"})
    void tree_notFound_should404() throws Exception {
        when(menuService.tree(88L)).thenThrow(new MenuNotFoundException(88L));

        mvc.perform(get("/api/menus/88/tree"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test @WithMockUser(roles = {"SUPERADMIN"})
    void patch_ok_should200() throws Exception {
        var req = new MenuRequest("Main Updated", "New Desc");
        var resp = new MenuResponse(1L, "Main Updated", "New Desc", UUID.randomUUID());

        when(menuService.patch(1L, req)).thenReturn(resp);

        mvc.perform(patch("/api/menus/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Main Updated"));
    }

    @Test @WithMockUser(roles = {"ADMIN"})
    void patch_notFound_should404() throws Exception {
        var req = new MenuRequest("X","Y");
        when(menuService.patch(77L, req)).thenThrow(new MenuNotFoundException(77L));

        mvc.perform(patch("/api/menus/77")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test @WithMockUser(roles = {"MANAGER"})
    void delete_ok_should204() throws Exception {
        mvc.perform(delete("/api/menus/5").with(csrf()))
                .andExpect(status().isNoContent());
        verify(menuService).delete(5L);
    }

    @Test @WithMockUser(roles = {"MANAGER"})
    void delete_notFound_should404() throws Exception {
        doThrow(new MenuNotFoundException(5L)).when(menuService).delete(5L);

        mvc.perform(delete("/api/menus/5").with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test @WithMockUser
    void list_ok_should200_andReturnArray() throws Exception {
        var r1 = new MenuResponse(1L,"A","a", UUID.randomUUID());
        var r2 = new MenuResponse(2L,"B","b", UUID.randomUUID());
        when(menuService.list()).thenReturn(List.of(r1, r2));

        mvc.perform(get("/api/menus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("A"))
                .andExpect(jsonPath("$[1].name").value("B"));
    }
}
