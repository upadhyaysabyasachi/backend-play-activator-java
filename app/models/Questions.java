package models;

/**
 * Created by sabyasachi.upadhyay on 25/08/16.
 */
public class Questions {

    public String post_time;
    public String categories;
    public String qid;
    public String userid;
    public String qtype;
    public String qstring;
    public String proposed_answer;
    public String proposed_keywords;
    public String hints;
    public String option1;
    public String option2;
    public String option3;
    public String option4;
    public String status1;
    public String status2;
    public String status3;
    public String status4;
    public String timer;

    public Questions(String param1, String param2, String param3, String param4, String param5, String param6, String param7, String param8
    ,String param9, String param10,String param11,String param12,String param13,String param14, String param15,String param16, String param17){

        this.userid = param1;
        this.qtype = param2;
        this.qstring = param3;
        this.proposed_answer = param4;
        this.proposed_keywords = param5;
        this.option1 = param6;
        this.option2 = param7;
        this.option3 = param8;
        this.option4 = param9;
        this.status1 = param10;
        this.status2 = param11;
        this.status3 = param12;
        this.status4 = param13;
        this.hints = param14;
        this.timer = param15;
        this.post_time = param16;
        this.categories = param17;



    }
}
