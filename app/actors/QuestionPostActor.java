package actors;

import akka.actor.UntypedActor;
import com.jolbox.bonecp.BoneCP;
import models.DBConnectionPool;
//import models.FBUser;
import models.Questions;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import play.api.Environment;
import play.api.db.ConnectionPool;
import play.api.db.HikariCPComponents;
import scala.compat.java8.FutureConverters;

import java.sql.*;

import static akka.pattern.Patterns.ask;

/**
 * Created by sabyasachi.upadhyay on 25/08/16.
 */
public class QuestionPostActor extends UntypedActor {


    public  JSONObject checkForFirstTimeUser(String fb_id) throws SQLException{
        BoneCP pool = DBConnectionPool.getConnectionPool();
        Connection conn = pool.getConnection();

        if(conn != null){
            System.out.println("Connection successful!");
            Statement stmt = conn.createStatement();
            System.out.println("query is " + checkQueryBuilder(fb_id));

            ResultSet rs = stmt.executeQuery(checkQueryBuilder(fb_id));
            int numRows = rs.getFetchSize();
            if(numRows > 0){
                System.out.println("user exists");
                while(rs.next()){
                    JSONObject jsonobj = new JSONObject();
                    jsonobj.put("fb_id", fb_id);
                    jsonobj.put("status", "exists");
                    return jsonobj;
                }
            }
            else{

                JSONObject jsonobj = new JSONObject();
                jsonobj.put("fb_id", fb_id);
                jsonobj.put("status", "new");
                return jsonobj;

            }
        }


        JSONObject jsonobj = new JSONObject();
        jsonobj.put("fb_id", fb_id);
        jsonobj.put("status", "dbconnectionfail");
        return jsonobj;




    }

    @Override
    public void onReceive(Object message) throws Throwable {


            System.out.println("inside the actor on receive - Question post actor");
            Connection conn = null;

            if (!(message instanceof Questions)) {

            } else {
                BoneCP pool = DBConnectionPool.getConnectionPool();
                try {

                    if (pool != null) {
                        conn = pool.getConnection();
                        if(conn != null){
                            Questions question = (Questions) message;
                            try{
                                System.out.println("Connection successful!");
                                Statement stmt = conn.createStatement();
                                System.out.println("query is " + insertQuestionQueryBuilder(question));
                                PreparedStatement ps = conn.prepareStatement(insertQuestionQueryBuilder(question),
                                        Statement.RETURN_GENERATED_KEYS);
                                int a = ps.executeUpdate(); // do something with the connection.
                                JSONObject jobj = new JSONObject();
                                jobj.put("status","success");
                                getSender().tell(jobj.toJSONString(),self());
                            }catch (SQLException se){
                                se.printStackTrace();
                                JSONObject jobj = new JSONObject();
                                jobj.put("status","failure");
                                getSender().tell(jobj.toJSONString(),self());
                            }
                        }

                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                    getSender().tell("failure",self());
                } finally {
                    if (conn != null) {
                        try {
                            conn.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        }


    public String checkQueryBuilder(String fb_id){
        return "SELECT TOP 1 fb_id FROM user_profiles WHERE fb_id = " + fb_id+" group by fb_id";
    }




    public String insertQuestionQueryBuilder(Questions q){

        if(q.qtype.equalsIgnoreCase("objective")){
            return "INSERT INTO questions(userid,qtype,qstring,proposed_answer,proposed_keywords,option1,option2,option3,option4,status1,status2,status3,status4," +
                    "post_time,category,hints,timer) " +
                    "values(" + q.userid+",'"+q.qtype+"',"+q.qstring+","+q.proposed_answer+","+q.keywords+","+q.option1+","+q.option2+","+
                    q.option3+","+q.option4+","+q.status1.substring(1,q.status1.length()-1)+","+q.status2.substring(1,q.status2.length()-1)+","+
                    q.status3.substring(1,q.status3.length()-1)+","+q.status4.substring(1,q.status4.length()-1)+","+q.post_time+",'"+q.categories+"',"+q.hints+"," +
                    q.timer+")";
        }
        else{
            return "INSERT INTO questions(userid,qtype,qstring,proposed_answer,proposed_keywords,option1,option2,option3,option4,status1,status2,status3,status4," +
                    "post_time,category,hints,timer) " +
                    "values(" + q.userid+",'"+q.qtype+"',"+q.qstring+","+q.proposed_answer+","+q.keywords+","+q.option1+","+q.option2+","+
                    q.option3+","+q.option4+","+q.status1+","+q.status2+","+
                    q.status3+","+q.status4+","+q.post_time+",'"+q.categories+"',"+q.hints+
                    ","+q.timer+")";
        }
    }
}


