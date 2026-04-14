package com.duoc.backend.invoice;

public interface InvoiceMailSender {

    // Envía un correo de texto plano con un único archivo PDF adjunto para la factura.
    void send(String from, String to, String subject, String body, String attachmentName, byte[] attachmentBytes);
}
