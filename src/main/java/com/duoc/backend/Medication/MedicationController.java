package com.duoc.backend.Medication;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.duoc.backend.Medication.dto.MedicationCreateDto;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/medication")
public class MedicationController {

    private final MedicationService medicationService;
    
    public MedicationController(MedicationService medicationService) {
        this.medicationService = medicationService;
    }

    @GetMapping
    public List<Medication> getAllMedications() {
        return medicationService.getAllMedications();
    }

    @GetMapping("/{id}")
    public Medication getMedicationById(@PathVariable Long id) {
        return medicationService.getMedicationById(id);
    }
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DOCTOR')")
    public Medication saveMedication(@Valid @RequestBody MedicationCreateDto dto) {
        
        Medication safeMedication = new Medication();
        
        // Solo permitimos que se escriban los datos del formulario (DTO)
        safeMedication.setName(dto.name());
        safeMedication.setCost(dto.cost());
        
        // No tocamos ni el id reponsabilidad exclusiva del servidor

        return medicationService.saveMedication(safeMedication);
    }

    @DeleteMapping("/{id}")
    public void deleteMedication(@PathVariable Long id) {
        medicationService.deleteMedication(id);
    }
}