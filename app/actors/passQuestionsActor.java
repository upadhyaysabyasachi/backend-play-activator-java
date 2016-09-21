package actors;

import akka.actor.UntypedActor;
import com.jolbox.bonecp.BoneCP;
import models.DBConnectionPool;
import models.Questions;
import org.json.simple.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by sabyasachi.upadhyay on 10/09/16.
 */
public class passQuestionsActor extends UntypedActor {

    public String insertAnswersQueryBuilder(String uid_questioner, String uid_answer, String qid, String time){

        return "INSERT INTO answers(qid," +
                "                        uid_answerer," +
                "                        uid_questioner," +
                "                        match_status," +
                "                        answer_time) "+
                " values("+qid+","+uid_answer+","+uid_questioner+",'passed',"+time+")";
    }




    @Override
    public void onReceive(Object message) throws Throwable {
        if(message instanceof JSONObject){
            System.out.println("pass this question");
            JSONObject jobj = (JSONObject)message;
            String uid_answerer = jobj.get("uid_answerer").toString();
            String uid_questioner = jobj.get("uid_questioner").toString();
            String qid = jobj.get("qid").toString();
            String time  = jobj.get("answer_time").toString();

            //update in the answers db as no match
            BoneCP pool = DBConnectionPool.getConnectionPool();
            Connection conn;
            try{
                conn = pool.getConnection();
                Statement stmt = conn.createStatement();
                System.out.println("query is " + insertAnswersQueryBuilder(uid_questioner,uid_answerer,qid,time));
                PreparedStatement ps = conn.prepareStatement(insertAnswersQueryBuilder(uid_questioner,uid_answerer,qid,time),
                        Statement.RETURN_GENERATED_KEYS);
                int a = ps.executeUpdate(); // do something with the connection.
                JSONObject jobj1 = new JSONObject();
                jobj1.put("status","success");
                getSender().tell(jobj.toJSONString(),self());

            }catch(SQLException sqe){
                sqe.printStackTrace();
            }

        }
    }
}
