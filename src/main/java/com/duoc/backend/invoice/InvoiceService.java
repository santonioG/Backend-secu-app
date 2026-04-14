package com.duoc.backend.invoice;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.duoc.backend.care.Care;
import com.duoc.backend.care.CareRepository;
import com.duoc.backend.medication.Medication;
import com.duoc.backend.medication.MedicationRepository;

@Service
public class InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private MedicationRepository medicationRepository;

    @Autowired
    private CareRepository careRepository;

    public List<Invoice> getAllInvoices() {
        return StreamSupport.stream(invoiceRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());
    }

    public Invoice getInvoiceById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));
    }

    public Invoice saveInvoice(Invoice invoice) {
        // Valida los IDs entrantes contra la base de datos antes de calcular el total final de la factura.
        List<Medication> requestedMedications = invoice.getMedications() == null
                ? Collections.emptyList()
                : invoice.getMedications();
        List<Care> requestedCares = invoice.getCares() == null
                ? Collections.emptyList()
                : invoice.getCares();

        List<Medication> validMedications = StreamSupport.stream(
                medicationRepository.findAllById(
                        requestedMedications.stream().map(Medication::getId).collect(Collectors.toList()))
                        .spliterator(),
                false).collect(Collectors.toList());
        if (validMedications.size() != requestedMedications.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Some medications do not exist in the database");
        }

        List<Care> validCares = StreamSupport.stream(
                careRepository.findAllById(
                        requestedCares.stream().map(Care::getId).collect(Collectors.toList()))
                        .spliterator(),
                false).collect(Collectors.toList());
        if (validCares.size() != requestedCares.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Some cares do not exist in the database");
        }

        double totalCareCost = validCares.stream()
                .mapToDouble(Care::getCost)
                .sum();
        double totalMedicationCost = validMedications.stream()
                .mapToDouble(Medication::getCost)
                .sum();

        invoice.setCares(validCares);
        invoice.setMedications(validMedications);
        invoice.setTotalCost(totalCareCost + totalMedicationCost);

        return invoiceRepository.save(invoice);
    }

    public void deleteInvoice(Long id) {
        if (!invoiceRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found");
        }
        invoiceRepository.deleteById(id);
    }
}
