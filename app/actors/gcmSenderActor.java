package actors;

import akka.actor.UntypedActor;
import com.jolbox.bonecp.BoneCP;
import models.*;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.util.Version;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.tartarus.snowball.ext.PorterStemmer;

import java.io.IOException;
import java.io.StringReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Date;

/**
 * Created by sabyasachi.upadhyay on 02/09/16.
 */
public class gcmSenderActor extends UntypedActor {

    public static String getRegistrationTokenBuilder(String uid){
        return "select device_token from user_sessions where userid = " + uid+" and login_status like 'yes' ";
    }

    public String insertIntoAnswers(Answers obj){
        return "INSERT INTO answers(qid," +
                "                        uid_answerer," +
                "                        uid_questioner," +
                "                        attempted_answer," +
                "                        attempted_keywords," +
                "                        match_status," +
                "                        answer_time) "+
                " values("+obj.qid+","+obj.uid_a+","+obj.uid_q+",'"+obj.attempted_answer+"','"+obj.attempted_keywords+
                "','"+obj.match_status+"','"+obj.answer_time+"')";
    }

    public static int distance(String a, String b){
        a = a.toLowerCase();
        b = b.toLowerCase();
        int[] costs = new int[b.length() + 1];
        for (int j = 0; j < costs.length; j++)
            costs[j] = j;
        for (int i = 1; i <= a.length(); i++)
        {
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= b.length(); j++)
            {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]),
                        a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[b.length()];
    }

    public static JSONArray getRegistrationToken(String uid) {
        BoneCP pool = DBConnectionPool.getConnectionPool();
        Connection conn = null;
        JSONArray arr = new JSONArray();
        try {
            if (pool != null) {
                conn = pool.getConnection();
                if (conn != null) {
                    System.out.println("Connection successful inside getRegistrationToken !");
                    Statement stmt = conn.createStatement();
                    System.out.println("query for getting registration tokens are " + getRegistrationTokenBuilder(uid));
                    ResultSet rs = stmt.executeQuery(getRegistrationTokenBuilder(uid));// do something with the connection.
                    while (rs.next()) {
                        String device_token = rs.getString("device_token");
                        JSONObject jobj = new JSONObject();
                        jobj.put("device_token",device_token);
                        arr.add(device_token);
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                    return arr;
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return arr;
    }

    public void notifyUsersOfMatch(ArrayList<String> device_tokens, String qid, String payload){
        System.out.println("MAtch has happened..sending notification");
        final Sender sender = new Sender("AIzaSyAwlhqfNKiK1HCjS3bzNh7XbrseeOzCtcY");
        com.google.android.gcm.server.MulticastResult result = null;

        final Message pushMessage = new Message
                .Builder()
                .timeToLive(30)
                .delayWhileIdle(true)
                .addData("date", new Date().getTime() + "")
                .addData("message", payload)
                .build();

        System.out.println("message is " + pushMessage.getData().toString());
        System.out.println("tokens to which messages have to be sent are " + String.valueOf(device_tokens));
        //Logger.info("entered2 : " + regids.size());
        try {
            result = sender.send(pushMessage, device_tokens, 1);
            System.out.println("notification sent");
        } catch (final IOException e) {
            e.printStackTrace();
        }


    }

    public String insertIntoChatDB(String uid_sender, String uid_receiver, String senderType, String time_stamp){
        if(senderType.equalsIgnoreCase("questioner")){
            return "INSERT INTO conversations(uid_sender,uid_receiver,message,time_stamp) values("+uid_sender+","
                    +uid_receiver+",'You answered my question, You are indeed a real match'"+",'"+time_stamp +"')";
        }else{
            return "INSERT INTO conversations(uid_sender,uid_receiver,message,time_stamp) values("+uid_sender+","
                    +uid_receiver+",'Yes, I did! '"+",'"+time_stamp +"')";
        }

    }

    public String createChatEntryInDB(String uid_sender, String uid_receiver, String time_stamp){
        //create an entry to store the first chat string for sender - You answered my match, You are indeed a "real match"
        //create an entry to store the second chat string for receiver - Yes, I did!

            BoneCP pool = null;
            Connection conn = null;
            try{
                pool = DBConnectionPool.getConnectionPool();
                if(pool!=null){
                    conn = pool.getConnection();
                    if(conn != null){
                        Statement stmt = conn.createStatement();

                        System.out.println("query for inserting into chat db for questioner is " + insertIntoChatDB(uid_sender, uid_receiver, "questioner",time_stamp));
                        PreparedStatement ps1 = conn.prepareStatement(insertIntoChatDB(uid_sender, uid_receiver, "questioner", time_stamp),
                                Statement.RETURN_GENERATED_KEYS);
                        int a1 = ps1.executeUpdate();

                        System.out.println("query for inserting into chat db for receiver is " + insertIntoChatDB(uid_sender, uid_receiver, "answerer", time_stamp));
                        PreparedStatement ps2 = conn.prepareStatement(insertIntoChatDB(uid_sender, uid_receiver, "answerer", time_stamp),
                                Statement.RETURN_GENERATED_KEYS);

                        int a2 = ps2.executeUpdate();

                        return "success";
                    }
                }
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
        return "failure";

        }

    public String getProfileString(String uid){
        return "select DOB," +
                "fullname," +
                "sex," +
                "preferred_categories,email,userid from user_profiles where userid = " + uid;
    }

    public String getQandAString(String uid_answerer, String uid_questioner){

        return "select ans.uid_answerer,ans.uid_questioner,qs.qstring,  qs.proposed_keywords,ans.attempted_answer, ans.answer_time from questions qs join answers ans " +
                "on ans.qid = qs.qid where ans.uid_answerer = "+uid_answerer +" and ans.uid_questioner = "+uid_questioner+
                " and match_status like 'yes' order by ans.answer_time DESC limit 1";

    }

    public String giveUserProfileOfTheMatchedPerson(String uid_answerer, String uid_questioner) throws ParseException {
        {
            BoneCP pool = null;
            Connection conn = null;
            try{
                pool = DBConnectionPool.getConnectionPool();
                if(pool!=null){
                    conn = pool.getConnection();
                    if(conn != null){
                        Statement stmt = conn.createStatement();

                        System.out.println("query for getting profile answerer from DB is  " + getProfileString(uid_answerer));
                        ResultSet rs = stmt.executeQuery(getProfileString(uid_answerer));
                        JSONObject profileObj = new JSONObject();
                        while(rs.next()){
                            JSONObject jobj = new JSONObject();
                            String dob = rs.getString("DOB");
                            String fullname = rs.getString("fullname");
                            String sex = rs.getString("sex");
                            String preferred_categories = rs.getString("preferred_categories");
                            String email = rs.getString("email");

                            jobj.put("dob",dob);
                            jobj.put("fullname",fullname);
                            jobj.put("sex",sex);
                            jobj.put("email",email);
                            jobj.put("preferred_categories",preferred_categories);
                            jobj.put("userid",uid_answerer);
                            profileObj.put("answerer",jobj);

                        }

                        System.out.println("query for getting profile questioner from DB is  " + getProfileString(uid_questioner));
                        ResultSet rs1 = stmt.executeQuery(getProfileString(uid_questioner));

                        while(rs1.next()){
                            JSONObject jobj = new JSONObject();
                            String dob = rs1.getString("DOB");
                            String fullname = rs1.getString("fullname");
                            String sex = rs1.getString("sex");
                            String preferred_categories = rs1.getString("preferred_categories");
                            String email = rs1.getString("email");
                            jobj.put("dob",dob);
                            jobj.put("fullname",fullname);
                            jobj.put("sex",sex);
                            jobj.put("preferred_categories",preferred_categories);
                            jobj.put("email",email);
                            jobj.put("userid",uid_questioner);
                            profileObj.put("questioner",jobj);
                        }

                        System.out.println("query for getting questions answered from DB is  " + getQandAString(uid_answerer,
                                uid_questioner));
                        ResultSet rs2 = stmt.executeQuery(getQandAString(uid_answerer,
                                uid_questioner));

                        JSONArray arr = new JSONArray();
                        JSONObject qobj = new JSONObject();
                        JSONParser parser = new JSONParser();
                        while(rs2.next()){
                            qobj.put("qstring",rs2.getString("qstring"));
                            JSONArray attempted_answer = (JSONArray)parser.parse(rs2.getString("attempted_answer"));
                            qobj.put("attempted_answer",attempted_answer);
                            JSONArray proposed_keywords = (JSONArray)parser.parse(rs2.getString("proposed_keywords"));
                            qobj.put("proposed_keywords",proposed_keywords);
                            qobj.put("quesid",rs2.getString("uid_questioner"));
                            qobj.put("ansid",rs2.getString("uid_answerer"));
                            qobj.put("time_stamp",rs2.getString("answer_time"));
                        }

                        profileObj.put("QandA",qobj);
                        profileObj.put("flag","match");
                        return profileObj.toJSONString();


                    }
                }
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
            return "failure";
        }
    }

    public String storeIntoAnswersDB(Answers obj){
        BoneCP pool = null;
        Connection conn = null;
        try{
            pool = DBConnectionPool.getConnectionPool();
            if(pool!=null){
                conn = pool.getConnection();
                if(conn != null){
                    Statement stmt = conn.createStatement();
                    System.out.println("query for inserting into asnwers db is " + insertIntoAnswers(obj));
                    PreparedStatement ps = conn.prepareStatement(insertIntoAnswers(obj),
                            Statement.RETURN_GENERATED_KEYS);
                    int a = ps.executeUpdate();
                    return "success";
                }
            }
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
        return "failure";
    }

    @Override
    public void onReceive(Object message) throws Throwable {
        if(message instanceof JSONObject){
            JSONParser parser = new JSONParser();
            JSONObject obj = (JSONObject)message;
            String qid = obj.get("qid").toString();
            String qtype = obj.get("qtype").toString();
            String uid_questioner = obj.get("uid_questioner").toString();
            String uid_answerer = obj.get("uid_answerer").toString();
            String answer_time = obj.get("answer_time").toString();

            JSONArray answers_questioner = (JSONArray)parser.parse(obj.get("answer_questioner").toString());
            JSONArray answers_answerer = (JSONArray)parser.parse(obj.get("answer_answerer").toString());

            ArrayList<String> answers_a = new ArrayList<String>();
            ArrayList<String> answers_q = new ArrayList<String>();

            for(int i=0; i < answers_questioner.size(); i++){
                JSONObject answerObject = (JSONObject)answers_questioner.get(i);
                String answer = answerObject.get("answer").toString().toLowerCase();
                answers_q.add(answer);
            }

            for(int i=0; i < answers_answerer.size(); i++){
                JSONObject answerObject = (JSONObject)answers_answerer.get(i);
                String answer = answerObject.get("answer").toString().toLowerCase();
                answers_a.add(answer);
            }

            if(qtype.equalsIgnoreCase("objective")){
                if(answers_a.size() == answers_q.size()){
                    if(Arrays.equals(answers_a.toArray(),answers_q.toArray())){
                        //match
                        JSONArray regId_questioner = getRegistrationToken(uid_questioner);
                        JSONArray regId_answerer = getRegistrationToken(uid_answerer);

                        ArrayList<String> devices = new ArrayList<String>();

                        System.out.println(String.valueOf(regId_questioner));
                        System.out.println(String.valueOf(regId_answerer));

                        for(int i=0; i< regId_questioner.size(); i++){
                            if(regId_questioner.get(i)!=null){
                                devices.add(regId_questioner.get(i).toString());
                            }

                        }

                        for(int i=0; i < regId_answerer.size(); i++){
                            if(regId_answerer.get(i)!=null){
                            devices.add(regId_answerer.get(i).toString());
                        }
                        }

                        String status = storeIntoAnswersDB(new Answers(qid,uid_questioner,uid_answerer, answers_answerer.toJSONString(), answers_answerer.toJSONString(),answers_questioner.toJSONString(),
                                "yes", answer_time));

                        createChatEntryInDB(uid_answerer,uid_questioner, answer_time);

                        String profileObjPayload = giveUserProfileOfTheMatchedPerson(uid_answerer,uid_questioner);

                        notifyUsersOfMatch(devices, qid, profileObjPayload);

                        if(status.equalsIgnoreCase("success")){
                            JSONObject jobj = new JSONObject();
                            jobj.put("status","success");
                            getSender().tell(jobj.toJSONString(),self());
                        }
                        else{
                            JSONObject jobj = new JSONObject();
                            jobj.put("status","failure");
                            getSender().tell(jobj.toJSONString(),self());
                        }
                        //store in db
                    }
                    else{
                        //not a match
                        String status = storeIntoAnswersDB(new Answers(qid,uid_questioner,uid_answerer, answers_answerer.toJSONString(), answers_answerer.toJSONString(),answers_questioner.toJSONString(),
                                "no", answer_time));

                        if(status.equalsIgnoreCase("success")){
                            JSONObject jobj = new JSONObject();
                            jobj.put("status","success");
                            getSender().tell(jobj.toJSONString(),self());
                        }else{
                            JSONObject jobj = new JSONObject();
                            jobj.put("status","failure");
                            getSender().tell(jobj.toJSONString(),self());
                        }
                    }
                }else{
                    //not a match
                    String status = storeIntoAnswersDB(new Answers(qid,uid_questioner,uid_answerer, answers_answerer.toJSONString(), answers_answerer.toJSONString(),answers_questioner.toJSONString(),
                            "no", answer_time));

                    if(status.equalsIgnoreCase("success")){
                        JSONObject jobj = new JSONObject();
                        jobj.put("status","success");
                        getSender().tell(jobj.toJSONString(),self());
                    }else{
                        JSONObject jobj = new JSONObject();
                        jobj.put("status","failure");
                        getSender().tell(jobj.toJSONString(),self());
                    }

                }

            }else{
                //subjective

               //remove the stop words
                for(String word: answers_q){
                    if(StopWords.isStopword(word)){
                        answers_q.remove(word);
                    }
                }

                for(String word: answers_a){
                    if(StopWords.isStopword(word)){
                        answers_a.remove(word);
                    }
                }

                PorterStemmer stemmer = new PorterStemmer();
                int correct_ans_size = answers_q.size();
                int est_correct_size = 0;
                for(String each_attempted_keyword : answers_a){
                    for(String each_correct_keyword : answers_q){

                        stemmer.setCurrent(each_attempted_keyword);
                        stemmer.stem();
                        String attempted_keyword_stemmed = stemmer.getCurrent();

                        stemmer.setCurrent(each_attempted_keyword);
                        stemmer.stem();
                        String correct_keyword_stemmed = stemmer.getCurrent();

                        if(attempted_keyword_stemmed.equalsIgnoreCase(correct_keyword_stemmed)  ||
                                (distance(attempted_keyword_stemmed,correct_keyword_stemmed) <= 1)
                                ){
                            est_correct_size++;
                            break;
                        }

                    }
                }

                if(est_correct_size == correct_ans_size){
                    //matched
                    //send the notification and store in db
                    JSONArray regId_questioner = getRegistrationToken(uid_questioner);
                    JSONArray regId_answerer = getRegistrationToken(uid_answerer);

                    ArrayList<String> devices = new ArrayList<String>();

                    for(int i=0; i< regId_questioner.size(); i++){
                        devices.add(regId_questioner.get(i).toString());
                    }

                    for(int i=0; i < regId_answerer.size(); i++){
                        devices.add(regId_answerer.get(i).toString());
                    }

                    //notifyUsersOfMatch(devices, qid);

                    String status = storeIntoAnswersDB(new Answers(qid,uid_questioner,uid_answerer, answers_questioner.toJSONString(), answers_answerer.toJSONString(),answers_questioner.toJSONString(),
                            "yes", answer_time));

                    createChatEntryInDB(uid_answerer,uid_questioner, answer_time);

                    String profileObjPayload = giveUserProfileOfTheMatchedPerson(uid_answerer,uid_questioner);

                    notifyUsersOfMatch(devices, qid, profileObjPayload);

                    if(status.equalsIgnoreCase("success")){
                        JSONObject jobj = new JSONObject();
                        jobj.put("status","success");
                        getSender().tell(jobj.toJSONString(),self());
                    }
                    else{
                        JSONObject jobj = new JSONObject();
                        jobj.put("status","failure");
                        getSender().tell(jobj.toJSONString(),self());
                    }

                }

            }

        }
    }
}
