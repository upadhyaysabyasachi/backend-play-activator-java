package actors;

import akka.actor.UntypedActor;

import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;

import java.sql.*;

import models.DBConnectionPool;
import models.FBUser;
import play.Play.*;
import play.mvc.*;
import play.db.*;

/**
 * Created by sabyasachi.upadhyay on 21/08/16.
 */
public class UserProfileInsertActor  extends UntypedActor {

    public String insertQueryBuilder(FBUser   user){

        return "INSERT INTO user_profiles(fb_email,gender,DOB,fullName,fb_id, preferred_categories) values("+user.fbemail+","+
                user.sex+","+user.dob+","+user.fullName+user.fb_id+","+user.preferredCategories+")";

    }


    @Override
    public void onReceive(Object message) throws Throwable {

        System.out.println("inside the actor on receive");
        Connection conn = null;

        if (!(message instanceof FBUser)) {

        } else {
            BoneCP pool = DBConnectionPool.getConnectionPool();
            try {
                FBUser user = (FBUser)message;
                if (pool != null) {
                    conn = pool.getConnection();
                    if(conn != null){
                        System.out.println("Connection successful!");
                        Statement stmt = conn.createStatement();
                        System.out.println("query is " + insertQueryBuilder(user));
                        PreparedStatement ps = conn.prepareStatement(insertQueryBuilder(user),
                                Statement.RETURN_GENERATED_KEYS);
                        int a = ps.executeUpdate(); // do something with the connection.
                        ResultSet key = ps.getGeneratedKeys();
                        if (key.next()) {
                            getSender().tell(key.getInt(1)+"",self());
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
