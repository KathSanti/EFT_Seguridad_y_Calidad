package com.duoc.backend.Appointment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.duoc.backend.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
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
    void getAllAppointments_returnsList() {
        when(appointmentRepository.findAll()).thenReturn(List.of(buildAppointment(1L)));

        List<Appointment> result = appointmentService.getAllAppointments();

        assertThat(result).hasSize(1);
    }

    @Test
    void getAppointmentById_found() {
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(buildAppointment(1L)));

        Appointment result = appointmentService.getAppointmentById(1L);

        assertThat(result.getReason()).isEqualTo("Control anual");
    }

    @Test
    void getAppointmentById_notFound_throws() {
        when(appointmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.getAppointmentById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void saveAppointment_delegatesToRepository() {
        Appointment toSave = buildAppointment(null);
        Appointment saved = buildAppointment(1L);
        when(appointmentRepository.save(toSave)).thenReturn(saved);

        Appointment result = appointmentService.saveAppointment(toSave);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void deleteAppointment_found_deletesIt() {
        when(appointmentRepository.existsById(1L)).thenReturn(true);

        appointmentService.deleteAppointment(1L);

        verify(appointmentRepository).deleteById(1L);
    }

    @Test
    void deleteAppointment_notFound_throwsAndNeverDeletes() {
        when(appointmentRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> appointmentService.deleteAppointment(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(appointmentRepository, never()).deleteById(99L);
    }
}
