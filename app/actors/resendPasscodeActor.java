package actors;

import akka.actor.UntypedActor;
import com.jolbox.bonecp.BoneCP;
import models.DBConnectionPool;
import org.json.simple.JSONObject;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.*;

/**
 * Created by sabyasachi.upadhyay on 13/09/16.
 */
public class resendPasscodeActor extends UntypedActor {



    @Override
    public void onReceive(Object message) throws Throwable {
        if(message instanceof String){
            String email = (String)message;
            JSONObject jobj = checkRegistrationActor.generatePasscode(email);
            getSender().tell(jobj.toJSONString(),getSelf());

        }
    }
}
