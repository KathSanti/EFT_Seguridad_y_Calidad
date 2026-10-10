package com.duoc.backend.Patient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.duoc.backend.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientService patientService;

    private Patient buildPatient(Long id, boolean active) {
        Patient patient = new Patient();
        patient.setId(id);
        patient.setName("Firulais");
        patient.setSpecies("Perro");
        patient.setBreed("Labrador");
        patient.setAge(3);
        patient.setOwner("Juan Perez");
        patient.setActive(active);
        return patient;
    }

    @Test
    void getAllPatients_returnsRepositoryList() {
        when(patientRepository.findAll()).thenReturn(List.of(buildPatient(1L, true)));

        List<Patient> result = patientService.getAllPatients();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Firulais");
    }

    @Test
    void getPatientById_found_returnsPatient() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(buildPatient(1L, true)));

        Patient result = patientService.getPatientById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getPatientById_notFound_throwsResourceNotFoundException() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.getPatientById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void savePatient_delegatesToRepository() {
        Patient toSave = buildPatient(null, true);
        Patient saved = buildPatient(1L, true);
        when(patientRepository.save(toSave)).thenReturn(saved);

        Patient result = patientService.savePatient(toSave);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void disablePatient_found_setsInactiveAndSaves() {
        Patient patient = buildPatient(1L, true);
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(patientRepository.save(any(Patient.class))).thenReturn(patient);

        patientService.disablePatient(1L);

        assertThat(patient.isActive()).isFalse();
        verify(patientRepository).save(patient);
    }

    @Test
    void disablePatient_notFound_throwsAndNeverSaves() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.disablePatient(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(patientRepository, never()).save(any());
    }

    @Test
    void getAllActivePatients_filtersOutInactive() {
        when(patientRepository.findAll()).thenReturn(List.of(
                buildPatient(1L, true),
                buildPatient(2L, false)
        ));

        List<Patient> result = patientService.getAllActivePatients();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }
}
