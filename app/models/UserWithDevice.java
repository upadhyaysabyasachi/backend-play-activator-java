package models;



/**
 * Created by sabyasachi.upadhyay on 02/09/16.
 */
public class UserWithDevice {

    public String uid;
    public String device_id;

    public UserWithDevice(String uid, String device_id){
        this.uid = uid;
        this.device_id = device_id;
    }
}
