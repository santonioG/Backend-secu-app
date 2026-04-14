package com.duoc.backend.invoice;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.duoc.backend.care.Care;
import com.duoc.backend.medication.Medication;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

@Service
public class InvoicePdfService {

    public byte[] generatePdf(Invoice invoice) {
        // Construye el documento de la factura en memoria para evitar tocar el sistema de archivos.
        List<Care> cares = invoice.getCares() == null ? Collections.emptyList() : invoice.getCares();
        List<Medication> medications = invoice.getMedications() == null
                ? Collections.emptyList()
                : invoice.getMedications();

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDocument = new PdfDocument(writer);
            try (Document document = new Document(pdfDocument)) {
                Paragraph title = new Paragraph("Backend invoice")
                        .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
                        .setFontSize(24)
                        .setTextAlignment(TextAlignment.CENTER);
                document.add(title);
                document.add(new Paragraph("\n"));
                document.add(new Paragraph("Factura ID: " + invoice.getId()));
                document.add(new Paragraph("Paciente: " + invoice.getPatientName()));
                document.add(new Paragraph("Fecha: " + invoice.getDate()));
                document.add(new Paragraph("Hora: " + invoice.getTime()));
                document.add(new Paragraph("\n"));
                
                Table table = new Table(2);
                table.setWidth(UnitValue.createPercentValue(100));
                table.addCell("Descripcion");
                table.addCell("Costo");
                
                for (Care care : cares) {
                    table.addCell(care.getName());
                    table.addCell("$" + care.getCost());
                }
                
                for (Medication medication : medications) {
                    table.addCell(medication.getName());
                    table.addCell("$" + medication.getCost());
                }
                
                document.add(table);
                document.add(new Paragraph("\nTotal: $" + invoice.getTotalCost()));
            }
            return outputStream.toByteArray();
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to generate invoice PDF", exception);
        }
    }
}
