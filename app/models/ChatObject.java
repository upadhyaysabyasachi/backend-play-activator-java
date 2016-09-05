package models;

/**
 * Created by sabyasachi.upadhyay on 04/09/16.
 */
public class ChatObject {
    public String uid_sender;
    public String uid_receiver;
    public String message;
    public String timestamp;

    public ChatObject(String uid_sender, String uid_receiver, String message, String timestamp) {
        this.uid_sender = uid_sender;
        this.uid_receiver = uid_receiver;
        this.message = message;
        this.timestamp = timestamp;
    }

    public ChatObject(String uid_sender, String uid_receiver) {
        this.uid_sender = uid_sender;
        this.uid_receiver = uid_receiver;
    }
}

