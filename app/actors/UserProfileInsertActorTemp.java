/*
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
import models.tempFBUser;
import play.Play.*;
import play.mvc.*;
import play.db.*;

*/
/**
 * Created by sabyasachi.upadhyay on 21/08/16.
 *//*

public class UserProfileInsertActorTemp  extends UntypedActor {

    public String insertQueryBuilder(tempFBUser user){

        return "INSERT INTO user_profiles(fb_email,fb_id,fullName,gender) values("+user.fbemail+","+
                user.fbid+","+user.fullName+","+user.sex+")";

    }


    @Override
    public void onReceive(Object message) throws Throwable {

        System.out.println("inside the actor on receive");
        Connection conn = null;

        if (!(message instanceof tempFBUser)) {

        } else {
            BoneCP pool = DBConnectionPool.getConnectionPool();
            try {
                tempFBUser user = (tempFBUser)message;
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
*/
