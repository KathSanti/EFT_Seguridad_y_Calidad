package com.duoc.backend.Patient;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.duoc.backend.exception.ResourceNotFoundException;

@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    public Patient getPatientById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado con id: " + id));
    }

    public Patient savePatient(Patient patient) {
        return patientRepository.save(patient);
    }

    public List<Patient> getAllActivePatients() {
    // Retorna únicamente los pacientes cuya propiedad active sea true
    return StreamSupport.stream(patientRepository.findAll().spliterator(), false)
            .filter(Patient::isActive)
            .collect(Collectors.toList());
    }

    public void disablePatient(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado con id: " + id));
        patient.setActive(false);
        patientRepository.save(patient);
    }
}