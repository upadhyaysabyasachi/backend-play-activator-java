package controllers;

import actors.UserProfileInsertActor;
import actors.UserProfileInsertActorTemp;
import actors.checkIfUserExistsActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.fasterxml.jackson.databind.JsonNode;
import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import models.*;
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
public class MainController extends Controller {

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
        actorSystem.actorOf( Props.create( checkIfUserExistsActor.class ), "checkIfUserExistsActor" );
        actorSystem.actorOf( Props.create( checkIfUserExistsActor.class ), "QuestionPostActor" );
        actorSystem.actorOf( Props.create( checkIfUserExistsActor.class ), "registerUserActor");
    }

    public Result index() {
        return ok(index.render("Your new application is ready."));
    }

    public String returnResponse(Object obj){
        JSONObject jsonobj = new JSONObject();
        jsonobj.put("user_sabera_id", (String) obj);
        return jsonobj.toJSONString();
    }



    public CompletionStage<Result> checkUserIfFbUserExists(){

        ActorSelection checkifUserExistsActorInstance =
                actorSystem.actorSelection( "/user/checkIfUserExistsActor" );
        JsonNode fbDetails = request().body().asJson();
        String email = fbDetails.get("email").toString();

        return FutureConverters.toJava(ask(checkifUserExistsActorInstance, email,100000))
                .thenApply(response -> ok(((JSONObject)response).toJSONString()));
    }

    public CompletionStage<Result> checkIfSaberaUserIfExists(){

        ActorSelection checkifUserExistsActorInstance =
                actorSystem.actorSelection( "/user/checkIfUserExistsActor" );
        JsonNode fbDetails = request().body().asJson();
        String email = fbDetails.get("email").toString();
        String password = fbDetails.get("password").toString();

        return FutureConverters.toJava(ask(checkifUserExistsActorInstance, new NormalUser(email,password),100000))
                .thenApply(response -> ok(((JSONObject)response).toJSONString()));
    }


    public CompletionStage<Result> registerNormalUser(){
        ActorSelection registerNewUser =
                actorSystem.actorSelection("/user/registerUserActor");

        JsonNode fbDetails = request().body().asJson();
        String email = fbDetails.get("email").toString();
        String password = fbDetails.get("password").toString();

        return FutureConverters.toJava(ask(registerNewUser, new RegisteredUser(email,password),100000))
                .thenApply(response -> ok(((JSONObject)response).toJSONString()));
    }


    /*public CompletionStage<Result> storeFBCredentials() {



    }
*/


    public CompletionStage<Result> storeCredentials() {

        ActorSelection userProfileInsertActorInstance =
                actorSystem.actorSelection("/user/UserProfileInsertActor");

        JsonNode fbDetails = request().body().asJson();

        System.out.println("___________________");
        System.out.println("request body in string as "+request().body().asText());
        System.out.println(fbDetails.toString());
        String uid = fbDetails.get("uid").toString();
        String dob = fbDetails.get("dob").toString();
        String gender = fbDetails.get("sex").toString();
        String fullName = fbDetails.get("fullName").toString();
        String preferredCategories = fbDetails.get("preferredCat").toString();

        return FutureConverters.toJava(ask(userProfileInsertActorInstance, new GenUser(uid,dob,gender,fullName,preferredCategories),100000))
                .thenApply(response -> ok(response.toString()));

    }

    public CompletionStage<Result> postQuestion(){


        ActorSelection QuestionPostActorInstance =
                actorSystem.actorSelection( "/user/QuestionPostActor" );


        JsonNode questionDetails = request().body().asJson();
        String question = questionDetails.get("question").toString();
        String uid = questionDetails.get("userid").toString();
        String qtype = questionDetails.get("qtype").toString();
        String proposed_answer = questionDetails.get("proposed_answer").toString();
        String keywords = questionDetails.get("keywords").toString();
        String hints = questionDetails.get("hints").toString();
        String timer = questionDetails.get("timer").toString();
        String option1 = "null";
        String option2 = "null";
        String option3 = "null";
        String option4 = "null";

        if(qtype.equalsIgnoreCase("objective")){
            //fetch the options
            option1 = questionDetails.get("option1").toString();
            option2 = questionDetails.get("option2").toString();
            option3 = questionDetails.get("option3").toString();
            option4 = questionDetails.get("option4").toString();
        }
        /* this.userid = param1;
        this.qtype = param2;
        this.qstring = param3;
        this.proposed_answer = param4;
        this.keywords = param5;
        this.option1 = param6;
        this.option2 = param7;
        this.option3 = param8;
        this.option4 = param9;
        */

        return FutureConverters.toJava(ask(QuestionPostActorInstance, new Questions(uid,qtype,question,proposed_answer,keywords,
                option1,option2,option3,option4,hints, timer),100000))
                .thenApply(response -> ok(returnResponse(response)));
    }


}
