package actors;

import akka.actor.UntypedActor;
import com.jolbox.bonecp.BoneCP;
import models.DBConnectionPool;
import models.NormalUser;
import models.RegisteredUser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Created by sabyasachi.upadhyay on 27/08/16.
 */
public class registerUserActor extends UntypedActor {

    public String insertQueryRegisterBuilder(RegisteredUser reguser){
        return "INSERT INTO user_profiles(email,password_string) values("+reguser.email+",SHA2("+reguser.password+",512))";
    }

    @Override
    public void onReceive(Object message) throws Throwable {
        if(message instanceof RegisteredUser){

                BoneCP pool = DBConnectionPool.getConnectionPool();
                Connection conn = pool.getConnection();
                RegisteredUser user = (RegisteredUser)message;

                if (conn != null) {
                    System.out.println("Connection successful!");

                    if(checkIfUserExistsActor.checkForFirstTimeNormalUser(new NormalUser(user.email,user.password)).get("status").toString().equalsIgnoreCase("old")){
                        JSONObject jobj = new JSONObject();
                        jobj.put("status","old");
                        getSender().tell(jobj.toJSONString(),self());
                    }
                    else{

                        Statement stmt = conn.createStatement();
                        System.out.println("query is " + insertQueryRegisterBuilder(user));

                        //ResultSet rs = stmt.executeQuery(insertQueryRegisterBuilder(user));
                        //System.out.println("query is " + insertQueryRegisterBuilder(user));
                        PreparedStatement ps = conn.prepareStatement(insertQueryRegisterBuilder(user),
                                Statement.RETURN_GENERATED_KEYS);
                        int a = ps.executeUpdate(); // do something with the connection.
                        ResultSet key = ps.getGeneratedKeys();
                        if (key.next()) {
                            String userid = key.getInt(1)+"";
                            JSONObject jobj = new JSONObject();
                            jobj.put("userid",userid);
                            jobj.put("email",user.email);
                            jobj.put("status","new");
                            getSender().tell(jobj.toJSONString(),self());
                        }
                    }
            }
        }
        else{
            System.out.println("did not receive registered user");
        }
    }
}
