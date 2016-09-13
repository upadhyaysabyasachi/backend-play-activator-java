package controllers;

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

public class SampleSender {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        final String username="admin@dineshonjava.com";
        final String password="******";
        Session session = Session.getDefaultInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username,password);
                    }
                });
        try {
            Store store = session.getStore("imaps");
            store.connect("imap.gmail.com",username, password);

            //Create a Folder object and open the folder
            Folder folder = store.getFolder("inbox");
            folder.open(Folder.READ_ONLY);
            Message message = folder.getMessage(10);
            // Get all the information from the message
            String from = InternetAddress.toString(message.getFrom());
            if (from != null) {
                System.out.println("From: " + from);
            }
            String replyTo = InternetAddress.toString(message.getReplyTo());
            if (replyTo != null) {
                System.out.println("Reply-to: " + replyTo);
            }
            String to = InternetAddress.toString(message.getRecipients(Message.RecipientType.TO));
            if (to != null) {
                System.out.println("To: " + to);
            }

            String subject = message.getSubject();
            if (subject != null) {
                System.out.println("Subject: " + subject);
            }
            Date sent = message.getSentDate();
            if (sent != null) {
                System.out.println("Sent: " + sent);
            }
            System.out.println(message.getContent());

            // compose the message to forward
            Message message2 = new MimeMessage(session);
            message2= (MimeMessage) message.reply(false);
            message2.setSubject("RE: " + message.getSubject());
            message2.setFrom(new InternetAddress(from));
            message2.setReplyTo(message.getReplyTo());

            message2.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            // Create your new message part
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText("Oiginal message:\n\n");

            // Create a multi-part to combine the parts
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);

            // Create and fill part for the forwarded content
            messageBodyPart = new MimeBodyPart();
            messageBodyPart.setDataHandler(message.getDataHandler());

            // Add part to multi part
            multipart.addBodyPart(messageBodyPart);

            // Associate multi-part with message
            message2.setContent(multipart);

            // Send message
            Transport.send(message2);

            System.out.println("message replied successfully ....");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}