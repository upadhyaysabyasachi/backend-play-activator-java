package actors;

import akka.actor.UntypedActor;
import com.jolbox.bonecp.BoneCP;
import models.DBConnectionPool;
//import models.FBUser;
import models.NormalUser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import scala.compat.java8.FutureConverters;

import java.sql.*;

import static akka.pattern.Patterns.ask;

/**
 * Created by sabyasachi.upadhyay on 25/08/16.
 */
public class checkIfUserExistsActor extends UntypedActor {


    public static String insertFBUser(String email){

        return "INSERT INTO user_profiles(email) values("+email+")";

    }


    public String checkQueryFBUserBuilder(String email){
        return "SELECT userid FROM user_profiles WHERE email = " + email+" limit 1";
    }



    public static String  checkQueryNormalUserBuilder(NormalUser user){
        return "SELECT userid FROM user_profiles WHERE email = " + user.email+" and password_string like SHA2("+user.password+",512) "+"limit 1";
    }

    public String insertRegisteredUser(String emailAndPassword){
        return "";
    }




    public static JSONArray loadQuestions(String uid, Connection conn) throws SQLException{
        Statement stmt = conn.createStatement();
        //without filters
        JSONArray arr = new JSONArray();
        ResultSet rs = stmt.executeQuery("select qstring,qtype,proposed_answer,proposed_keywords,hints,timer,option1,option2,option3,option4,status1,status2,status3,status4 from questions  " +
                " where userid <> " + uid + " limit 10");
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

            arr.add(obj);
        }

        JSONObject mainObj = new JSONObject();
        mainObj.put("questions", arr);
        return arr;

    }

    /*qid BIGINT NOT NULL AUTO_INCREMENT,
                            userid BIGINT,
                            qtype	varchar(11),
                            qstring varchar(255),
                            proposed_answer	varchar(255),
                            proposed_keywords varchar(255),
                            option1	varchar(20),
                            option2 varchar(20),
                            option3 varchar(20),
                            option4 varchar(20),
                            post_time	datetime*/



    public  static JSONObject checkForFirstTimeNormalUser(NormalUser user) throws SQLException {
        BoneCP pool = DBConnectionPool.getConnectionPool();
        Connection conn = pool.getConnection();

        if (conn != null) {
            System.out.println("Connection successful!");
            Statement stmt = conn.createStatement();
            System.out.println("query is " + checkQueryNormalUserBuilder(user));

            ResultSet rs = stmt.executeQuery(checkQueryNormalUserBuilder(user));
            int numRows = rs.getFetchSize();
            if (numRows > 0) {
                System.out.println("user exists");
                while (rs.next()) {
                    JSONObject jobj = new JSONObject();
                    String uid = rs.getString("userid");
                    JSONArray questionObj = loadQuestions(uid, conn);
                    jobj.put("status", "old");
                    //jobj.put("questions", questionObj);
                    JSONObject profileObj =  UserProfileInsertActor.loadProfile(uid,conn);
                    //JSONObject jobj = new JSONObject();
                    jobj.put("userid",uid);
                    jobj.put("questions",questionObj);
                    jobj.put("sex",profileObj.get("sex"));
                    jobj.put("dob",profileObj.get("dob"));
                    jobj.put("email",profileObj.get("email"));
                    jobj.put("preferred_categories",profileObj.get("preferred_categories"));
                    jobj.put("fullname",profileObj.get("fullname"));
                    return jobj;
                }
            }

            //tell the front end that the normal user is not registered and ask him to register
            else {
                JSONObject jsonobj = new JSONObject();
                jsonobj.put("status", "new");
                return jsonobj;

            }
        }
        //for no db connection
        JSONObject jsonobj = new JSONObject();
        jsonobj.put("status", "nodbconnection");
        return jsonobj;

    }



    private int getRowCount(ResultSet resultSet) {
        if (resultSet == null) {
            return 0;
        }
        try {
            resultSet.last();
            return resultSet.getRow();
        } catch (SQLException exp) {
            exp.printStackTrace();
        } finally {
            try {
                resultSet.beforeFirst();
            } catch (SQLException exp) {
                exp.printStackTrace();
            }
        }
        return 0;
    }


    public  JSONObject checkForFirstTimeFBUser(String email) throws SQLException{
        BoneCP pool = DBConnectionPool.getConnectionPool();
        Connection conn = pool.getConnection();

        if(conn != null){
            System.out.println("Connection successful inside checkForFirstTimeFBUser!");
            Statement stmt = conn.createStatement();
            System.out.println("query is " + checkQueryFBUserBuilder(email));

            ResultSet rs = stmt.executeQuery(checkQueryFBUserBuilder(email));
            int numRows = rs.getFetchSize();
            System.out.println("number of rows" + numRows);
            if(getRowCount(rs)>0){
                System.out.println("user exists");
                while(rs.next()){
                    JSONObject jobj = new JSONObject();
                    String uid = rs.getString("userid");

                    JSONArray questionObj = loadQuestions(uid, conn);
                    jobj.put("userid",uid);
                    jobj.put("status", "old");
                    jobj.put("questions",questionObj);
                    JSONObject profileObj =  UserProfileInsertActor.loadProfile(uid,conn);
                    //JSONObject jobj = new JSONObject();
                    //jobj.put("userid",uid);
                    //jobj.put("questions",questions);
                    jobj.put("sex",profileObj.get("sex"));
                    jobj.put("dob",profileObj.get("dob"));
                    jobj.put("email",profileObj.get("email"));
                    jobj.put("fullname",profileObj.get("fullname"));
                    jobj.put("preferred_categories",profileObj.get("preferred_categories"));
                    return jobj;
                }
            }
            //Insert FB User, generate userid and give it to front end
            else{

                JSONObject jsonobj = new JSONObject();
                //generate the userid and give it too;

                System.out.println("query is " + insertFBUser(email));
                PreparedStatement ps = conn.prepareStatement(insertFBUser(email),
                        Statement.RETURN_GENERATED_KEYS);
                int a = ps.executeUpdate(); // do something with the connection.
                ResultSet key = ps.getGeneratedKeys();
                if (key.next()) {
                    String userid = key.getInt(1)+"";
                    JSONArray arr = loadQuestions(userid,conn);
                    JSONObject jobj = new JSONObject();
                    jobj.put("userid",userid);
                    jobj.put("status","new");
                    getSender().tell(jobj.toJSONString(),self());
                }

                //stmt.executeQuery(insertFBUser(email));
                return jsonobj;

            }
        }


        JSONObject jsonobj = new JSONObject();
        //jsonobj.put("fb_id", fb_id);
        jsonobj.put("status", "dbconnectionfail");
        return jsonobj;




    }


    @Override
    public void onReceive(Object message) throws Throwable {

            System.out.println("inside the actor on receive");
            Connection conn = null;
            if (message instanceof NormalUser) {
                //Normal User
                BoneCP pool = DBConnectionPool.getConnectionPool();
                try {

                    if (pool != null) {
                        conn = pool.getConnection();
                        if (conn != null) {
                            NormalUser user = (NormalUser) message;
                            try {
                                getSender().tell(checkForFirstTimeNormalUser(user), self());
                            } catch (SQLException se) {
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
            //FB USer
            else if (message instanceof String){
                System.out.println("hellofb user");
                BoneCP pool = DBConnectionPool.getConnectionPool();
                try {

                    if (pool != null) {
                        conn = pool.getConnection();
                        if(conn != null){
                            String email = (String)message;
                            try{
                                getSender().tell(checkForFirstTimeFBUser(email),self());
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
            }//else endss
        }

}
