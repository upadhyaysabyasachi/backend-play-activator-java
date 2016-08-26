package models;

/**
 * Created by sabyasachi.upadhyay on 22/08/16.
 */
public class GenUser {
    public String uid=null;
    //public String fb_id;
    public String dob;
    public String sex;
    public String fullName;
    public String preferredCategories;

    public GenUser(String param1, String param2, String param3, String param4, String param5){
        this.uid = param1;
        this.dob = param2;
        this.sex = param3;
        this.fullName = param4;
        this.preferredCategories = param5;
    }







}
