//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package models;

import com.google.android.gcm.server.InvalidRequestException;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Notification;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Result.Builder;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Sender {
    protected static final String UTF8 = "UTF-8";
    protected static final int BACKOFF_INITIAL_DELAY = 1000;
    protected static final int MAX_BACKOFF_DELAY = 1024000;
    protected final Random random = new Random();
    protected static final Logger logger = Logger.getLogger(Sender.class.getName());
    private final String key;

    public Sender(String key) {
        this.key = (String)nonNull(key);
    }

    public Result send(Message message, String to, int retries) throws IOException {
        int attempt = 0;
        int backoff = 1000;

        Result result;
        boolean tryAgain;
        do {
            ++attempt;
            if(logger.isLoggable(Level.FINE)) {
                logger.fine("Attempt #" + attempt + " to send message " + message + " to regIds " + to);
            }

            result = this.sendNoRetry(message, to);
            tryAgain = result == null && attempt <= retries;
            if(tryAgain) {
                int sleepTime = backoff / 2 + this.random.nextInt(backoff);
                this.sleep((long)sleepTime);
                if(2 * backoff < 1024000) {
                    backoff *= 2;
                }
            }
        } while(tryAgain);

        if(result == null) {
            throw new IOException("Could not send message after " + attempt + " attempts");
        } else {
            return result;
        }
    }

    public Result sendNoRetry(Message message, String to) throws IOException {
        nonNull(to);
        HashMap jsonRequest = new HashMap();
        this.messageToMap(message, jsonRequest);
        jsonRequest.put("to", to);
        String responseBody = this.makeGcmHttpRequest(jsonRequest);
        if(responseBody == null) {
            return null;
        } else {
            JSONParser parser = new JSONParser();

            try {
                JSONObject jsonResponse = (JSONObject)parser.parse(responseBody);
                Builder e = new Builder();
                if(jsonResponse.containsKey("results")) {
                    JSONArray success = (JSONArray)jsonResponse.get("results");
                    if(success.size() != 1) {
                        logger.log(Level.WARNING, "Found null or " + success.size() + " results, expected one");
                        return null;
                    }

                    JSONObject failure = (JSONObject)success.get(0);
                    String failedIds = (String)failure.get("message_id");
                    String jFailedIds = (String)failure.get("registration_id");
                    String i = (String)failure.get("error");
                    e.messageId(failedIds).canonicalRegistrationId(jFailedIds).errorCode(i);
                } else if(to.startsWith("/topics/")) {
                    if(jsonResponse.containsKey("message_id")) {
                        Long var15 = (Long)jsonResponse.get("message_id");
                        e.messageId(var15.toString());
                    } else {
                        if(!jsonResponse.containsKey("error")) {
                            logger.log(Level.WARNING, "Expected message_id or error found: " + responseBody);
                            return null;
                        }

                        String var16 = (String)jsonResponse.get("error");
                        e.errorCode(var16);
                    }
                } else {
                    if(!jsonResponse.containsKey("success") || !jsonResponse.containsKey("failure")) {
                        logger.warning("Unrecognized response: " + responseBody);
                        throw this.newIoException(responseBody, new Exception("Unrecognized response."));
                    }

                    int var17 = this.getNumber(jsonResponse, "success").intValue();
                    int var18 = this.getNumber(jsonResponse, "failure").intValue();
                    ArrayList var19 = null;
                    if(jsonResponse.containsKey("failed_registration_ids")) {
                        JSONArray var20 = (JSONArray)jsonResponse.get("failed_registration_ids");
                        var19 = new ArrayList();

                        for(int var21 = 0; var21 < var20.size(); ++var21) {
                            var19.add((String)var20.get(var21));
                        }
                    }

                    e.success(Integer.valueOf(var17)).failure(Integer.valueOf(var18)).failedRegistrationIds(var19);
                }

                return e.build();
            } catch (ParseException var13) {
                throw this.newIoException(responseBody, var13);
            } catch (Sender.CustomParserException var14) {
                throw this.newIoException(responseBody, var14);
            }
        }
    }

    public MulticastResult send(Message message, List<String> regIds, int retries) throws IOException {
        int attempt = 0;
        int backoff = 1000;
        HashMap results = new HashMap();
        Object unsentRegIds = new ArrayList(regIds);
        ArrayList multicastIds = new ArrayList();

        System.out.println("regIds obtained are " + regIds.toString());
        System.out.println(String.valueOf(regIds));

        boolean tryAgain;
        int var21;
        do {
            MulticastResult multicastResult = null;
            ++attempt;
            if(logger.isLoggable(Level.FINE)) {
                logger.fine("Attempt #" + attempt + " to send message " + message + " to regIds " + unsentRegIds);
            }

            try {
                multicastResult = this.sendNoRetry(message, (List)unsentRegIds);
                //System.out.println("size of the multiCastResult " + multicastResul);
            } catch (IOException var20) {
                logger.log(Level.FINEST, "IOException on attempt " + attempt, var20);
            }

            if(multicastResult == null) {
                tryAgain = attempt <= retries;
            } else {
                long success = multicastResult.getMulticastId();
                logger.fine("multicast_id on attempt # " + attempt + ": " + success);
                multicastIds.add(Long.valueOf(success));
                unsentRegIds = this.updateStatus((List)unsentRegIds, results, multicastResult);
                tryAgain = !((List)unsentRegIds).isEmpty() && attempt <= retries;
            }

            if(tryAgain) {
                var21 = backoff / 2 + this.random.nextInt(backoff);
                this.sleep((long)var21);
                if(2 * backoff < 1024000) {
                    backoff *= 2;
                }
            }
        } while(tryAgain);

        System.out.println("");

        if(multicastIds.isEmpty()) {
            throw new IOException("Could not post JSON requests to GCM after " + attempt + " attempts");
        } else {
            var21 = 0;
            int failure = 0;
            int canonicalIds = 0;
            Iterator multicastId = results.values().iterator();

            while(multicastId.hasNext()) {
                Result result = (Result)multicastId.next();
                if(result.getMessageId() != null) {
                    ++var21;
                    if(result.getCanonicalRegistrationId() != null) {
                        ++canonicalIds;
                    }
                } else {
                    ++failure;
                }
            }

            long var22 = ((Long)multicastIds.remove(0)).longValue();
            com.google.android.gcm.server.MulticastResult.Builder builder = (new com.google.android.gcm.server.MulticastResult.Builder(var21, failure, canonicalIds, var22)).retryMulticastIds(multicastIds);
            Iterator i$ = regIds.iterator();

            while(i$.hasNext()) {
                String regId = (String)i$.next();
                Result result1 = (Result)results.get(regId);
                builder.addResult(result1);
            }

            return builder.build();
        }
    }

    private List<String> updateStatus(List<String> unsentRegIds, Map<String, Result> allResults, MulticastResult multicastResult) {
        List results = multicastResult.getResults();
        if(results.size() != unsentRegIds.size()) {
            throw new RuntimeException("Internal error: sizes do not match. currentResults: " + results + "; unsentRegIds: " + unsentRegIds);
        } else {
            ArrayList newUnsentRegIds = new ArrayList();

            for(int i = 0; i < unsentRegIds.size(); ++i) {
                String regId = (String)unsentRegIds.get(i);
                Result result = (Result)results.get(i);
                allResults.put(regId, result);
                String error = result.getErrorCodeName();
                if(error != null && (error.equals("Unavailable") || error.equals("InternalServerError"))) {
                    newUnsentRegIds.add(regId);
                }
            }

            return newUnsentRegIds;
        }
    }

    public MulticastResult sendNoRetry(Message message, List<String> registrationIds) throws IOException {
        if(((List)nonNull(registrationIds)).isEmpty()) {
            throw new IllegalArgumentException("registrationIds cannot be empty");
        } else {
            HashMap jsonRequest = new HashMap();
            this.messageToMap(message, jsonRequest);
            jsonRequest.put("registration_ids", registrationIds);
            String responseBody = this.makeGcmHttpRequest(jsonRequest);
            if(responseBody == null) {
                return null;
            } else {
                JSONParser parser = new JSONParser();

                try {
                    JSONObject jsonResponse = (JSONObject)parser.parse(responseBody);
                    int e = this.getNumber(jsonResponse, "success").intValue();
                    int failure = this.getNumber(jsonResponse, "failure").intValue();
                    int canonicalIds = this.getNumber(jsonResponse, "canonical_ids").intValue();
                    long multicastId = this.getNumber(jsonResponse, "multicast_id").longValue();
                    com.google.android.gcm.server.MulticastResult.Builder builder = new com.google.android.gcm.server.MulticastResult.Builder(e, failure, canonicalIds, multicastId);
                    List results = (List)jsonResponse.get("results");
                    if(results != null) {
                        Iterator i$ = results.iterator();

                        while(i$.hasNext()) {
                            Map jsonResult = (Map)i$.next();
                            String messageId = (String)jsonResult.get("message_id");
                            String canonicalRegId = (String)jsonResult.get("registration_id");
                            String error = (String)jsonResult.get("error");
                            Result result = (new Builder()).messageId(messageId).canonicalRegistrationId(canonicalRegId).errorCode(error).build();
                            builder.addResult(result);
                        }
                    }

                    return builder.build();
                } catch (ParseException var20) {
                    throw this.newIoException(responseBody, var20);
                } catch (Sender.CustomParserException var21) {
                    throw this.newIoException(responseBody, var21);
                }
            }
        }
    }

    private String makeGcmHttpRequest(Map<Object, Object> jsonRequest) throws InvalidRequestException {
        String requestBody = JSONValue.toJSONString(jsonRequest);
        logger.finest("JSON request: " + requestBody);
        System.out.println("request body is " + requestBody);

        HttpURLConnection conn;
        int status;
        try {
            System.out.println("sending request to GCM server");
            conn = this.post("https://fcm.googleapis.com/fcm/send", "application/json", requestBody);
            System.out.println("request sent to GCM server");
            //System.out.println("conn properties are " + conn.getContent().toString());
            status = conn.getResponseCode();
            System.out.println("status is " + status);
        } catch (IOException var9) {
            logger.log(Level.FINE, "IOException posting to GCM", var9);
            return null;
        }

        String responseBody;
        if(status != 200) {
            try {
                responseBody = getAndClose(conn.getErrorStream());
                logger.finest("JSON error response: " + responseBody);
            } catch (IOException var7) {
                responseBody = "N/A";
                logger.log(Level.FINE, "Exception reading response: ", var7);
            }

            throw new InvalidRequestException(status, responseBody);
        } else {
            try {
                responseBody = getAndClose(conn.getInputStream());
            } catch (IOException var8) {
                logger.log(Level.WARNING, "IOException reading response", var8);
                return null;
            }

            logger.finest("JSON response: " + responseBody);
            return responseBody;
        }
    }

    private void messageToMap(Message message, Map<Object, Object> mapRequest) {
        if(message != null && mapRequest != null) {
            this.setJsonField(mapRequest, "priority", message.getPriority());
            this.setJsonField(mapRequest, "time_to_live", message.getTimeToLive());
            this.setJsonField(mapRequest, "collapse_key", message.getCollapseKey());
            this.setJsonField(mapRequest, "restricted_package_name", message.getRestrictedPackageName());
            this.setJsonField(mapRequest, "delay_while_idle", message.isDelayWhileIdle());
            this.setJsonField(mapRequest, "dry_run", message.isDryRun());
            Map payload = message.getData();
            if(!payload.isEmpty()) {
                mapRequest.put("data", payload);
            }

            if(message.getNotification() != null) {
                Notification notification = message.getNotification();
                HashMap nMap = new HashMap();
                if(notification.getBadge() != null) {
                    this.setJsonField(nMap, "badge", notification.getBadge().toString());
                }

                this.setJsonField(nMap, "body", notification.getBody());
                this.setJsonField(nMap, "body_loc_args", notification.getBodyLocArgs());
                this.setJsonField(nMap, "body_loc_key", notification.getBodyLocKey());
                this.setJsonField(nMap, "click_action", notification.getClickAction());
                this.setJsonField(nMap, "color", notification.getColor());
                this.setJsonField(nMap, "icon", notification.getIcon());
                this.setJsonField(nMap, "sound", notification.getSound());
                this.setJsonField(nMap, "tag", notification.getTag());
                this.setJsonField(nMap, "title", notification.getTitle());
                this.setJsonField(nMap, "title_loc_args", notification.getTitleLocArgs());
                this.setJsonField(nMap, "title_loc_key", notification.getTitleLocKey());
                mapRequest.put("notification", nMap);
            }

        }
    }

    private IOException newIoException(String responseBody, Exception e) {
        String msg = "Error parsing JSON response (" + responseBody + ")";
        logger.log(Level.WARNING, msg, e);
        return new IOException(msg + ":" + e);
    }

    private static void close(Closeable closeable) {
        if(closeable != null) {
            try {
                closeable.close();
            } catch (IOException var2) {
                logger.log(Level.FINEST, "IOException closing stream", var2);
            }
        }

    }

    private void setJsonField(Map<Object, Object> json, String field, Object value) {
        if(value != null) {
            json.put(field, value);
        }

    }

    private Number getNumber(Map<?, ?> json, String field) {
        Object value = json.get(field);
        if(value == null) {
            throw new Sender.CustomParserException("Missing field: " + field);
        } else if(!(value instanceof Number)) {
            throw new Sender.CustomParserException("Field " + field + " does not contain a number: " + value);
        } else {
            return (Number)value;
        }
    }

    protected HttpURLConnection post(String url, String body) throws IOException {
        return this.post(url, "application/x-www-form-urlencoded;charset=UTF-8", body);
    }

    protected HttpURLConnection post(String url, String contentType, String body) throws IOException {
        if(url != null && contentType != null && body != null) {
            if(!url.startsWith("https://")) {
                logger.warning("URL does not use https: " + url);
            }

            System.out.println("a---------");
            logger.fine("Sending POST to " + url);
            logger.finest("POST body: " + body);
            byte[] bytes = body.getBytes("UTF-8");
            HttpURLConnection conn = this.getConnection(url);
            System.out.println("b---------");
            conn.setDoOutput(true);
            System.out.println("b---------");
            conn.setUseCaches(false);
            System.out.println("b---------");
            conn.setFixedLengthStreamingMode(bytes.length);
            System.out.println("b---------");
            conn.setRequestMethod("POST");
            System.out.println("b---------");
            conn.setRequestProperty("Content-Type", contentType);
            System.out.println("b---------");
            conn.setRequestProperty("Authorization", "key=" + this.key);
            System.out.println("b---------");
            System.out.println("conn is" );
            System.out.println("b---------");
            OutputStream out = conn.getOutputStream();
            System.out.println("c---------");

            //System.out.println("conn is " + conn.getContent().toString());

            try {
                System.out.println("d---------");
                out.write(bytes);
                System.out.println("e---------");
                System.out.println("---------");
                System.out.println("---------");
            } finally {
                close(out);
            }

            return conn;
        } else {
            throw new IllegalArgumentException("arguments cannot be null");
        }
    }

    protected static final Map<String, String> newKeyValues(String key, String value) {
        HashMap keyValues = new HashMap(1);
        keyValues.put(nonNull(key), nonNull(value));
        return keyValues;
    }

    protected static StringBuilder newBody(String name, String value) {
        return (new StringBuilder((String)nonNull(name))).append('=').append((String)nonNull(value));
    }

    protected static void addParameter(StringBuilder body, String name, String value) {
        ((StringBuilder)nonNull(body)).append('&').append((String)nonNull(name)).append('=').append((String)nonNull(value));
    }

    protected HttpURLConnection getConnection(String url) throws IOException {
        return (HttpURLConnection)(new URL(url)).openConnection();
    }

    protected static String getString(InputStream stream) throws IOException {
        if(stream == null) {
            return "";
        } else {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            StringBuilder content = new StringBuilder();

            String newLine;
            do {
                newLine = reader.readLine();
                if(newLine != null) {
                    content.append(newLine).append('\n');
                }
            } while(newLine != null);

            if(content.length() > 0) {
                content.setLength(content.length() - 1);
            }

            return content.toString();
        }
    }

    private static String getAndClose(InputStream stream) throws IOException {
        String var1;
        try {
            var1 = getString(stream);
        } finally {
            if(stream != null) {
                close(stream);
            }

        }

        return var1;
    }

    static <T> T nonNull(T argument) {
        if(argument == null) {
            throw new IllegalArgumentException("argument cannot be null");
        } else {
            return argument;
        }
    }

    void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException var4) {
            Thread.currentThread().interrupt();
        }

    }

    class CustomParserException extends RuntimeException {
        CustomParserException(String message) {
            super(message);
        }
    }
}
