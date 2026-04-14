package com.duoc.backend.invoice;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class SmtpInvoiceMailSender implements InvoiceMailSender {

    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final boolean smtpAuth;
    private final boolean startTls;

    public SmtpInvoiceMailSender(
            String host,
            int port,
            String username,
            String password,
            boolean smtpAuth,
            boolean startTls) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.smtpAuth = smtpAuth;
        this.startTls = startTls;
    }

    @Override
    public void send(String from, String to, String subject, String body, String attachmentName, byte[] attachmentBytes) {
        if (host == null || host.isBlank()) {
            throw new IllegalStateException("SMTP host is not configured");
        }

        try {
            MailConnection connection = MailConnection.open(host, port);
            try {
                expectCode(connection, "220");
                sendCommand(connection, "EHLO localhost", "250");

                // Actualiza el socket antes de enviar las credenciales cuando el relé es compatible con STARTTLS.
                if (startTls) {
                    sendCommand(connection, "STARTTLS", "220");
                    connection.upgradeToTls(host, port);
                    sendCommand(connection, "EHLO localhost", "250");
                }

                if (smtpAuth) {
                    authenticate(connection);
                }

                sendCommand(connection, "MAIL FROM:<" + from + ">", "250");
                sendCommand(connection, "RCPT TO:<" + to + ">", "250");
                sendCommand(connection, "DATA", "354");
                writeMessageData(connection.writer, from, to, subject, body, attachmentName, attachmentBytes);
                expectCode(connection, "250");
                sendCommand(connection, "QUIT", "221");
            } finally {
                connection.close();
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to send invoice email", exception);
        }
    }

    private void authenticate(MailConnection connection) throws IOException {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new IllegalStateException("SMTP authentication is enabled but credentials are missing");
        }

        sendCommand(connection, "AUTH LOGIN", "334");
        sendCommand(connection, encode(username), "334");
        sendCommand(connection, encode(password), "235");
    }

    private void writeMessageData(
            BufferedWriter writer,
            String from,
            String to,
            String subject,
            String body,
            String attachmentName,
            byte[] attachmentBytes) throws IOException {
        String boundary = "invoice-boundary-" + UUID.randomUUID();

        writer.write("Subject: " + subject + "\r\n");
        writer.write("From: " + from + "\r\n");
        writer.write("To: " + to + "\r\n");
        writer.write("MIME-Version: 1.0\r\n");
        writer.write("Content-Type: multipart/mixed; boundary=\"" + boundary + "\"\r\n");
        writer.write("\r\n");

        writer.write("--" + boundary + "\r\n");
        writer.write("Content-Type: text/plain; charset=UTF-8\r\n");
        writer.write("Content-Transfer-Encoding: 8bit\r\n");
        writer.write("\r\n");
        writer.write(body + "\r\n");

        // Adjunta el PDF, no solo una notificación.
        writer.write("--" + boundary + "\r\n");
        writer.write("Content-Type: application/pdf; name=\"" + attachmentName + "\"\r\n");
        writer.write("Content-Transfer-Encoding: base64\r\n");
        writer.write("Content-Disposition: attachment; filename=\"" + attachmentName + "\"\r\n");
        writer.write("\r\n");
        writer.write(wrapBase64(encodeBytes(attachmentBytes)));
        writer.write("\r\n--" + boundary + "--\r\n");
        writer.write(".\r\n");
        writer.flush();
    }

    private void sendCommand(MailConnection connection, String command, String expectedCode) throws IOException {
        connection.writer.write(command + "\r\n");
        connection.writer.flush();
        expectCode(connection, expectedCode);
    }

    private void expectCode(MailConnection connection, String expectedCode) throws IOException {
        String response = readResponse(connection.reader);
        if (!response.startsWith(expectedCode)) {
            throw new IllegalStateException("Unexpected SMTP response: " + response);
        }
    }

    // Los servidores SMTP suelen responder al comando EHLO con múltiples líneas, por lo que leemos hasta la línea de estado final.
    private String readResponse(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        if (line == null) {
            throw new IllegalStateException("SMTP server closed the connection");
        }

        StringBuilder response = new StringBuilder(line);
        while (line.length() > 3 && line.charAt(3) == '-') {
            line = reader.readLine();
            if (line == null) {
                break;
            }
            response.append('\n').append(line);
        }
        return response.toString();
    }

    private String encode(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String encodeBytes(byte[] value) {
        return Base64.getEncoder().encodeToString(value);
    }

    private String wrapBase64(String value) {
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < value.length(); index += 76) {
            int endIndex = Math.min(index + 76, value.length());
            builder.append(value, index, endIndex).append("\r\n");
        }
        return builder.toString();
    }

    private static final class MailConnection {
        private Socket socket;
        private BufferedReader reader;
        private BufferedWriter writer;

        private MailConnection(Socket socket) throws IOException {
            this.socket = socket;
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        }

        private static MailConnection open(String host, int port) throws IOException {
            return new MailConnection(new Socket(host, port));
        }

        private void upgradeToTls(String host, int port) throws IOException {
            SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket secureSocket = (SSLSocket) sslSocketFactory.createSocket(socket, host, port, true);
            secureSocket.startHandshake();
            socket = secureSocket;
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        }

        private void close() throws IOException {
            socket.close();
        }
    }
}
