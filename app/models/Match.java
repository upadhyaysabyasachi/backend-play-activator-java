package models;

/**
 * Created by sabyasachi.upadhyay on 02/09/16.
 */
public class Match {
    public String uid_questioner;
    public String uid_answerer;
    public String answer_answerer;
    public String answer_questioner;

    public Match(String uid_questioner ,  String uid_answerer, String answer_answerer, String answer_questioner){
        this.uid_questioner = uid_questioner;
        this.uid_answerer = uid_answerer;
        this.answer_questioner = answer_questioner;
        this.answer_answerer = answer_answerer;
    }
}
