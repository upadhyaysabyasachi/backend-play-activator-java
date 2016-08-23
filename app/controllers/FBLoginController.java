package controllers;

import actors.UserProfileInsertActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.fasterxml.jackson.databind.JsonNode;
import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import models.FBUser;
import play.libs.Akka;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;
import scala.compat.java8.FutureConverters;
import views.html.index;
import akka.actor.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CompletionStage;
import play.libs.F;
import play.libs.F.Promise;

import static akka.pattern.Patterns.ask;

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



    static ActorSystem actorSystem = ActorSystem.create( "play" );

    static {
        // Create our local actors
        actorSystem.actorOf( Props.create( UserProfileInsertActor.class ), "UserProfileInsertActor" );
    }

    public Result index() {
        return ok(index.render("Your new application is ready."));
    }

    public CompletionStage<Result> storeFBCredentials() {

        ActorSelection userProfileInsertActorInstance =
                actorSystem.actorSelection( "/user/UserProfileInsertActor" );
        JsonNode request = request().body().asJson();
        JsonNode fbDetails = request().body().asJson();
        String fbemail = fbDetails.get("fb_email").toString();
        String alternativeemail = fbDetails.get("alternative_email").toString();
        String dob = fbDetails.get("dob").toString();
        String gender = fbDetails.get("sex").toString();
        String firstName = fbDetails.get("firstName").toString();
        String lastName = fbDetails.get("lastName").toString();


        return FutureConverters.toJava(ask(userProfileInsertActorInstance, new FBUser(fbemail,alternativeemail,dob,gender,firstName,lastName),100000))
                .thenApply(response -> ok((String) response));
        //userProfileInsertActor.tell(new FBUser(fbemail,alternativeemail,dob,gender,firstName,lastName), userProfileInsertActor);

        //return ok("200");

    }



}
