package models;

/**
 * Created by sabyasachi.upadhyay on 22/08/16.
 */
public class FBUser {
    public String fbemail = null;
    public String altemail = null;
    public String dob;
    public String sex;
    public String firstName;
    public String lastName;

    public FBUser(String param1, String param, String param2, String param3, String param4, String param5){
        this.fbemail = param1;
        this.dob = param2;
        this.sex = param3;
        this.firstName = param4;
        this.lastName = param5;
        this.altemail = param;
    }


}
