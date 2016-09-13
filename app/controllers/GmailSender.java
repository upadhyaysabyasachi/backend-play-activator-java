package controllers;
import com.sun.mail.util.TraceInputStream;
import controllers.SendEmailUtil;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import com.sun.mail.smtp.*;

public class GmailSender {


    public static void main(String[] args) throws UnsupportedEncodingException {
        try
        {
            // Recipient's email ID needs to be mentioned.
            String to = "imeavinash@gmail.com";

            // Sender's email ID needs to be mentioned
            String from = "noreplysabera@gmail.com";

            // Assuming you are sending email from localhost
            String host = "smtp.gmail.com";

            // Get system properties
            Properties properties = System.getProperties();

            // Setup mail serverx
            properties.put("mail.smtp.host", host);
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.auth","true");
            properties.put("mail.smtp.port", "587");
            //properties.put("mail.smtp.from", "xyz@123.com");

            Authenticator auth = new Authenticator() {
                //override the getPasswordAuthentication method
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(from, "kuchhbhi123");
                }
            };

            // Get the default Session object.
            Session session = Session.getDefaultInstance(properties, auth);


            /*SMTPMessage mess = new SMTPMessage(session);
            mess.setFrom(new InternetAddress(from));
            mess.addRecipient(Message.RecipientType.TO, new InternetAddress(to));;
            mess.setSubject("Thanks for registering on our website!");
            mess.setText("Welcome To Job Portal !!!!  Again Thanks ");*/



            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress("noreply@sabera.com","Sabera"));


            // Set To: header field of the header.
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setReplyTo(InternetAddress.parse("noreply@sabera.com", false));
            message.setHeader("from","noreply@sabera.com");

            // Set Subject: header field
            message.setSubject("Subject 5");


            // Now set the actual message
           Enumeration en = message.getAllHeaderLines();
            System.out.println(en);

            Transport.send(message);
            System.out.println("Sent message successfully....");
        }catch (MessagingException mex) {
            mex.printStackTrace();
        }




    }

}
