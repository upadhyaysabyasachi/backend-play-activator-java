package controllers;

import actors.UserProfileInsertActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.fasterxml.jackson.databind.JsonNode;
import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import models.FBUser;
import play.libs.Akka;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by sabyasachi.upadhyay on 21/08/16.
 */
public class FBLoginController extends Controller {

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */

    static{
        BoneCPConfig config = new BoneCPConfig();
        BoneCP connectionPool = null;
        try {
            connectionPool = new BoneCP(config);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            Connection conn = connectionPool.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Result index() {
        return ok(index.render("Your new application is ready."));
    }

    public Result storeFBCredentials() {
        JsonNode request = request().body().asJson();
        JsonNode fbDetails = request().body().asJson();
        String fbemail = fbDetails.get("fb_email").toString();
        String alternativeemail = fbDetails.get("alternative_email").toString();
        String dob = fbDetails.get("dob").toString();
        String gender = fbDetails.get("sex").toString();
        String firstName = fbDetails.get("firstName").toString();
        String lastName = fbDetails.get("lastname").toString();

        ActorRef userProfileInsertActor = Akka.system().actorOf(
                Props.create(UserProfileInsertActor.class).withDispatcher("mkmk"));

        userProfileInsertActor.tell(new FBUser(fbemail,alternativeemail,dob,gender,firstName,lastName), userProfileInsertActor);

        return ok("200");

    }

}
