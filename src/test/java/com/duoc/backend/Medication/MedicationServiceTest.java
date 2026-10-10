package com.duoc.backend.Medication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
class MedicationServiceTest {

    @Mock
    private MedicationRepository medicationRepository;

    @InjectMocks
    private MedicationService medicationService;

    private Medication buildMedication(Long id) {
        Medication medication = new Medication();
        medication.setId(id);
        medication.setName("Antiparasitario Interno");
        medication.setCost(15000.0);
        return medication;
    }

    @Test
    void getAllMedications_returnsList() {
        when(medicationRepository.findAll()).thenReturn(List.of(buildMedication(1L)));

        List<Medication> result = medicationService.getAllMedications();

        assertThat(result).hasSize(1);
    }

    @Test
    void getMedicationById_found() {
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(buildMedication(1L)));

        Medication result = medicationService.getMedicationById(1L);

        assertThat(result.getName()).isEqualTo("Antiparasitario Interno");
    }

    @Test
    void getMedicationById_notFound_throws() {
        when(medicationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> medicationService.getMedicationById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void saveMedication_delegatesToRepository() {
        Medication toSave = buildMedication(null);
        Medication saved = buildMedication(1L);
        when(medicationRepository.save(toSave)).thenReturn(saved);

        Medication result = medicationService.saveMedication(toSave);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void deleteMedication_found_deletesIt() {
        when(medicationRepository.existsById(1L)).thenReturn(true);

        medicationService.deleteMedication(1L);

        verify(medicationRepository).deleteById(1L);
    }

    @Test
    void deleteMedication_notFound_throwsAndNeverDeletes() {
        when(medicationRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> medicationService.deleteMedication(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(medicationRepository, never()).deleteById(99L);
    }
}
