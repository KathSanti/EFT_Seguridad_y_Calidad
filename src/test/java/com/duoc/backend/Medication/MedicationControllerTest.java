package com.duoc.backend.Medication;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.duoc.backend.exception.ResourceNotFoundException;

@WebMvcTest(MedicationController.class)
@AutoConfigureMockMvc(addFilters = false)
class MedicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MedicationService medicationService;

    private Medication buildMedication(Long id) {
        Medication medication = new Medication();
        medication.setId(id);
        medication.setName("Antiparasitario Interno");
        medication.setCost(15000.0);
        return medication;
    }

    @Test
    void getAllMedications_returns200WithList() throws Exception {
        when(medicationService.getAllMedications()).thenReturn(List.of(buildMedication(1L)));

        mockMvc.perform(get("/medication"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Antiparasitario Interno"));
    }

    @Test
    void getMedicationById_notFound_returns404() throws Exception {
        when(medicationService.getMedicationById(99L))
                .thenThrow(new ResourceNotFoundException("Medicamento no encontrado con id: 99"));

        mockMvc.perform(get("/medication/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void saveMedication_validBody_returns200() throws Exception {
        when(medicationService.saveMedication(any(Medication.class))).thenReturn(buildMedication(1L));

        String body = """
                {
                  "name": "Antiparasitario Interno",
                  "cost": 15000
                }
                """;

        mockMvc.perform(post("/medication").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void saveMedication_negativeCost_returns400() throws Exception {
        String body = """
                {
                  "name": "Antiparasitario Interno",
                  "cost": -5
                }
                """;

        mockMvc.perform(post("/medication").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteMedication_found_returns200() throws Exception {
        doNothing().when(medicationService).deleteMedication(1L);

        mockMvc.perform(delete("/medication/1"))
                .andExpect(status().isOk());

        verify(medicationService).deleteMedication(1L);
    }

    @Test
    void deleteMedication_notFound_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Medicamento no encontrado con id: 99"))
                .when(medicationService).deleteMedication(99L);

        mockMvc.perform(delete("/medication/99"))
                .andExpect(status().isNotFound());
    }
}
