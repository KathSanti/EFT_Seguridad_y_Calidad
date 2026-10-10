package com.duoc.backend.Patient;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.duoc.backend.exception.ResourceNotFoundException;

@WebMvcTest(PatientController.class)
@AutoConfigureMockMvc(addFilters = false)
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PatientService patientService;

    private Patient buildPatient(Long id) {
        Patient patient = new Patient();
        patient.setId(id);
        patient.setName("Firulais");
        patient.setSpecies("Perro");
        patient.setBreed("Labrador");
        patient.setAge(3);
        patient.setOwner("Juan Perez");
        patient.setActive(true);
        return patient;
    }

    @Test
    void getAllPatients_returns200WithList() throws Exception {
        when(patientService.getAllPatients()).thenReturn(List.of(buildPatient(1L)));

        mockMvc.perform(get("/patient"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Firulais"));
    }

    @Test
    void getPatientById_found_returns200() throws Exception {
        when(patientService.getPatientById(1L)).thenReturn(buildPatient(1L));

        mockMvc.perform(get("/patient/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getPatientById_notFound_returns404() throws Exception {
        when(patientService.getPatientById(99L))
                .thenThrow(new ResourceNotFoundException("Paciente no encontrado con id: 99"));

        mockMvc.perform(get("/patient/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Paciente no encontrado con id: 99"));
    }

    @Test
    void savePatient_validBody_returns200() throws Exception {
        when(patientService.savePatient(any(Patient.class))).thenReturn(buildPatient(1L));

        String body = """
                {
                  "name": "Firulais",
                  "species": "Perro",
                  "breed": "Labrador",
                  "age": 3,
                  "owner": "Juan Perez"
                }
                """;

        mockMvc.perform(post("/patient").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void savePatient_missingRequiredField_returns400() throws Exception {
        String body = """
                {
                  "species": "Perro",
                  "age": 3,
                  "owner": "Juan Perez"
                }
                """;

        mockMvc.perform(post("/patient").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void disablePatient_found_returns200() throws Exception {
        doNothing().when(patientService).disablePatient(1L);

        mockMvc.perform(put("/patient/disable/1"))
                .andExpect(status().isOk());

        verify(patientService).disablePatient(1L);
    }

    @Test
    void disablePatient_notFound_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Paciente no encontrado con id: 99"))
                .when(patientService).disablePatient(99L);

        mockMvc.perform(put("/patient/disable/99"))
                .andExpect(status().isNotFound());
    }
}
