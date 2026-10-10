package com.duoc.backend.Invoice;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.duoc.backend.exception.ResourceNotFoundException;

@WebMvcTest(InvoiceController.class)
@AutoConfigureMockMvc(addFilters = false)
class InvoiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InvoiceService invoiceService;

    private Invoice buildInvoice(Long id) {
        Invoice invoice = new Invoice();
        invoice.setId(id);
        invoice.setPatientName("Firulais");
        invoice.setDate(LocalDate.of(2026, 10, 20));
        invoice.setTime(LocalTime.of(11, 0));
        invoice.setTotalCost(45000.0);
        return invoice;
    }

    @Test
    void getAllInvoices_returns200WithList() throws Exception {
        when(invoiceService.getAllInvoices()).thenReturn(List.of(buildInvoice(1L)));

        mockMvc.perform(get("/invoice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].patientName").value("Firulais"));
    }

    @Test
    void getInvoiceById_notFound_returns404() throws Exception {
        when(invoiceService.getInvoiceById(99L))
                .thenThrow(new ResourceNotFoundException("Factura no encontrada con id: 99"));

        mockMvc.perform(get("/invoice/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void saveInvoice_validBody_returns200() throws Exception {
        when(invoiceService.saveInvoice(any(Invoice.class))).thenReturn(buildInvoice(1L));

        String body = """
                {
                  "patientName": "Firulais",
                  "date": "2026-10-20",
                  "time": "11:00:00",
                  "careIds": [1],
                  "medicationIds": [1]
                }
                """;

        mockMvc.perform(post("/invoice").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCost").value(45000.0));
    }

    @Test
    void saveInvoice_withoutOptionalIds_returns200() throws Exception {
        when(invoiceService.saveInvoice(any(Invoice.class))).thenReturn(buildInvoice(1L));

        String body = """
                {
                  "patientName": "Firulais",
                  "date": "2026-10-20",
                  "time": "11:00:00"
                }
                """;

        mockMvc.perform(post("/invoice").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
    }

    @Test
    void saveInvoice_invalidReferences_returns400() throws Exception {
        when(invoiceService.saveInvoice(any(Invoice.class)))
                .thenThrow(new IllegalArgumentException("Algunos medicamentos no existen en la base de datos."));

        String body = """
                {
                  "patientName": "Firulais",
                  "date": "2026-10-20",
                  "time": "11:00:00",
                  "medicationIds": [999]
                }
                """;

        mockMvc.perform(post("/invoice").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void saveInvoice_missingPatientName_returns400() throws Exception {
        String body = """
                {
                  "date": "2026-10-20",
                  "time": "11:00:00"
                }
                """;

        mockMvc.perform(post("/invoice").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteInvoice_found_returns200() throws Exception {
        doNothing().when(invoiceService).deleteInvoice(1L);

        mockMvc.perform(delete("/invoice/1"))
                .andExpect(status().isOk());

        verify(invoiceService).deleteInvoice(1L);
    }

    @Test
    void deleteInvoice_notFound_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Factura no encontrada con id: 99"))
                .when(invoiceService).deleteInvoice(99L);

        mockMvc.perform(delete("/invoice/99"))
                .andExpect(status().isNotFound());
    }
}
