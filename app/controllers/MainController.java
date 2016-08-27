package controllers;

import actors.QuestionPostActor;
import actors.UserProfileInsertActor;
//import actors.UserProfileInsertActorTemp;
import actors.checkIfUserExistsActor;
import actors.registerUserActor;
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
        actorSystem.actorOf( Props.create( QuestionPostActor.class ), "QuestionPostActor" );
        actorSystem.actorOf( Props.create( registerUserActor.class ), "registerUserActor");
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
                .thenApply(response -> ok(response.toString()));
    }

    public CompletionStage<Result> checkIfSaberaUserExists(){

        ActorSelection checkifUserExistsActorInstance =
                actorSystem.actorSelection( "/user/checkIfUserExistsActor" );
        JsonNode fbDetails = request().body().asJson();
        String email = fbDetails.get("email").toString();
        String password = fbDetails.get("password").toString();

        return FutureConverters.toJava(ask(checkifUserExistsActorInstance, new NormalUser(email,password),100000))
                .thenApply(response -> ok((response.toString())));
    }


    public CompletionStage<Result> registerNormalUser(){
        ActorSelection registerNewUser =
                actorSystem.actorSelection("/user/registerUserActor");

        JsonNode fbDetails = request().body().asJson();
        String email = fbDetails.get("email").toString();
        String password = fbDetails.get("password").toString();

        return FutureConverters.toJava(ask(registerNewUser, new RegisteredUser(email,password),100000))
                .thenApply(response -> ok((response.toString())));
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
        String uid = fbDetails.get("userid").toString();
        String dob = fbDetails.get("dob").toString();
        String gender = fbDetails.get("sex").toString();
        String fullName = fbDetails.get("fullname").toString();
        String preferredCategories = fbDetails.get("preferred_categories").toString();

        return FutureConverters.toJava(ask(userProfileInsertActorInstance, new GenUser(uid,dob,gender,fullName,preferredCategories),100000))
                .thenApply(response -> ok(response.toString()));

    }

    public CompletionStage<Result> postQuestion(){


        ActorSelection QuestionPostActorInstance =
                actorSystem.actorSelection( "/user/QuestionPostActor" );


        JsonNode questionDetails = request().body().asJson();
        String question = questionDetails.get("question").toString();
        String uid = questionDetails.get("userid").toString();
        String qtype = questionDetails.get("qtype").toString().substring(1,questionDetails.get("qtype").toString().length()-1);
        String proposed_answer = null;

        System.out.println("qtype " + qtype);
        if(qtype.equalsIgnoreCase("subjective")) {
            proposed_answer = questionDetails.get("proposed_answer").toString();
            System.out.println("subjective questions " + proposed_answer);
        }

        //String proposed_answer = questionDetails.get("proposed_answer").toString();
        String keywords = null;
        if(qtype.equalsIgnoreCase("subjective")){
            keywords = questionDetails.get("keywords").toString();
        }

        String hints = questionDetails.get("hints").toString();
        String timer = questionDetails.get("timer").toString();
        String post_time = questionDetails.get("post_time").toString();
        String categories = questionDetails.get("categories").toString();

        String option1 = null;
        String option2 = null;
        String option3 = null;
        String option4 = null;

        String status1 = null;
        String status2 = null;
        String status3 = null;
        String status4 = null;


        if(qtype.equalsIgnoreCase("objective")){
            //fetch the options
            //questionDetails.get("options").iterator()
            option1 = questionDetails.get("option1").toString();
            option2 = questionDetails.get("option2").toString();
            option3 = questionDetails.get("option3").toString();
            option4 = questionDetails.get("option4").toString();
            //status1 = questionDetails.get("option1").toString();;
            status1 = questionDetails.get("status1").toString();
            status2 = questionDetails.get("status2").toString();
            status3 = questionDetails.get("status3").toString();
            status4 = questionDetails.get("status4").toString();
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

        System.out.println("proposed answer "+proposed_answer);
        System.out.println("hints given is " + hints);
        return FutureConverters.toJava(ask(QuestionPostActorInstance, new Questions(uid,qtype,question,proposed_answer,keywords,
                option1,option2,option3,option4,status1,status2,status3,status4,hints,timer,post_time, categories),100000))
                .thenApply(response -> ok(response.toString()));
    }


}