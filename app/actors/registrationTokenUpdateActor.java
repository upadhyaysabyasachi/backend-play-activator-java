package actors;

import akka.actor.UntypedActor;
import com.jolbox.bonecp.BoneCP;
import models.DBConnectionPool;
import models.UserWithDevice;
import org.json.simple.JSONObject;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by sabyasachi.upadhyay on 02/09/16.
 */
public class registrationTokenUpdateActor extends UntypedActor {

    public String insertQueryDeviceRegistrationBuilder(UserWithDevice user) {
        return "INSERT INTO user_sessions(userid,device_token,login_status) values(" + user.uid + "," + user.device_id + ","+"'yes')";
    }

    @Override
    public void onReceive(Object message) throws Throwable {
        //message contains the userid and the registrationToken
        if (message instanceof UserWithDevice) {
            {
                BoneCP pool = DBConnectionPool.getConnectionPool();
                Connection conn = pool.getConnection();
                try {
                    UserWithDevice user = (UserWithDevice) message;

                    if (conn != null) {
                        System.out.println("Connection successful inside registrationTokenActor!");
                        Statement stmt = conn.createStatement();
                        System.out.println("query is " + insertQueryDeviceRegistrationBuilder(user));

                        //ResultSet rs = stmt.executeQuery(insertQueryRegisterBuilder(user));
                        //System.out.println("query is " + insertQueryRegisterBuilder(user));
                        int a = stmt.executeUpdate(insertQueryDeviceRegistrationBuilder(user));
                        //PreparedStatement ps = conn.prepareStatement(insertQueryRegisterBuilder(user),
                        //        Statement.RETURN_GENERATED_KEYS);
                        //int a = ps.executeUpdate(); // do something with the connection.
                        //ResultSet key = ps.getGeneratedKeys();
                        JSONObject obj = new JSONObject();
                        obj.put("status", "inserted");
                        getSender().tell(obj.toJSONString(), self());
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
}
