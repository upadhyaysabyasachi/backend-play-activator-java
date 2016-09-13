package actors;

import akka.actor.UntypedActor;
import com.jolbox.bonecp.BoneCP;
import models.DBConnectionPool;
import models.NormalUser;
import org.json.simple.JSONObject;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.*;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

/**
 * Created by sabyasachi.upadhyay on 13/09/16.
 */
public class checkRegistrationActor extends UntypedActor {

    public static String  checkQueryEmailBuilder(String email){
        return "SELECT userid FROM user_profiles WHERE email = '" + email+"'";
    }

    public static String insertRegistrationEmail(String email, String passcode){
        return "INSERT INTO USER_PROFILES(email,passcode) values('"+email+"','"+passcode+"')";
    }

    public static JSONObject generatePasscode(String email) throws SQLException {
        //SecureRandom random = new SecureRandom();

        char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 5; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        String output = sb.toString();
        System.out.println(output);
        String passcode = output;
        //System.out.println("query is " + insertRegistrationEmail(email,passcode));
            JSONObject jobj = new JSONObject();
            jobj.put("email",email);
            jobj.put("passcode",passcode);
            jobj.put("status","new");
            return jobj;
    }


    public static void sendMail(String email, String passcode){

        //1. send the passcode in mail first
        try
        {
            // Recipient's email ID needs to be mentioned.
            String to = email;

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
            Session session = Session.getInstance(properties, auth);

            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress("noreply@sabera.com","Sabera"));


            // Set To: header field of the header.
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setReplyTo(InternetAddress.parse("noreply@sabera.com", false));
            message.setHeader("from","noreply@sabera.com");

            // Set Subject: header field
            message.setSubject("Passcode for Sabera");
            message.setText("Registration passcode for Sabera : " + passcode);

            Transport.send(message);

            System.out.println("Sent message successfully....");
        }catch (MessagingException mex) {
            System.out.println("exception MessagingException");
            mex.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            System.out.println("exception UnsupportedEncodingException");
            e.printStackTrace();
        }

    }

    @Override
    public void onReceive(Object message) throws Throwable {
        if(message instanceof String){
            String email = (String)message;
            BoneCP pool = DBConnectionPool.getConnectionPool();
            Connection conn = null;
            try{
                conn = pool.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(checkQueryEmailBuilder(email));
                if(getRowCount(rs) > 0){
                    JSONObject jobj = new JSONObject();
                    jobj.put("status","old");
                    getSender().tell(jobj.toJSONString(),getSelf());

                }else{
                    //new user
                    JSONObject  jobj = new JSONObject();

                    jobj = generatePasscode(email);

                    sendMail(email,jobj.get("passcode").toString());
                    System.out.println("mail sent");

                    getSender().tell(jobj.toJSONString(),getSelf());
                    System.out.println("sent the jsonstring");

                }
            }catch(SQLException sqe){
                sqe.printStackTrace();
            }finally {
                if(conn!=null){
                    conn.close();
                }

            }


        }
    }


    private static int getRowCount(ResultSet resultSet) {
        if (resultSet == null) {
            return 0;
        }
        try {
            resultSet.last();
            return resultSet.getRow();
        } catch (SQLException exp) {
            exp.printStackTrace();
        } finally {
            try {
                resultSet.beforeFirst();
            } catch (SQLException exp) {
                exp.printStackTrace();
            }
        }
        return 0;
    }
}
