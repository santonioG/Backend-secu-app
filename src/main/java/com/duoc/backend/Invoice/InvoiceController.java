package com.duoc.backend.invoice;

import java.io.ByteArrayOutputStream;
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

import com.duoc.backend.care.Care;
import com.duoc.backend.medication.Medication;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

@RestController
@RequestMapping("/invoice")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @GetMapping
    public List<Invoice> getAllInvoices() {
        return (List<Invoice>) invoiceService.getAllInvoices();
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

    @GetMapping("/pdf/{id}")
    public ResponseEntity<byte[]> generateInvoicePdf(@PathVariable Long id) {
        Invoice invoice = invoiceService.getInvoiceById(id);

        if (invoice == null) {
            return ResponseEntity.notFound().build();
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            com.itextpdf.kernel.pdf.PdfDocument pdfDocument = new com.itextpdf.kernel.pdf.PdfDocument(writer);
            Document document = new Document(pdfDocument);

                 // Título de la factura
            Paragraph title = new Paragraph("Veterinaria Mi Mascota")
                .setFont(com.itextpdf.kernel.font.PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD))
                .setFontSize(24)
                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER);
            document.add(title);

            // Espaciado después del título
            document.add(new Paragraph("\n"));

            // Encabezado de la factura
            document.add(new Paragraph("Factura ID: " + invoice.getId()));
            document.add(new Paragraph("Paciente: " + invoice.getPatientName()));
            document.add(new Paragraph("Fecha: " + invoice.getDate()));
            document.add(new Paragraph("\n"));

            Table table = new Table(2); // 2 columnas: Descripción, Costo
            table.setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100));
            table.addCell("Descripción");
            table.addCell("Costo");

            // Agregar servicios a la tabla
            for (Care care : invoice.getCares()) {
                table.addCell(care.getName());
                table.addCell("$" + care.getCost());
            }

            // Agregar medicamentos a la tabla
            for (Medication medication : invoice.getMedications()) {
                table.addCell(medication.getName());
                table.addCell("$" + medication.getCost());
            }

            document.add(table);

            // Total
            document.add(new Paragraph("\nTotal: $" + invoice.getTotalCost()));
            document.close();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "invoice_" + id + ".pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(out.toByteArray());
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}
