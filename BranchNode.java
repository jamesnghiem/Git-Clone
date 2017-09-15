package gitlet;

import java.io.Serializable;
import java.util.LinkedHashMap;

// Serialize the BranchNode as the branch name
public class BranchNode implements Serializable {
    private String branchHeadID;
    private String branchName;
    // Maps all ancestor branches to split points (commitID)
    private LinkedHashMap<String, String> splitHistory;

    public BranchNode(String bname, String headID, LinkedHashMap<String, String> prevSH) {
        branchName = bname;
        branchHeadID = headID;
        splitHistory = prevSH;

    }

    public String getBranchName() {
        return branchName;
    }

    public LinkedHashMap<String, String> getSplitHistory() {
        return splitHistory;
    }

    public void updateSplitHistory(String bname, String splitID) {
        splitHistory.put(bname, splitID);
    }

    public String getBranchHeadID() {
        return branchHeadID;
    }

    public void updateBranchHeadID(String headID) {
        branchHeadID = headID;
    }

}
