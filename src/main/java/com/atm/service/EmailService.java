package com.atm.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailService {

    @Value("${sendgrid.api.key:}")
    private String sendGridApiKey;

    @Value("${email.from.address:}")
    private String fromEmail;

    @Value("${email.from.name:ATM Portal}")
    private String fromName;

    public void sendEmail(String toEmail, String subject, String body) {
        // Guard: if not configured, log and continue (don’t break OTP flow)
        if (sendGridApiKey == null || sendGridApiKey.isBlank()
                || fromEmail == null || fromEmail.isBlank()) {
            System.out.println("⚠️ Email not configured. Skipping send to " + toEmail);
            return;
        }

        Email from = new Email(fromEmail, fromName);
        Email to = new Email(toEmail);
        Content content = new Content("text/plain", body);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            System.out.println("📧 Email sent to " + toEmail + " (status: " + response.getStatusCode() + ")");
        } catch (IOException ex) {
            // DO NOT throw — keep the transaction/OTP flow going
            System.out.println("❌ Failed to send email: " + ex.getMessage());
        }
    }
}
