package gitlet;

import java.io.Serializable;
import java.util.HashSet;

// Perhaps separate Commit from CommitNode
// Commit will only contain a hash set of all commit ids + current head id
// CommitNode will contain all the information for each Commit
// Serialize each CommitNode, use Commit to access the current CommitNode
public class Commit implements Serializable {


    private String headID;                                       // Points to current CommitNode
    private HashSet<String> allCommitsID = new HashSet<>();        // Commit ID : CommitNode


    // Used when initiating a new Gitlet repo
    public Commit(String hashID) {
        headID = hashID;
        allCommitsID.add(headID);
    }

    // Have this take in string BranchName, update the branchName's node accordingly
    public void updateHead(String hashID) {
        headID = hashID;
    }


    public String getHeadID() {
        return headID;
    }

    public HashSet<String> getAllCommitsID() {
        return allCommitsID;
    }

    public void updateAllCommitsID(String commitID) {
        allCommitsID.add(commitID);
    }


}
