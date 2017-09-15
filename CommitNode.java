package gitlet;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class CommitNode implements Serializable {

    private String parentID;                        // Parent commit hash ID
    private String logMessage;                      // commit log message
    private String timeStamp;                       // Time of commit
    private HashMap<String, String> blobs;          // file name to hash content value hashmap
    private Date time;

    public CommitNode(String message, String prevID, HashMap<String, String> blob) {
        // Look into staging to get file to commit
        // Compare parent file names with commit file names to find missing files

        // First Commit
        if (prevID == null) {
            parentID = null;
        } else {
            parentID = prevID;
        }

        // commitID should be serialized object hash
        logMessage = message;
        time = new Date();
        timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(time);
        blobs = blob;
    }

    /* Getter Methods */
    public String getParentID() {
        return this.parentID;
    }

    public String getLogMessage() {
        return this.logMessage;
    }

    public String getTimeStamp() {
        return this.timeStamp;
    }

    public Date getTime() {
        return this.time;
    }

    public HashMap<String, String> getBlobs() {
        return this.blobs;
    }
}

