package actors;

import akka.actor.UntypedActor;
import com.jolbox.bonecp.BoneCP;
import models.ChatObject;
import models.DBConnectionPool;
import models.NormalUser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.sql.*;

import static akka.pattern.Patterns.ask;

/**
 * Created by sabyasachi.upadhyay on 25/08/16.
 */
public class checkIfUserExistsActor extends UntypedActor {

    public static JSONObject loadProfile(String userid, Connection conn) throws  SQLException{
        //without filters
        JSONObject obj = new JSONObject();
        Statement stmt = conn.createStatement();
        System.out.println("select userid,sex, dob, preferred_categories, email, fullname, image from user_profiles" +
                " where userid = " + userid);
        ResultSet rs = stmt.executeQuery("select userid,sex, dob, preferred_categories, email, fullname, image  from user_profiles" +
                " where userid = " + userid);
        while(rs.next()){
            obj.put("sex",rs.getString("sex"));
            obj.put("dob",rs.getString("dob"));
            obj.put("preferred_categories",rs.getString("preferred_categories"));
            obj.put("email",rs.getString("email"));
            obj.put("fullname",rs.getString("fullname"));
            obj.put("userid",rs.getString("userid"));
            obj.put("image_string",rs.getString("image"));

        }

        return obj;
    }

    public static JSONArray loadQuestions(String uid, Connection conn) throws SQLException{

        //without filters
        JSONArray arr = new JSONArray();
        Statement stmt = conn.createStatement();
        String query = "select ques.qid,ques.userid,ques.qstring,ques.qtype,ques.proposed_answer,ques.proposed_keywords,ques.hints,timer," +
                "option1,option2,option3,option4,status1,status2,status3,status4 from questions ques left outer join answers ans  " +
                "on ques.qid = ans.qid where ques.userid <> " + uid + " and ans.uid_answerer IS NULL " +
                "UNION " +
                "select ques.qid,ques.userid,ques.qstring,ques.qtype,ques.proposed_answer,ques.proposed_keywords,ques.hints,timer," +
                "option1,option2,option3,option4,status1,status2,status3,status4 from questions ques left outer join answers ans  " +
                "on ques.qid = ans.qid where ques.userid <> 1 and ans.uid_answerer IS NOT NULL and ans.uid_answerer <> " + uid + " limit 3";

        System.out.println("query for getting questions is " + query);

        ResultSet rs = stmt.executeQuery(query);

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

    public static String insertFBUser(String email){

        return "INSERT INTO user_profiles(email) values("+email+")";

    }

    public String checkQueryFBUserBuilder(String email){
        return "SELECT userid FROM user_profiles WHERE email = " + email+" limit 1";
    }

    public static String  checkQueryNormalUserBuilder(NormalUser user){
        return "SELECT userid FROM user_profiles WHERE email = " + user.email+" and password_string like SHA2("+user.password+",512) "+"limit 1";
    }

    public static String getMatchingUsers(String uid_login){

        return "select distinct uid_answerer uid from answers where uid_questioner ="+ uid_login+ " " +
        "UNION "+
        "select uid_questioner uid from answers where uid_answerer = " + uid_login;

    }

    public static String getMatchingUserQuestionsinfo(String uid_login, String uid_matcher){

        return "select ans.uid_answerer uid_answerer, ans.uid_questioner uid_questioner, qs.qstring qstring, qs.proposed_keywords proposed_keywords,ans.attempted_answer attempted_answer " +
                "from questions qs join answers ans " +
                "on ans.qid = qs.qid  where ans.uid_answerer = "+uid_login +" and ans.uid_questioner = " + uid_matcher+
                " and match_status like 'yes' " +
                " UNION " +
                "select ans.uid_answerer uid_answerer, ans.uid_questioner uid_questioner, qs.qstring qstring, qs.proposed_keywords proposed_keywords,ans.attempted_answer attempted_answer " +
                "from questions qs join answers ans " +
                "on ans.qid = qs.qid  where ans.uid_answerer = "+uid_matcher +" and ans.uid_questioner = " + uid_login+
                " and match_status like 'yes'";

    }

    public static JSONObject QandA(Connection conn, String query) throws SQLException, ParseException {

        System.out.println("-------------------- INSIDE QndA JSON construction object-----------------");
        JSONObject finalObj = new JSONObject();
        JSONArray arrQandA =  new JSONArray();
        JSONParser parser =  new JSONParser();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);

        while(rs.next()){
            JSONObject jobj = new JSONObject();
            jobj.put("qstring",rs.getString("qstring"));
            jobj.put("ansid",rs.getString("uid_answerer"));
            jobj.put("quesid",rs.getString("uid_questioner"));
            JSONArray attempted_answer = (JSONArray)parser.parse(rs.getString("attempted_answer"));
            jobj.put("attempted_answer",attempted_answer);
            JSONArray proposed_keywords = (JSONArray)parser.parse(rs.getString("proposed_keywords"));
            jobj.put("proposed_keywords",proposed_keywords);
            arrQandA.add(jobj);
        }

        finalObj.put("QandA",arrQandA);
        return finalObj;
    }

    public static JSONObject matchedUserProfiles(String userid, Connection conn) throws SQLException, ParseException {
        //without filters

        JSONObject matchedUsersObject = new JSONObject();
        JSONArray matchedUserProfileArray = new JSONArray();
        Statement stmt = conn.createStatement();

        System.out.println("Query inside matchedUserProfiles is " + getMatchingUsers(userid));

        ResultSet rs_matches_answering_uids = stmt.executeQuery(getMatchingUsers(userid));

        while(rs_matches_answering_uids.next()){
            //JSONObject each_user_obj = new JSONObject();
            String each_matching_userid = rs_matches_answering_uids.getString("uid");
            //get the profile for the matcher
            System.out.println("---------------- LOADING MATCHER PROFILES --------------");
            JSONObject matcher_profile = loadProfile(each_matching_userid, conn);
            System.out.println("---------------- LOADING QUESTIONS AND ANSWER INTERACTIONS WITH THE MATCHER  --------------");
            JSONObject QandAForThisMatcher = QandA(conn, getMatchingUserQuestionsinfo(userid,each_matching_userid));
            System.out.println("---------------- LOADING ALL THE CHATS  WITH THE MATCHER  --------------");
            JSONObject chatsWithThisUser = loadChatActor.loadChats(new ChatObject(each_matching_userid,userid),conn);
            matcher_profile.put("QandA",(JSONArray)QandAForThisMatcher.get("QandA"));
            System.out.println("QandA is " + matcher_profile.toJSONString());
            matcher_profile.put("chats",(JSONArray)chatsWithThisUser.get("chats"));
            System.out.println("chats with Q and A are " + matcher_profile.toJSONString());
            matchedUserProfileArray.add(matcher_profile);
            System.out.println("one entry added");
        }
        System.out.println("outside the loop ");
        matchedUsersObject.put("matched_users",matchedUserProfileArray);
        //rs_matches_answering_uids.close();

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
                        System.out.println("--------GETTING THE QUESTIONS INTENDED FOR THE USER-----------------");
                        JSONArray questionObj = loadQuestions(uid, conn);
                        System.out.println("--------GETTING THE PROFILE FOR THE USER-----------------");
                        JSONObject profileObj = loadProfile(uid, conn);
                        System.out.println("--------GETTING THE PROFILES FOR THE MATCHED USERS ----------------");
                        JSONObject matchedUsersProfile =  matchedUserProfiles(uid,conn);

                        profileObj.put("matched_users",matchedUsersProfile.get("matched_users"));
                        profileObj.put("questions",questionObj);
                        profileObj.put("chats",matchedUsersProfile.get("chats"));
                        profileObj.put("status","old");

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

    public  JSONObject checkForFirstTimeFBUser(String email) throws ParseException {
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

                        JSONArray questionObj = loadQuestions(uid, conn);

                        JSONObject profileObj = loadProfile(uid, conn);
                        JSONObject matchedUsersProfile =  matchedUserProfiles(uid,conn);

                        profileObj.put("matched_users",matchedUsersProfile.get("matched_users"));
                        profileObj.put("questions",questionObj);
                        profileObj.put("chats",matchedUsersProfile.get("chats"));
                        profileObj.put("status","old");

                        return profileObj;


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
