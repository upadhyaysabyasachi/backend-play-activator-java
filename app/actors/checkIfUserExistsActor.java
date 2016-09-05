package actors;

import akka.actor.UntypedActor;
import com.jolbox.bonecp.BoneCP;
import models.ChatObject;
import models.DBConnectionPool;
//import models.FBUser;
import models.NormalUser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
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

    public static String getMatchingUserIdWhileAnswering(String uid_login){

        return "select distinct ans.uid_questioner from questions qs join answers ans  " +
                "on ans.qid = qs.qid where ans.uid_answerer = "+uid_login +
                " and match_status like 'yes'";

    }

    public static String getMatchingUsers(String uid_login){

        return "select distinct ans.uid_answerer uid  from  questions qs join answers ans " +
                "on ans.qid = qs.qid where (ans.uid_questioner = "+uid_login +" OR ans.uid_answerer =  " + uid_login+")"+
                " and match_status like 'yes'";

    }


    public static String getMatchingUserQuestionsinfo(String uid_login, String uid_matcher){

        return "select ans.uid_answerer, ans.uid_questioner, qs.qstring, qs.proposed_keywords,ans.attempted_answer " +
                "from questions qs join answers ans " +
                "on ans.qid = qs.qid  where ans.uid_answerer = "+uid_login +" and ans.uid_questioner = " + uid_matcher+
                " and match_status like 'yes'" +
                " UNION " +
                "select ans.uid_answerer, ans.uid_questioner, qs.qstring, qs.proposed_keywords,ans.attempted_answer " +
                "from questions qs join answers ans " +
                "on ans.qid = qs.qid  where ans.uid_answerer = "+uid_matcher +" and ans.uid_questioner = " + uid_login+
                " and match_status like 'yes'";

    }

    public static String getMatchingUsersWhileQuestioning(String uid_login, String uid_matcher){

        return "select qs.qid qid, ans.aid aid, ans.uid_answerer answerer, ans.uid_questioner questioner ,qs.qstring qtsring, " +
                "qs.proposed_keywords proposed_keywords ,ans.attempted_answer attempted_answer from questions qs" +
                " join answers ans " +
                "on ans.qid = qs.qid  where ans.uid_questioner = "+ uid_login + " and ans.uid_answerer = " + uid_matcher+
                " and match_status like 'yes'";
    }


    public static JSONObject QandA(Statement stmt, String query) throws SQLException, ParseException {

        JSONObject finalObj = new JSONObject();
        JSONArray arrQandA =  new JSONArray();
        JSONParser parser =  new JSONParser();
        ResultSet rs = stmt.executeQuery(query);

        while(rs.next()){
            JSONObject jobj = new JSONObject();
            jobj.put("qstring",rs.getString("qtsring"));
            jobj.put("ans_id",rs.getString("aid"));
            jobj.put("ques_id",rs.getString("qid"));
            JSONArray attempted_answer = (JSONArray)parser.parse(rs.getString("attempted_answer"));
            jobj.put("attempted_answer",attempted_answer);
            JSONArray proposed_keywords = (JSONArray)parser.parse(rs.getString("proposed_keywords"));
            jobj.put("proposed_keywords",proposed_keywords);
            arrQandA.add(jobj);
        }

        finalObj.put("QandA",arrQandA);
        return finalObj;
    }


    public static JSONObject matchedUserProfiles(String userid, Statement stmt) throws SQLException, ParseException {
        //without filters

        JSONObject matchedUsersObject = new JSONObject();
        JSONArray matchedUserProfileArray = new JSONArray();
        ResultSet rs_matches_answering_uids = stmt.executeQuery(getMatchingUsers(userid));
        while(rs_matches_answering_uids.next()){
            JSONObject each_user_obj = new JSONObject();
            String each_matching_userid = rs_matches_answering_uids.getString("uid");
            //get the profile for the matcher
            JSONObject matcher_profile = UserProfileInsertActor.loadProfile(each_matching_userid, stmt);
            JSONObject QandAForThisMatcher = QandA(stmt, getMatchingUserQuestionsinfo(userid,each_matching_userid));
            JSONObject chatsWithThisUser = loadChatActor.loadChats(new ChatObject(each_matching_userid,userid),stmt);
            matcher_profile.put("QandA",(JSONArray)QandAForThisMatcher.get("QandA"));
            matcher_profile.put("chats",(JSONArray)QandAForThisMatcher.get("chats"));
            matchedUserProfileArray.add(each_user_obj);
        }
        matchedUsersObject.put("matched_users",matchedUserProfileArray);

        return matchedUsersObject;
    }

    public   static JSONObject checkForFirstTimeNormalUser(NormalUser user) throws ParseException {
        BoneCP pool = DBConnectionPool.getConnectionPool();
        Connection conn = null;

        try {
            conn = pool.getConnection();

            if (conn != null) {
                System.out.println("Connection successful!");
                Statement stmt = conn.createStatement();
                System.out.println("query is " + checkQueryNormalUserBuilder(user));

                ResultSet rs = stmt.executeQuery(checkQueryNormalUserBuilder(user));
                int numRows = rs.getFetchSize();
                if (getRowCount(rs) > 0 ) {
                    System.out.println("user exists");
                    while (rs.next()) {
                        JSONObject jobj = new JSONObject();
                        String uid = rs.getString("userid");

                        JSONArray questionObj = UserProfileInsertActor.loadQuestions(uid, stmt);
                        JSONObject profileObj = UserProfileInsertActor.loadProfile(uid, stmt);
                        JSONObject matchedUsersProfile =  matchedUserProfiles(uid,stmt);


                        profileObj.put("matched_users",matchedUsersProfile.get("matched_users"));
                        profileObj.put("questions",questionObj);
                        profileObj.put("chats",matchedUsersProfile.get("chats"));
                        profileObj.put("old",questionObj);

                        return profileObj;
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

        }catch(SQLException sqe){
            sqe.printStackTrace();
        }finally {
            if(conn!=null){
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;

    }



    private static int getRowCount(ResultSet resultSet) {
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


    public  JSONObject checkForFirstTimeFBUser(String email) {
        BoneCP pool = DBConnectionPool.getConnectionPool();
        Connection conn = null;

        try {
            conn = pool.getConnection();

            if (conn != null) {
                System.out.println("Connection successful inside checkForFirstTimeFBUser!");
                Statement stmt = conn.createStatement();
                System.out.println("query is " + checkQueryFBUserBuilder(email));

                ResultSet rs = stmt.executeQuery(checkQueryFBUserBuilder(email));
                int numRows = rs.getFetchSize();
                System.out.println("number of rows" + numRows);
                if (getRowCount(rs) > 0) {
                    System.out.println("user exists");
                    while (rs.next()) {
                        JSONObject jobj = new JSONObject();
                        String uid = rs.getString("userid");

                        JSONArray questionObj = UserProfileInsertActor.loadQuestions(uid, stmt);
                        jobj.put("userid", uid);
                        jobj.put("status", "old");
                        jobj.put("questions", questionObj);
                        JSONObject profileObj = UserProfileInsertActor.loadProfile(uid, stmt);
                        //JSONObject jobj = new JSONObject();
                        //jobj.put("userid",uid);
                        //jobj.put("questions",questions);
                        jobj.put("sex", profileObj.get("sex"));
                        jobj.put("dob", profileObj.get("dob"));
                        jobj.put("email", profileObj.get("email"));
                        jobj.put("fullname", profileObj.get("fullname"));
                        jobj.put("preferred_categories", profileObj.get("preferred_categories"));
                        return jobj;
                    }
                }
                //Insert FB User, generate userid and give it to front end
                else {

                    JSONObject jsonobj = new JSONObject();
                    //generate the userid and give it too;

                    System.out.println("query is " + insertFBUser(email));
                    PreparedStatement ps = conn.prepareStatement(insertFBUser(email),
                            Statement.RETURN_GENERATED_KEYS);
                    int a = ps.executeUpdate(); // do something with the connection.
                    ResultSet key = ps.getGeneratedKeys();
                    if (key.next()) {
                        String userid = key.getInt(1) + "";
                        JSONArray arr = UserProfileInsertActor.loadQuestions(userid, stmt);
                        JSONObject jobj = new JSONObject();
                        jobj.put("userid", userid);
                        jobj.put("status", "new");
                        getSender().tell(jobj.toJSONString(), self());
                    }

                    //stmt.executeQuery(insertFBUser(email));
                    return jsonobj;

                }
            }


            JSONObject jsonobj = new JSONObject();
            //jsonobj.put("fb_id", fb_id);
            jsonobj.put("status", "dbconnectionfail");
            return jsonobj;
        }catch (SQLException sqe){
            sqe.printStackTrace();
        }finally{
            if(conn!=null){
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }


        return null;

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
                            } catch (Exception se) {
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
                            }catch (Exception se){
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
