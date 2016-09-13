package actors;

import akka.actor.UntypedActor;
import com.jolbox.bonecp.BoneCP;
import models.ChatObject;
import models.DBConnectionPool;
import models.NormalUser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import scala.util.parsing.json.JSON;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by sabyasachi.upadhyay on 04/09/16.
 */
public class loadChatActor extends UntypedActor{




    public static String getChatQueryString(ChatObject obj){

        return "select cid, uid_receiver,uid_sender, message, time_stamp from conversations where uid_receiver = " + obj.uid_receiver + " and " +
                "uid_sender = " + obj.uid_sender + "  " +
                " UNION " +
                "select cid, uid_receiver,uid_sender, message, time_stamp from conversations where uid_receiver = " + obj.uid_sender + " and " +
                "uid_sender = " + obj.uid_receiver + " order by time_stamp ";

    }

    public static JSONObject loadChats(ChatObject chobj, Connection conn) throws SQLException {
        JSONArray chatArray = new JSONArray();
        System.out.println("chat query string is " + getChatQueryString(chobj));
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(getChatQueryString(chobj));
        while (rs.next()) {
            JSONObject obj = new JSONObject();
            obj.put("uid_sender", rs.getString("uid_sender"));
            obj.put("uid_receiver", rs.getString("uid_receiver"));
            obj.put("message", rs.getString("message"));
            obj.put("time_stamp", rs.getString("time_stamp"));
            obj.put("chat_id",rs.getString("cid"));
            chatArray.add(obj);
        }
        JSONObject mainObj = new JSONObject();
        mainObj.put("chats", chatArray);
        return mainObj;
    }


    @Override
    public void onReceive(Object message) throws Throwable {

        {

            System.out.println("inside the actor on receive");
            Connection conn = null;
            if (message instanceof ChatObject) {
                //Normal User
                BoneCP pool = DBConnectionPool.getConnectionPool();
                Statement stmt = conn.createStatement();
                try {

                    if (pool != null) {
                        conn = pool.getConnection();
                        if (conn != null) {
                            ChatObject chobj = (ChatObject) message;
                            try {
                                JSONObject chats = loadChats(chobj,conn);

                                getSender().tell(chats.toJSONString(), self());
                            }catch (Exception se) {
                                se.printStackTrace();
                            }
                        }

                    }

                } catch (SQLException e) {
                    e.printStackTrace();
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
