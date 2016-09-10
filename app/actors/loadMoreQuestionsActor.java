package actors;

import akka.actor.UntypedActor;
import com.jolbox.bonecp.BoneCP;

import models.DBConnectionPool;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by sabyasachi.upadhyay on 10/09/16.
 */
public class loadMoreQuestionsActor extends UntypedActor {
    @Override
    public void onReceive(Object message) throws Throwable {
        if(message instanceof JSONObject){
            String uid = ((JSONObject)message).get("userid").toString();
            BoneCP pool = DBConnectionPool.getConnectionPool();
            Connection conn = null;
            try {
                conn = pool.getConnection();
                JSONArray questions = checkIfUserExistsActor.loadQuestions(uid,conn);
                JSONObject jobj = new JSONObject();
                jobj.put("questions",questions);
                getSender().tell(jobj, getSelf());
            }catch (SQLException sqe){
                sqe.printStackTrace();
            }
        }
    }
}
