package actors;

import akka.actor.UntypedActor;

import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import models.DBConnectionPool;
import models.FBUser;
import play.Play.*;
import play.mvc.*;
import play.db.*;

/**
 * Created by sabyasachi.upadhyay on 21/08/16.
 */
public class UserProfileInsertActor  extends UntypedActor {

    @Override
    public void onReceive(Object message) throws Throwable {

        if (!(message instanceof FBUser)) {

        } else {

            Connection conn = DBConnectionPool.getConnection();
            try {
                FBUser user = (FBUser)message;
                if (conn != null) {
                    System.out.println("Connection successful!");
                    Statement stmt = conn.createStatement();

                    ResultSet rs = stmt.executeQuery("INSERT INTO user_profile values('"+user.fbemail+"','"+user.altemail+"','"+
                    user.sex+"','"+user.dob+"','"+user.firstName+"','"+user.lastName); // do something with the connection.
                    while (rs.next()) {
                        System.out.println(rs.getString(1)); // should print out "1"'
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
