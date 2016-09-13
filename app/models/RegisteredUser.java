package models;

/**
 * Created by sabyasachi.upadhyay on 27/08/16.
 */
public class RegisteredUser {

    public String email;
    public String password;

    public RegisteredUser(String email, String password){
        this.email = email;
        this.password = password;
    }
}
