package actors;

import akka.actor.UntypedActor;

import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;

import java.sql.*;

import models.DBConnectionPool;
//import models.FBUser;
import models.GenUser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import play.Play.*;
import play.mvc.*;
import play.db.*;

/**
 * Created by sabyasachi.upadhyay on 21/08/16.
 */
public class UserProfileInsertActor  extends UntypedActor {

   public static String updateQueryBuilder(GenUser user){

        return "UPDATE user_profiles" +
                "SET sex = "+user.sex.trim()+","+
                "dob = " + user.dob.trim() + ","+
                "preferredCategories = " + user.preferredCategories.trim() + "," +
                "fullName = " + user.fullName.trim() +
                " where userid = " + user.uid.trim()+")";

    }

    //After first login, insert data and also load the questions

    @Override
    public void onReceive(Object message) throws Throwable {

        System.out.println("inside the actor on receive");
        Connection conn = null;

        if (!(message instanceof GenUser)) {

        } else {
            BoneCP pool = DBConnectionPool.getConnectionPool();
            try {
                GenUser user = (GenUser)message;
                if (pool != null) {
                    conn = pool.getConnection();
                    if(conn != null){
                        System.out.println("Connection successful!");
                        Statement stmt = conn.createStatement();
                        System.out.println("query is " + updateQueryBuilder(user));
                        PreparedStatement ps = conn.prepareStatement(updateQueryBuilder(user),
                                Statement.RETURN_GENERATED_KEYS);
                        int a = ps.executeUpdate(); // do something with the connection.
                        ResultSet key = ps.getGeneratedKeys();
                        if (key.next()) {
                            String userid = key.getInt(1)+"";
                            JSONArray arr = QuestionPostActor.loadQuestions(userid,conn);
                            JSONObject jobj = new JSONObject();
                            jobj.put("userid",userid);
                            jobj.put("questions",arr);
                            getSender().tell(jobj.toJSONString(),self());
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
