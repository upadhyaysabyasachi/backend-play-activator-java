package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import play.mvc.*;

import views.html.*;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
    public Result index() {
        return ok(index.render("Your new application is ready."));
    }

    public Result storeFBCredentials() {
        JsonNode request = request().body().asJson();  JsonNode fbDetails = request().body().asJson();

        String email = fbDetails.get("email").toString();
        String age = fbDetails.get("age").toString();
        String gender = fbDetails.get("sex").toString();
        String firstName = fbDetails.get("firstName").toString();
        String lastName = fbDetails.get("lastname").toString();


        return ok("200");
        
    }

}
