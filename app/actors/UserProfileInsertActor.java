package actors;

import akka.actor.UntypedActor;

import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;

import java.sql.*;

import models.DBConnectionPool;
//import models.FBUser;
import models.GenUser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import play.Play.*;
import play.mvc.*;
import play.db.*;

/**
 * Created by sabyasachi.upadhyay on 21/08/16.
 */
public class UserProfileInsertActor  extends UntypedActor {

   public static String updateQueryBuilder(GenUser user){

        return "UPDATE user_profiles " +
                "SET sex = "+user.sex.trim()+","+
                "dob = " + user.dob.trim() + ","+
                "preferred_categories = " + user.preferredCategories.trim() + "," +
                "fullname = " + user.fullName.trim() +
                " where userid = " + user.uid.trim().substring(1,user.uid.trim().length()-1);

    }

    public static JSONArray loadQuestions(String uid, Statement stmt) throws SQLException{

        //without filters
        JSONArray arr = new JSONArray();
        ResultSet rs = stmt.executeQuery("select qid,userid,qstring,qtype,proposed_answer,proposed_keywords,hints,timer,option1,option2,option3,option4,status1,status2,status3,status4 from questions  " +
                " where userid <> " + uid + " limit 3");
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
            obj.put("status1",rs.getString("status1"));
            obj.put("status2",rs.getString("status2"));
            obj.put("status3",rs.getString("status3"));
            obj.put("status4",rs.getString("status4"));
            obj.put("uid",rs.getString("userid"));
            obj.put("qid",rs.getString("qid"));
            obj.put("proposed_keywords",rs.getString("proposed_keywords"));
            arr.add(obj);
        }

        JSONObject mainObj = new JSONObject();
        mainObj.put("questions", arr);
        return arr;

    }

    public static JSONObject loadProfile(String userid, Statement stmt) throws  SQLException{
        //without filters
        JSONObject obj = new JSONObject();
        System.out.println("select userid,sex, dob, preferred_categories, email, fullname from user_profiles" +
                " where userid = " + userid);
        ResultSet rs = stmt.executeQuery("select userid,sex, dob, preferred_categories, email, fullname from user_profiles" +
                " where userid = " + userid);
        while(rs.next()){
            obj.put("sex",rs.getString("sex"));
            obj.put("dob",rs.getString("dob"));
            obj.put("preferred_categories",rs.getString("preferred_categories"));
            obj.put("email",rs.getString("email"));
            obj.put("fullname",rs.getString("fullname"));
            obj.put("userid",rs.getString("userid"));
        }

        return obj;
    }

    //After first login, insert data and also load the questions

    @Override
    public void onReceive(Object message) throws Throwable {

        System.out.println("inside the actor on receive");
        Connection conn = null;

        if (!(message instanceof GenUser)) {

        } else {
            BoneCP pool = DBConnectionPool.getConnectionPool();
            try {
                GenUser user = (GenUser)message;
                if (pool != null) {
                    conn = pool.getConnection();
                    if(conn != null){
                        System.out.println("Connection successful!");
                        Statement stmt = conn.createStatement();
                        System.out.println("query is " + updateQueryBuilder(user));
                        PreparedStatement ps = conn.prepareStatement(updateQueryBuilder(user),
                                Statement.RETURN_GENERATED_KEYS);
                        int a = ps.executeUpdate(); // do something with the connection.
                        ResultSet key = ps.getGeneratedKeys();
                            System.out.println("userid" + user.uid);
                            JSONArray questions = loadQuestions(user.uid,stmt);
                            System.out.println("loading questions done");
                            //JSONObject profileObj =  loadProfile(userid,conn);
                            //System.out.println("loading profiles  done");
                            JSONObject jobj = new JSONObject();
                            jobj.put("userid",user.uid);
                            jobj.put("questions",questions);
                            System.out.println("jsonstring obtained is " + jobj.toJSONString());
                            getSender().tell(jobj.toJSONString(),self());
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
