package com.duoc.backend.Care;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CareController.class)
@AutoConfigureMockMvc(addFilters = false)
class CareControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CareRepository careRepository;

    private Care buildCare(Long id) {
        Care care = new Care();
        care.setId(id);
        care.setName("Consulta General");
        care.setCost(30000.0);
        return care;
    }

    @Test
    void getAllCares_returns200WithList() throws Exception {
        when(careRepository.findAll()).thenReturn(List.of(buildCare(1L)));

        mockMvc.perform(get("/care"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Consulta General"));
    }

    @Test
    void getCareById_found_returns200() throws Exception {
        when(careRepository.findById(1L)).thenReturn(Optional.of(buildCare(1L)));

        mockMvc.perform(get("/care/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getCareById_notFound_returns404() throws Exception {
        when(careRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/care/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void saveCare_validBody_returns200() throws Exception {
        when(careRepository.save(any(Care.class))).thenReturn(buildCare(1L));

        String body = """
                {
                  "name": "Consulta General",
                  "cost": 30000
                }
                """;

        mockMvc.perform(post("/care").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void saveCare_blankName_returns400() throws Exception {
        String body = """
                {
                  "name": "",
                  "cost": 30000
                }
                """;

        mockMvc.perform(post("/care").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteCare_found_returns200() throws Exception {
        when(careRepository.existsById(1L)).thenReturn(true);

        mockMvc.perform(delete("/care/1"))
                .andExpect(status().isOk());

        verify(careRepository).deleteById(1L);
    }

    @Test
    void deleteCare_notFound_returns404() throws Exception {
        when(careRepository.existsById(99L)).thenReturn(false);

        mockMvc.perform(delete("/care/99"))
                .andExpect(status().isNotFound());
    }
}
