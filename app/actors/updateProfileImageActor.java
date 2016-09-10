package actors;

import akka.actor.UntypedActor;
import com.jolbox.bonecp.BoneCP;
import models.ChatObject;
import models.DBConnectionPool;
import org.json.simple.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by sabyasachi.upadhyay on 08/09/16.
 */
public class updateProfileImageActor extends UntypedActor {

    public String updateImageQuery(String photo, String uid) {
        return "UPDATE user_profiles SET image=" + photo + " where userid = " + uid;
    }

    @Override
    public void onReceive(Object message) throws Throwable {
        if (message instanceof JSONObject) {
            JSONObject jobj = (JSONObject) message;
            String photo = jobj.get("image_string").toString();
            String uid = jobj.get("userid").toString();

            BoneCP pool = DBConnectionPool.getConnectionPool();
            Connection conn = null;
            try {
                if (pool != null) {
                    conn = pool.getConnection();
                    if (conn != null) {

                        System.out.println("Connection successful inside update image!");
                        Statement stmt = conn.createStatement();
                        //System.out.println("query is " + (chat));
                        System.out.println("query is " + updateImageQuery(photo,uid));
                        PreparedStatement ps = conn.prepareStatement(updateImageQuery(photo, uid),
                                Statement.RETURN_GENERATED_KEYS);
                        int a = ps.executeUpdate();

                        JSONObject status = new JSONObject();
                        jobj.put("status","success");
                        getSender().tell(jobj.toJSONString(),getSelf());
                    }
                }
            } catch (SQLException sqe) {
                sqe.printStackTrace();
            } finally {
                if (conn != null) {
                    conn.close();
                }
            }
        }
    }
}
