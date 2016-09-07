package actors;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Notification;
import com.google.android.gcm.server.Sender;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by sabyasachi.upadhyay on 02/09/16.
 */
public class SampleGCMSender {

    public static void main(String[] args){
        final Sender sender = new Sender("AIzaSyAwlhqfNKiK1HCjS3bzNh7XbrseeOzCtcY");
        com.google.android.gcm.server.MulticastResult result = null;




        final Message pushMessage = new Message.Builder().timeToLive(30)
                .delayWhileIdle(true)
                .addData("date", new Date().getTime() + "")
                .addData("message", "You are matched with ").
                        build();

        final List<String> regids = new ArrayList<String>();
        //regids.add("c3klWR0WtP0:APA91bEQPLOhv1DP_1drNCTocj2ArY3omWaq2PPEKxk7Mx9_cURqHh3My2FD5gAQoJ6vGDKavxjyKSqxuuKlBDbFXR-VZC5W0MVxADbwNHnN-ikVqo4MgAciit5RYBYPjGj73qovUkGV");
        regids.add("dTNGFKnklN4:APA91bGlsCMPLOG3hOzOIR31HyXB8nI85rKosyuD4a3tlPcHD341GR90T9aDyIgvfO7dodi55Bo6seBefZDT6SW31T0P7pVUUqYGPJ_-9bYlnFACxOnRh2NO_cjmJi0pjGFdhE5PV2V9");
        regids.add("chgi0VgFvsE:APA91bGY3mpaJDagnz9VlGchZkMTaIBqZcEYNKqMRe5Yo7rxES4xRO4KH3bLNSH8NR-BiIz_UmJQyYBWoTgD7Q6N568rZu3cx9xmZf5eGqKadLFFMhH0QkgBg2t1nXNHts0EFIe4x-zF");
        //regids.add("cvcefGEh0AU:APA91bFA3ZjvAt7HJ3ncpDJ3P0dczk4Pco4xYt4RnbdvErlu1YvS_YMcMuslrn0IO05vnRDuo6gjXs74q9YwtKXu4-TKLIeyJbmFWn_bXWiV4VBtImRy9CYh2WRmDS90O9qyNvTKrAj2");
        //regids.add("fWCXK2Z-QDQ:APA91bHySTdtT7crtYIezBkja75kXpy1k8Fp1o5iRx0eaQbXXrf4VxavfMj73l8j4glINXp1MxHbXiZ6hjmOi7WYp2LnkSN3XMKtMOyAp3oQo82iAiR-8NF9CKxzWQGOpsi0efLYVD7d");
        //regids.add(regId_questioner);
        //Logger.info("entered2 : " + regids.size());
        try {
            result = sender.send(pushMessage, regids, 2);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}
