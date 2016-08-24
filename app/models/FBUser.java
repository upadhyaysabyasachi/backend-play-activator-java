package models;

/**
 * Created by sabyasachi.upadhyay on 22/08/16.
 */
public class FBUser {
    public String fbemail = null;
    public String fb_id;
    public String dob;
    public String sex;
    public String fullName;
    public String preferredCategories;

    public FBUser(String param1, String param, String param2, String param3, String param4, String param5, String param6){
        this.fbemail = param1;
        this.fb_id = param2;
        this.dob = param3;
        this.sex = param4;
        this.fullName = param5;
        this.preferredCategories = param6;
    }







}
