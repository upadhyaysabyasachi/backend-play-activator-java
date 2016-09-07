package actors;

import akka.actor.UntypedActor;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Sender;
import com.jolbox.bonecp.BoneCP;
import models.ChatObject;
import models.DBConnectionPool;
import models.Questions;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.sql.*;
import java.util.*;

import static org.reflections.util.ConfigurationBuilder.build;

/**
 * Created by sabyasachi.upadhyay on 04/09/16.
 */
public class storeChatActor extends UntypedActor {


    public String insertChatQueryBuilder(ChatObject chobj){
        return "INSERT INTO conversations(uid_sender," +
                "                    uid_receiver," +
                "                    message, time_stamp) values(" +
                chobj.uid_sender+","+chobj.uid_receiver+",'"+chobj.message+"',"+chobj.timestamp+")";

    }


    @Override
    public void onReceive(Object message) throws Throwable {

        {


            System.out.println("inside the actor on receive - Question post actor");
            Connection conn = null;

            if (!(message instanceof ChatObject)) {

            } else {
                BoneCP pool = DBConnectionPool.getConnectionPool();
                try {

                    if (pool != null) {
                        conn = pool.getConnection();
                        if(conn != null){
                            ChatObject chat = (ChatObject) message;
                            try{
                                System.out.println("Connection successful inside storeChat!");
                                Statement stmt = conn.createStatement();
                                System.out.println("query is " + insertChatQueryBuilder(chat));
                                PreparedStatement ps = conn.prepareStatement(insertChatQueryBuilder(chat),
                                        Statement.RETURN_GENERATED_KEYS);
                                int a = ps.executeUpdate(); // do something with the connection.
                                ResultSet key = ps.getGeneratedKeys();
                                if (key.next()) {
                                    String chatid = key.getInt(1) + "";
                                    JSONObject jobj = new JSONObject();
                                    jobj.put("chat_id", chatid);
                                    jobj.put("message", chat.message);
                                    jobj.put("uid_sender",chat.uid_sender);
                                    jobj.put("uid_receiver",chat.uid_receiver);
                                    jobj.put("flag","chat");
                                    jobj.put("time_stamp",chat.timestamp);
                                    jobj.put("status","success");

                                    JSONArray regId_receiver = gcmSenderActor.getRegistrationToken(chat.uid_receiver);
                                    //JSONArray regId_answerer = getRegistrationToken(uid_answerer);

                                    ArrayList<String> devices = new ArrayList<String>();

                                    for(int i=0; i< regId_receiver.size(); i++){
                                        if(regId_receiver.get(i)!=null){
                                            devices.add(regId_receiver.get(i).toString());
                                        }

                                    }

                                    //Sending the chat
                                    System.out.println("Sending the chat");
                                    final Sender sender = new Sender("AIzaSyAwlhqfNKiK1HCjS3bzNh7XbrseeOzCtcY");
                                    com.google.android.gcm.server.MulticastResult result = null;

                                    final Message pushMessage = new Message
                                            .Builder()
                                            .timeToLive(30)
                                            .delayWhileIdle(true)
                                            .addData("date", new java.util.Date().getTime() + "")
                                            .addData("message", jobj.toJSONString())
                                            .build();

                                    System.out.println("message" + pushMessage.getData().toString());
                                    System.out.println("tokens to which messages have to be sent are " + String.valueOf(devices));
                                    //Logger.info("entered2 : " + regids.size());
                                    try {
                                        result = sender.send(pushMessage, devices, 1);
                                        System.out.println("notification sent");
                                    } catch (final IOException e) {
                                        e.printStackTrace();
                                    }


                                    System.out.println("response is "+jobj.toJSONString());

                                    getSender().tell(jobj.toJSONString(), self());
                                }

                            }catch (SQLException se){
                                se.printStackTrace();
                                JSONObject jobj = new JSONObject();
                                jobj.put("status","failure");
                                getSender().tell(jobj.toJSONString(),self());
                            }
                        }

                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                    getSender().tell("failure",self());
                } finally {
                    if (conn != null) {
                        try {
                            conn.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        }

    }
}
