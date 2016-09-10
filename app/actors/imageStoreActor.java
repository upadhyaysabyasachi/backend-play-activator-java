package actors;

import akka.actor.UntypedActor;
import play.mvc.Http;

/**
 * Created by sabyasachi.upadhyay on 08/09/16.
 */
public class imageStoreActor extends UntypedActor {
    @Override
    public void onReceive(Object message) throws Throwable {
        if(message instanceof Http.RequestBody){
            Http.RequestBody body = (Http.RequestBody)message;
            byte[] input = body.toString().getBytes();


        }
    }
}
