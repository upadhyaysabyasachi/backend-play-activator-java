package controllers;

import actors.UserProfileInsertActor;
import actors.UserProfileInsertActorTemp;
import actors.checkIfUserExistsActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.fasterxml.jackson.databind.JsonNode;
import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import models.DBConnectionPool;
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.concurrent.CompletionStage;
import play.libs.F;
import play.libs.F.Promise;

import static akka.pattern.Patterns.ask;
import filters.CheckForFirstTimeUser;

/**
 * Created by sabyasachi.upadhyay on 21/08/16.
 */
public class QuestionPostController extends Controller {

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */

    static ActorSystem actorSystem = ActorSystem.create("play");

    static {
        // Create our local actors
        actorSystem.actorOf( Props.create( UserProfileInsertActor.class ), "UserProfileInsertActor" );
        actorSystem.actorOf( Props.create( checkIfUserExistsActor.class ), "checkIfUserExists" );
    }

    public Result index() {
        return ok(index.render("Your new application is ready."));
    }

    public String returnResponse(Object obj){
        JSONObject jsonobj = new JSONObject();
        jsonobj.put("user_sabera_id", (String) obj);
        return jsonobj.toJSONString();
    }

    public CompletionStage<Result> checkUserIfExists(){

        ActorSelection checkifUserExistsActorInstance =
                actorSystem.actorSelection( "/user/UserProfileInsertActorTemp" );
        JsonNode fbDetails = request().body().asJson();
        String fb_id = fbDetails.get("fb_id").toString();

        return FutureConverters.toJava(ask(checkifUserExistsActorInstance, fb_id,100000))
                .thenApply(response -> ok(((JSONObject)response).toJSONString()));
    }



}
