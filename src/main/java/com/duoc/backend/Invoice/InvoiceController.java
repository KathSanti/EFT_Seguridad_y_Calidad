package com.duoc.backend.Invoice;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.duoc.backend.Care.Care;
import com.duoc.backend.Invoice.dto.InvoiceCreateDto;
import com.duoc.backend.Medication.Medication;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/invoice")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping
    public List<Invoice> getAllInvoices() {
        return invoiceService.getAllInvoices();
    }

    @GetMapping("/{id}")
    public Invoice getInvoiceById(@PathVariable Long id) {
        return invoiceService.getInvoiceById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_ASISTENTE')")
    public Invoice saveInvoice(@Valid @RequestBody InvoiceCreateDto dto) {
        
        Invoice safeInvoice = new Invoice();
        
        safeInvoice.setPatientName(dto.patientName());
        safeInvoice.setDate(dto.date());
        safeInvoice.setTime(dto.time());

        // Mapear ids a objetos Care
        if (dto.careIds() != null) {
            safeInvoice.setCares(dto.careIds().stream().map(id -> {
                Care care = new Care();
                care.setId(id);
                return care;
            }).collect(Collectors.toList()));
        } else {
            safeInvoice.setCares(new ArrayList<>());
        }

        // Mapear ids a objetos Medication
        if (dto.medicationIds() != null) {
            safeInvoice.setMedications(dto.medicationIds().stream().map(id -> {
                Medication med = new Medication();
                med.setId(id);
                return med;
            }).collect(Collectors.toList()));
        } else {
            safeInvoice.setMedications(new ArrayList<>());
        }

        return invoiceService.saveInvoice(safeInvoice);
    } 
    

    @DeleteMapping("/{id}")
    public void deleteInvoice(@PathVariable Long id) {
        invoiceService.deleteInvoice(id);
    }
}
