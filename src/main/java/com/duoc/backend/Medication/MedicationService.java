package com.duoc.backend.Medication;

import java.util.List;

import org.springframework.stereotype.Service;

import com.duoc.backend.exception.ResourceNotFoundException;

@Service
public class MedicationService {


    private MedicationRepository medicationRepository;

    public MedicationService(MedicationRepository medicationRepository) {
        this.medicationRepository = medicationRepository;
    }

    public List<Medication> getAllMedications() {
        return medicationRepository.findAll();
    }

    public Medication getMedicationById(Long id) {
        return medicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicamento no encontrado con id: " + id));
    }

    public Medication saveMedication(Medication medication) {
        return medicationRepository.save(medication);
    }

    public void deleteMedication(Long id) {
        if (!medicationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Medicamento no encontrado con id: " + id);
        }
        medicationRepository.deleteById(id);
    }
}