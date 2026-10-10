package com.duoc.backend.Appointment;


import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.duoc.backend.Appointment.dto.AppointmentCreateDto;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/appointment")
public class AppointmentController {

    //Inyección por constructor 
    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping
    public List<Appointment> getAllAppointments() {
        return appointmentService.getAllAppointments();
    }

    @GetMapping("/{id}")
    public Appointment getAppointmentById(@PathVariable Long id) {
        return appointmentService.getAppointmentById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_ASISTENTE')")
    public Appointment saveAppoinment(@Valid @RequestBody AppointmentCreateDto dto) {
        
        Appointment safeAppoinment = new Appointment();
        
        // Solo permitimos que se escriban los datos del formulario (DTO)
        safeAppoinment.setDate(dto.date());
        safeAppoinment.setTime(dto.time());
        safeAppoinment.setReason(dto.reason());
        safeAppoinment.setVeterinarian(dto.veterinarian());
        
        // No tocamos ni el id reponsabilidad exclusiva del servidor

        return appointmentService.saveAppointment(safeAppoinment);
    }


    @DeleteMapping("/{id}")
    public void deleteAppointment(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
    }
}
