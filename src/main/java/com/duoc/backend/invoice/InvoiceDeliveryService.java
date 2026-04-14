package com.duoc.backend.invoice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class InvoiceDeliveryService {

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private InvoicePdfService invoicePdfService;

    @Autowired
    private InvoiceMailSender invoiceMailSender;

    @Value("${app.mail.from}")
    private String mailFrom;

    @Value("${app.base-url:http://localhost:${server.port:8080}}")
    private String baseUrl;

    public byte[] generatePrintablePdf(Long invoiceId) {
        // Reutiliza el mismo generador de PDF que se usa para el envío por correo electrónico.
        Invoice invoice = invoiceService.getInvoiceById(invoiceId);
        return invoicePdfService.generatePdf(invoice);
    }

    public void emailInvoice(Long invoiceId, InvoiceEmailRequest request) {
        if (request == null || request.getEmail() == null || request.getEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }

        Invoice invoice = invoiceService.getInvoiceById(invoiceId);
        byte[] pdfBytes = invoicePdfService.generatePdf(invoice);

        try {
            invoiceMailSender.send(
                    mailFrom,
                    request.getEmail(),
                    "Factura " + invoice.getId() + " - Backend App",
                    buildEmailBody(invoice),
                    "invoice_" + invoice.getId() + ".pdf",
                    pdfBytes);
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to send invoice email", exception);
        }
    }

    private String buildEmailBody(Invoice invoice) {
        return """
               Estimado cliente,
               
               Adjuntamos la factura """ + invoice.getId() + " correspondiente a "
                + invoice.getPatientName() + ".\n"
                + "Total: $" + invoice.getTotalCost() + "\n"
                + "Puede imprimirla desde: " + baseUrl + "/invoices/" + invoice.getId() + "/print\n\n"
                + "Saludos,\nBackend App";
    }
}
