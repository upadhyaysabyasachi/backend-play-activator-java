package controllers;

import actors.UserProfileInsertActor;
import actors.UserProfileInsertActorTemp;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.fasterxml.jackson.databind.JsonNode;
import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import models.FBUser;
import models.tempFBUser;
import play.libs.Akka;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;
import scala.collection.immutable.Map;
import scala.compat.java8.FutureConverters;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import views.html.index;
import akka.actor.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
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
        actorSystem.actorOf( Props.create( UserProfileInsertActorTemp.class ), "UserProfileInsertActorTemp" );
    }

    public Result index() {
        return ok(index.render("Your new application is ready."));
    }

    public String returnResponse(Object obj){
        JSONObject jsonobj = new JSONObject();
        jsonobj.put("user_sabera_id", (String) obj);
        return jsonobj.toJSONString();
    }


    public CompletionStage<Result> storeFBCredentials() {

        ActorSelection userProfileInsertActorInstance =
                actorSystem.actorSelection( "/user/UserProfileInsertActorTemp" );
        System.out.println("___________________");
        System.out.println("request body in string as "+request().body().asText());
        //JsonNode request = request().body().asJson();
        JsonNode fbDetails = request().body().asJson();
        System.out.println(fbDetails.toString());
        String fbemail = fbDetails.get("fb_email").toString();
        //String alternativeemail = fbDetails.get("alternative_email").toString();
        //String dob = fbDetails.get("dob").toString();
        String gender = fbDetails.get("sex").toString();
        //String firstName = fbDetails.get("firstName").toString();
        //String lastName = fbDetails.get("lastName").toString();
        String fullName = fbDetails.get("fullName").toString();
        //String preferredCategories = fbDetails.get("preferredCat").toString();
        String fb_id = fbDetails.get("fb_id").toString();

        //val jsonString = write(jsonClass)



        return FutureConverters.toJava(ask(userProfileInsertActorInstance, new tempFBUser(fbemail,fb_id,fullName,gender),100000))
                .thenApply(response -> ok(returnResponse(response)));
        //userProfileInsertActor.tell(new FBUser(fbemail,alternativeemail,dob,gender,firstName,lastName), userProfileInsertActor);

        //return ok("200");

    }



}
