package gitlet;

import java.io.Serializable;
import java.util.HashSet;
public class Branch implements Serializable {

    // HashSet containing all current branch names
    private HashSet<String> branchNames = new HashSet<>();
    private String currBranch;

    public Branch() {
        currBranch = "master";
        branchNames.add("master");
    }

    public String getCurrBranchName() {
        return currBranch;
    }

    public void updateCurrBranch(String name) {
        currBranch = name;
    }

    public HashSet<String> getBranchNames() {
        return this.branchNames;
    }
}
