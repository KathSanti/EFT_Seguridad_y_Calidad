package com.duoc.backend.Invoice;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.stereotype.Service;

import com.duoc.backend.Care.Care;
import com.duoc.backend.Care.CareRepository;
import com.duoc.backend.Medication.Medication;
import com.duoc.backend.Medication.MedicationRepository;
import com.duoc.backend.exception.ResourceNotFoundException;

@Service
public class InvoiceService {


    private final InvoiceRepository invoiceRepository;
    private final MedicationRepository medicationRepository;
    private final CareRepository careRepository;

    public InvoiceService(
        CareRepository careRepository, 
        InvoiceRepository invoiceRepository, 
        MedicationRepository medicationRepository) {
            
        this.careRepository = careRepository;
        this.invoiceRepository = invoiceRepository;
        this.medicationRepository = medicationRepository;
    }



    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    public Invoice getInvoiceById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Factura no encontrada con id: " + id));
    }

    public Invoice saveInvoice(Invoice invoice) {
        // Validar que los medicamentos existen
        List<Medication> validMedications = StreamSupport.stream(
                medicationRepository.findAllById(
                        invoice.getMedications().stream().map(Medication::getId).collect(Collectors.toList())
                ).spliterator(), false
        ).collect(Collectors.toList());
        if (validMedications.size() != invoice.getMedications().size()) {
            throw new IllegalArgumentException("Algunos medicamentos no existen en la base de datos.");
        }

        // Validar que los servicios existen
        List<Care> validCares = StreamSupport.stream(
                careRepository.findAllById(
                        invoice.getCares().stream().map(Care::getId).collect(Collectors.toList())
                ).spliterator(), false
        ).collect(Collectors.toList());
        if (validCares.size() != invoice.getCares().size()) {
            throw new IllegalArgumentException("Algunos servicios no existen en la base de datos.");
        }

        // Calcular el costo total basado en los servicios y medicamentos asociados
        double totalCareCost = validCares.stream()
                .mapToDouble(Care::getCost)
                .sum();

        double totalMedicationCost = validMedications.stream()
                .mapToDouble(Medication::getCost)
                .sum();

        invoice.setTotalCost(totalCareCost + totalMedicationCost);

        // Guardar la factura en el repositorio
        return invoiceRepository.save(invoice);
    }

    public void deleteInvoice(Long id) {
        if (!invoiceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Factura no encontrada con id: " + id);
        }
        invoiceRepository.deleteById(id);
    }
}