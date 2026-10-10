package com.duoc.backend.Invoice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.duoc.backend.Care.Care;
import com.duoc.backend.Care.CareRepository;
import com.duoc.backend.Medication.Medication;
import com.duoc.backend.Medication.MedicationRepository;
import com.duoc.backend.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private CareRepository careRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private MedicationRepository medicationRepository;

    @InjectMocks
    private InvoiceService invoiceService;

    private Care buildCare(Long id, double cost) {
        Care care = new Care();
        care.setId(id);
        care.setName("Consulta General");
        care.setCost(cost);
        return care;
    }

    private Medication buildMedication(Long id, double cost) {
        Medication medication = new Medication();
        medication.setId(id);
        medication.setName("Antiparasitario Interno");
        medication.setCost(cost);
        return medication;
    }

    private Invoice buildInvoiceRequest(List<Care> cares, List<Medication> medications) {
        Invoice invoice = new Invoice();
        invoice.setPatientName("Firulais");
        invoice.setDate(LocalDate.of(2026, 10, 20));
        invoice.setTime(LocalTime.of(11, 0));
        invoice.setCares(cares);
        invoice.setMedications(medications);
        return invoice;
    }

    @Test
    void getAllInvoices_returnsList() {
        when(invoiceRepository.findAll()).thenReturn(List.of(new Invoice()));

        List<Invoice> result = invoiceService.getAllInvoices();

        assertThat(result).hasSize(1);
    }

    @Test
    void getInvoiceById_found() {
        Invoice invoice = new Invoice();
        invoice.setId(1L);
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));

        Invoice result = invoiceService.getInvoiceById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getInvoiceById_notFound_throws() {
        when(invoiceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> invoiceService.getInvoiceById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void saveInvoice_valid_calculatesTotalCostAndSaves() {
        Care care = buildCare(1L, 30000.0);
        Medication medication = buildMedication(1L, 15000.0);
        Invoice request = buildInvoiceRequest(List.of(care), List.of(medication));

        when(medicationRepository.findAllById(List.of(1L))).thenReturn(List.of(medication));
        when(careRepository.findAllById(List.of(1L))).thenReturn(List.of(care));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Invoice result = invoiceService.saveInvoice(request);

        assertThat(result.getTotalCost()).isEqualTo(45000.0);
        verify(invoiceRepository).save(request);
    }

    @Test
    void saveInvoice_medicationDoesNotExist_throwsIllegalArgumentException() {
        Invoice request = buildInvoiceRequest(new ArrayList<>(), List.of(buildMedication(99L, 15000.0)));

        when(medicationRepository.findAllById(List.of(99L))).thenReturn(List.of());

        assertThatThrownBy(() -> invoiceService.saveInvoice(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("medicamentos");

        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void saveInvoice_careDoesNotExist_throwsIllegalArgumentException() {
        Invoice request = buildInvoiceRequest(List.of(buildCare(99L, 30000.0)), new ArrayList<>());

        when(medicationRepository.findAllById(List.of())).thenReturn(List.of());
        when(careRepository.findAllById(List.of(99L))).thenReturn(List.of());

        assertThatThrownBy(() -> invoiceService.saveInvoice(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("servicios");

        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void deleteInvoice_found_deletesIt() {
        when(invoiceRepository.existsById(1L)).thenReturn(true);

        invoiceService.deleteInvoice(1L);

        verify(invoiceRepository).deleteById(1L);
    }

    @Test
    void deleteInvoice_notFound_throwsAndNeverDeletes() {
        when(invoiceRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> invoiceService.deleteInvoice(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(invoiceRepository, never()).deleteById(99L);
    }
}
