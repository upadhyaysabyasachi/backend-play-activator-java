package models;

/**
 * Created by sabyasachi.upadhyay on 02/09/16.
 */
public class Answers {
    public String qid;
    public String uid_q;
    public String uid_a;
    public String attempted_answer;
    public String attempted_keywords;
    public String match_status;
    public String answer_time;

    public Answers(String qid, String uid_q, String uid_a, String attempted_answer, String attempted_keywords, String match_status, String answer_time) {
        this.qid = qid;
        this.uid_q = uid_q;
        this.uid_a = uid_a;
        this.attempted_answer = attempted_answer;
        this.attempted_keywords = attempted_keywords;
        this.match_status = match_status;
        this.answer_time = answer_time;
    }
}
