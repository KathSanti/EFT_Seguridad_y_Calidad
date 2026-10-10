package com.duoc.backend.Appointment;

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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.duoc.backend.exception.ResourceNotFoundException;

@WebMvcTest(AppointmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AppointmentService appointmentService;

    private Appointment buildAppointment(Long id) {
        Appointment appointment = new Appointment();
        appointment.setId(id);
        appointment.setDate(LocalDate.of(2026, 10, 20));
        appointment.setTime(LocalTime.of(10, 30));
        appointment.setReason("Control anual");
        appointment.setVeterinarian("Dra. Soto");
        return appointment;
    }

    @Test
    void getAllAppointments_returns200WithList() throws Exception {
        when(appointmentService.getAllAppointments()).thenReturn(List.of(buildAppointment(1L)));

        mockMvc.perform(get("/appointment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reason").value("Control anual"));
    }

    @Test
    void getAppointmentById_notFound_returns404() throws Exception {
        when(appointmentService.getAppointmentById(99L))
                .thenThrow(new ResourceNotFoundException("Cita no encontrada con id: 99"));

        mockMvc.perform(get("/appointment/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void saveAppointment_validBody_returns200() throws Exception {
        when(appointmentService.saveAppointment(any(Appointment.class))).thenReturn(buildAppointment(1L));

        String body = """
                {
                  "date": "2026-10-20",
                  "time": "10:30:00",
                  "reason": "Control anual",
                  "veterinarian": "Dra. Soto"
                }
                """;

        mockMvc.perform(post("/appointment").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.veterinarian").value("Dra. Soto"));
    }

    @Test
    void saveAppointment_missingReason_returns400() throws Exception {
        String body = """
                {
                  "date": "2026-10-20",
                  "time": "10:30:00",
                  "veterinarian": "Dra. Soto"
                }
                """;

        mockMvc.perform(post("/appointment").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteAppointment_found_returns200() throws Exception {
        doNothing().when(appointmentService).deleteAppointment(1L);

        mockMvc.perform(delete("/appointment/1"))
                .andExpect(status().isOk());

        verify(appointmentService).deleteAppointment(1L);
    }

    @Test
    void deleteAppointment_notFound_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Cita no encontrada con id: 99"))
                .when(appointmentService).deleteAppointment(99L);

        mockMvc.perform(delete("/appointment/99"))
                .andExpect(status().isNotFound());
    }
}
