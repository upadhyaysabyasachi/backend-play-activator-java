package controllers;
import controllers.SendEmailUtil;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import com.sun.mail.smtp.*;

public class MailSender {


    public static void main(String[] args){
        try
        {


            // Recipient's email ID needs to be mentioned.
            String to = "imeavinash@gmail.com";

            // Sender's email ID needs to be mentioned
            String from = "no-reply@gmail.com";

            // Assuming you are sending email from localhost
            String host = "smtp.gmail.com";

            // Get system properties
            Properties properties = System.getProperties();

            // Setup mail serverx
            properties.put("mail.smtp.host", host);
            properties.put("mail.smtp.starttls.enable", true);
            properties.put("mail.smtp.auth",false);
            properties.put("mail.smtp.port", "25");

            // Get the default Session object.
            Session session = Session.getDefaultInstance(properties);

            SMTPMessage mess = new SMTPMessage(session);
            mess.setFrom(new InternetAddress(from));
            mess.addRecipient(Message.RecipientType.TO, new InternetAddress(to));;
            mess.setSubject("Thanks for registering on our website!");
            mess.setText("Welcome To Job Portal !!!!  Again Thanks ");



                // Create a default MimeMessage object.
                MimeMessage message = new MimeMessage(session);

                // Set From: header field of the header.
                message.setFrom(new InternetAddress(from));

                // Set To: header field of the header.
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

                // Set Subject: header field
                message.setSubject("Thanks for registering on our website!");

                // Now set the actual message
                message.setText("Welcome To Job Portal !!!!  Again Thanks ");

               Transport tr = session.getTransport("smtp");



                // Send message
                tr.send(mess);
                System.out.println("Sent message successfully....");
            }catch (MessagingException mex) {
                mex.printStackTrace();
            }




    }

    }
