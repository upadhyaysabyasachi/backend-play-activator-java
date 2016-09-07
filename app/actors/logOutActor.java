package actors;

import akka.actor.UntypedActor;
import com.jolbox.bonecp.BoneCP;
import models.DBConnectionPool;
import org.json.simple.JSONObject;

import java.sql.*;

/**
 * Created by sabyasachi.upadhyay on 07/09/16.
 */
public class logOutActor extends UntypedActor {


    public JSONObject update(String uid, String token){

        String query = "UPDATE user_sessions SET login_status = 'no' where userid = " + uid + " and device_token = " + token;
        Connection conn = null;
        BoneCP pool = DBConnectionPool.getConnectionPool();
        try {
            conn = pool.getConnection();
            PreparedStatement ps = conn.prepareStatement(query,
                    Statement.RETURN_GENERATED_KEYS);
            int a = ps.executeUpdate(); // do something with the connection.
            ResultSet key = ps.getGeneratedKeys();
            JSONObject jobj = new JSONObject();
            jobj.put("status","success");
            return jobj;
        } catch (SQLException e) {
            e.printStackTrace();
        }finally{
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        JSONObject jobj = new JSONObject();
        jobj.put("status","failure");
        return jobj;
    }





    @Override
    public void onReceive(Object message) throws Throwable {
        if(message instanceof JSONObject){
            JSONObject logout = (JSONObject)message;
            String uid =  logout.get("uid").toString();
            String token = logout.get("device_token").toString();

            //update the login_status to false
            JSONObject status = update(uid,token);
            getSender().tell(status.toJSONString(),getSelf());

        }

    }
}
