package actors;

import akka.actor.UntypedActor;
import com.jolbox.bonecp.BoneCP;
import models.ChatObject;
import models.DBConnectionPool;
import models.Questions;
import org.json.simple.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

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
                                JSONObject jobj = new JSONObject();
                                jobj.put("status","success");
                                getSender().tell(jobj.toJSONString(),self());
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
