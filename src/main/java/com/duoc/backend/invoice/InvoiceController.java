package com.duoc.backend.invoice;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/invoices")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private InvoiceDeliveryService invoiceDeliveryService;

    @GetMapping
    public List<Invoice> getAllInvoices() {
        return invoiceService.getAllInvoices();
    }

    @GetMapping("/{id}")
    public Invoice getInvoiceById(@PathVariable Long id) {
        return invoiceService.getInvoiceById(id);
    }

    @PostMapping
    public Invoice saveInvoice(@RequestBody Invoice invoice) {
        return invoiceService.saveInvoice(invoice);
    }

    @DeleteMapping("/{id}")
    public void deleteInvoice(@PathVariable Long id) {
        invoiceService.deleteInvoice(id);
    }

    @GetMapping("/{id}/print")
    public ResponseEntity<byte[]> printInvoice(@PathVariable Long id) {
        // Retorna el PDF en línea (inline) para que el navegador o el cliente puedan abrir el cuadro de diálogo de impresión.
        byte[] pdfBytes = invoiceDeliveryService.generatePrintablePdf(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=invoice_" + id + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @PostMapping("/{id}/email")
    public ResponseEntity<String> emailInvoice(@PathVariable Long id, @RequestBody InvoiceEmailRequest request) {
        // Envía el PDF de la factura generada al correo electrónico del cliente proporcionado en el cuerpo de la solicitud.
        invoiceDeliveryService.emailInvoice(id, request);
        return ResponseEntity.ok("Invoice emailed successfully");
    }
}
