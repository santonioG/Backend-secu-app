package com.duoc.backend.invoice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InvoiceMailConfig {

    @Value("${app.mail.host:}")
    private String host;

    @Value("${app.mail.port:25}")
    private int port;

    @Value("${app.mail.username:}")
    private String username;

    @Value("${app.mail.password:}")
    private String password;

    @Value("${app.mail.smtp-auth:false}")
    private boolean smtpAuth;

    @Value("${app.mail.starttls:false}")
    private boolean startTls;

    @Bean
    public InvoiceMailSender invoiceMailSender() {
        // Centraliza la configuración SMTP para que el servicio de entrega solo coordine el flujo de facturación.
        return new SmtpInvoiceMailSender(host, port, username, password, smtpAuth, startTls);
    }
}
