package actors;

import akka.actor.UntypedActor;
import com.jolbox.bonecp.BoneCP;
import models.DBConnectionPool;
//import models.FBUser;
import models.Questions;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import scala.compat.java8.FutureConverters;

import java.sql.*;

import static akka.pattern.Patterns.ask;

/**
 * Created by sabyasachi.upadhyay on 25/08/16.
 */
public class QuestionPostActor extends UntypedActor {


    public String checkQueryBuilder(String fb_id){
        return "SELECT TOP 1 fb_id FROM user_profiles WHERE fb_id = " + fb_id+" group by fb_id";
    }


    public static JSONArray loadQuestions(String uid, Connection conn) throws SQLException{
        Statement stmt = conn.createStatement();
        //without filters
        JSONArray arr = new JSONArray();
        ResultSet rs = stmt.executeQuery("select qstring,qtype,proposed_answer,proposed_keywords,hints,timer,option1,option2,option3,option4 from questions join " +
                " where user not like  '" + uid + "' limit 10");
        while(rs.next()){
            JSONObject obj = new JSONObject();
            obj.put("qstring",rs.getString("qstring"));
            obj.put("qtypes",rs.getString("qtype"));
            obj.put("proposed_answer",rs.getString("proposed_answer"));
            obj.put("hints",rs.getString("hints"));
            obj.put("timer",rs.getString("timer"));
            obj.put("option1",rs.getString("option1"));
            obj.put("option2",rs.getString("option2"));
            obj.put("option3",rs.getString("option3"));
            obj.put("option4",rs.getString("option4"));
            arr.add(obj);
        }

        JSONObject mainObj = new JSONObject();
        mainObj.put("questions", arr);
        conn.close();
        return arr;

    }

    public String insertQuestionQueryBuilder(Questions q){
        return "INSERT INTO questions(userid,qtype,qstring,answer,keywords,option1,option2,option3,option4) " +
                "values(" + q.userid+","+q.qtype+","+q.qstring+","+q.proposed_answer+","+q.keywords+","+q.option1+","+q.option2+","+
                q.option3+","+q.option4+")";
    }

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
        {

            System.out.println("inside the actor on receive");
            Connection conn = null;

            if (!(message instanceof String)) {

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
                                getSender().tell(a,self());
                            }catch (SQLException se){
                                se.printStackTrace();
                            }
                        }

                    }

                } catch (SQLException e) {
                    e.printStackTrace();
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
    }
}

