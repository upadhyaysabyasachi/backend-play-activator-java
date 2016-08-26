package models;

/**
 * Created by sabyasachi.upadhyay on 25/08/16.
 */
public class Questions {

    public String qid;
    public String userid;
    public String qtype;
    public String qstring;
    public String proposed_answer;
    public String keywords;
    public String hints;
    public String option1;
    public String option2;
    public String option3;
    public String option4;
    public String timer;

    public Questions(String param1, String param2, String param3, String param4, String param5, String param6, String param7, String param8
    ,String param9, String param10,String param11){

        this.userid = param1;
        this.qtype = param2;
        this.qstring = param3;
        this.proposed_answer = param4;
        this.keywords = param5;
        this.option1 = param6;
        this.option2 = param7;
        this.option3 = param8;
        this.option4 = param9;
        this.hints = param10;
        this.timer = param11;


    }
}
