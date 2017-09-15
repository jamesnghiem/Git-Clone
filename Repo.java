package gitlet;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.ArrayList;

import static gitlet.Utils.restrictedDelete;
import static gitlet.Utils.sha1;

public class Repo implements CommandInterface {

    /****                          ****
     ****   DANK HELPER METHODS    ****
     ****                          ****/

    File pwd = new File(System.getProperty("user.dir"));

    // Returns true if .gitlet directory exists, else false
    public boolean checkInit() {
        File gitletCheck = new File(pwd, ".gitlet");
        return gitletCheck.exists();
    }

    // Returns true if file exists in working directory, else false
    public boolean checkFileExists(String fileName) {
        File file = new File(pwd, ".");
        List<String> allFiles = Utils.plainFilenamesIn(file.getAbsolutePath());
        return allFiles.contains(fileName);
    }

    //writes a file to the working directory/ for checkout etc.
    public void writeFileToWorkingDir(String fileName, byte[] bytes) {
        File f = new File(pwd, "/" + fileName);
        Utils.writeContents(f, bytes);
    }

    //writes new blobs. uses .txt May need to change that later
    public void writeFileToBlob(String fileName, byte[] bytes) {
        File f = new File(pwd, ".gitlet/.blobs/" + fileName + ".txt");
        Utils.writeContents(f, bytes);
    }
    // Use to add new branches / update branch heads
    public void writeFileToBranch(String fileName, Object obj) {
        File f = new File(pwd, ".gitlet/.branches/" + fileName + ".ser");
        Utils.writeContents(f, FileIO.serialize(obj));
    }

    // Use ONLY for branches.ser + commitTree.ser
    public void writeFileToGitlet(String fileName, Object obj) {
        File f = new File(pwd, ".gitlet/" + fileName + ".ser");
        Utils.writeContents(f, FileIO.serialize(obj));
    }

    // Use to create new commit node files
    public void writeFileToCommit(String fileName, Object obj) {
        File f = new File(pwd, ".gitlet/.commits/" + fileName + ".ser");
        Utils.writeContents(f, FileIO.serialize(obj));
    }

    //Reads a file from the working directory
    public byte[] readFileFromWorkingDir(String fileName) {
        File f = new File(pwd, "/" + fileName);
        return Utils.readContents(f);
    }

    public byte[] readFileFromGitlet(String fileName) {
        File f = new File(pwd, ".gitlet/" + fileName + ".ser");
        return Utils.readContents(f);
    }

    public byte[] readFileFromBranch(String fileName) {
        File f = new File(pwd, ".gitlet/.branches/" + fileName + ".ser");
        return Utils.readContents(f);
    }

    public byte[] readFileFromCommit(String fileName) {
        File f = new File(pwd, ".gitlet/.commits/" + fileName + ".ser");
        return Utils.readContents(f);
    }

    public byte[] readFileFromBlob(String fileName) {
        File f = new File(pwd, ".gitlet/.blobs/" + fileName + ".txt");
        return Utils.readContents(f);
    }

    public String getCurrHeadID() {
        Branch branch = (Branch) FileIO.deserialize(readFileFromGitlet("branches"));
        BranchNode currBranch = (BranchNode)
                FileIO.deserialize(readFileFromBranch(branch.getCurrBranchName()));
        return currBranch.getBranchHeadID();
    }

    public String getSplitPoint(BranchNode currBranch, BranchNode otherBranch) {
        LinkedHashMap<String, String> currBranchSH = currBranch.getSplitHistory();
        LinkedHashMap<String, String> otherBranchSH = otherBranch.getSplitHistory();
        String splitPointCommit;

        // Base Cases - Check if branch name is contained in each split history
        if (currBranchSH.containsKey(otherBranch.getBranchName())) {
            splitPointCommit = currBranchSH.get(otherBranch.getBranchName());

        } else if (otherBranchSH.containsKey(currBranch.getBranchName())) {
            splitPointCommit = otherBranchSH.get(currBranch.getBranchName());


        // Algorithm - iterate through and find the oldest common ancestor
        // Compare the two commitID's time of commit and get the oldest one
        } else {
            String oldestAncestorBranch = "master";
            for (String bname : currBranchSH.keySet()) {
                if (otherBranchSH.containsKey(bname)) {
                    oldestAncestorBranch = bname;
                } else {
                    break;
                }
            }
            String currCommitID = currBranchSH.get(oldestAncestorBranch);
            String otherCommitID = otherBranchSH.get(oldestAncestorBranch);

            CommitNode currCommit = (CommitNode)
                    FileIO.deserialize(readFileFromCommit(currCommitID));
            CommitNode otherCommit = (CommitNode)
                    FileIO.deserialize(readFileFromCommit(otherCommitID));

            if (currCommit.getTime().before(otherCommit.getTime())) {
                splitPointCommit = currCommitID;
            } else {
                splitPointCommit = otherCommitID;
            }
        }
        return splitPointCommit;
    }

    // ---------------------------------------------------------------------- //

    @Override
    public void init() {
        // Check if gitlet has already been initialized
        if (checkInit()) {
            System.out.println("A gitlet version-control system already "
                    + "exists in the current directory.");
            return;

        //Create new gitlet directory
        } else {
            File newRepo = new File(pwd, ".gitlet");
            newRepo.mkdir();

            // Creating subdirectories within .gitlet
            // .branches contains all branch.ser files
            // .commit contains all commitNode.ser files
            // .stage contains the stagingArea.ser file
            File blobFolder = new File(pwd, ".gitlet/.blobs");
            File branchFolder = new File(pwd, ".gitlet/.branches");
            File commitFolder = new File(pwd, ".gitlet/.commits");

            blobFolder.mkdir();
            branchFolder.mkdir();
            commitFolder.mkdir();

            CommitNode firstCommit =
                    new CommitNode("initial commit", null, new HashMap<String, String>());
            String firstCommitNodeID = sha1(FileIO.serialize(firstCommit));
            Commit commitTree = new Commit(firstCommitNodeID);
            Branch branches = new Branch();
            BranchNode masterBranch = new
                    BranchNode("master", firstCommitNodeID, new LinkedHashMap<String, String>());
            Staging stagingArea = new Staging();

            writeFileToGitlet("commitTree", commitTree);
            writeFileToGitlet("branches", branches);
            writeFileToBranch("master", masterBranch);
            writeFileToCommit(firstCommitNodeID, firstCommit);
            writeFileToGitlet("stagingArea", stagingArea);
        }
    }

    @Override
    public void add(String fileName) {
        if (!checkInit()) {
            System.out.println("A gitlet version-control system does "
                    + "not exist in the current directory.");
            return;
        } else if (!checkFileExists(fileName)) {
            System.out.println("File does not exist.");
            return;
        }
        Staging currStaging = (Staging) FileIO.deserialize(readFileFromGitlet("stagingArea"));
        CommitNode currCommit = (CommitNode) FileIO.deserialize(
                readFileFromCommit(getCurrHeadID()));

        String hashedFileContent = sha1(readFileFromWorkingDir(fileName));

        if (currStaging.getFilesToRemove().containsKey(fileName)) {
            currStaging.removeFromFTR(fileName);
        }

        if (currCommit.getBlobs().containsKey(fileName)
                && currCommit.getBlobs().get(fileName).equals(hashedFileContent)) {
            writeFileToGitlet("stagingArea", currStaging);
            return;
        }

        currStaging.addToFTA(fileName, sha1(readFileFromWorkingDir(fileName)));
        currStaging.addToFTC(fileName, readFileFromWorkingDir(fileName));
        writeFileToGitlet("stagingArea", currStaging);
    }

    @Override
    /** CHECK WHICH BRANCH YOU ARE IN BEFORE ADDING AND UPDATE THE BRANCH'S HEAD*/
    public void commit(String message) {
        if (!checkInit()) {
            System.out.println("Not in an initialized gitlet directory.");
            return;

        // checks for an empty message
        } else if (message.equals("")) {
            System.out.println("Please enter a commit message.");
            return;
        }

        Staging stagingArea = (Staging) FileIO.deserialize(readFileFromGitlet("stagingArea"));
        Branch branch = (Branch) FileIO.deserialize(readFileFromGitlet("branches"));
        BranchNode currBranch =
                (BranchNode) FileIO.deserialize(readFileFromBranch(branch.getCurrBranchName()));
        Commit commitTree = (Commit) FileIO.deserialize(readFileFromGitlet("commitTree"));

        String currHeadID = getCurrHeadID();
        CommitNode currHead = (CommitNode) FileIO.deserialize(readFileFromCommit(currHeadID));

        HashMap<String, String> currHeadBlobs = currHead.getBlobs();
        HashMap<String, String> filesToAdd = stagingArea.getFilesToAdd();
        HashMap<String, String> filesToRemove = stagingArea.getFilesToRemove();
        HashMap<String, byte[]> filesToCopy = stagingArea.getFilesToCopy();

        // Check that the staging area contains staged files
        if (filesToAdd.isEmpty() && filesToRemove.isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        }

        // Create HashMap of files tracked
        HashMap<String, String> blobsToAdd = new HashMap<>();

        for (String fileName : filesToAdd.keySet()) {
            blobsToAdd.put(fileName, filesToAdd.get(fileName));
        }

        for (String fileName : currHeadBlobs.keySet()) {
            if (!blobsToAdd.containsKey(fileName) && !filesToRemove.containsKey(fileName)) {
                blobsToAdd.put(fileName, currHeadBlobs.get(fileName));
            }
        }

        CommitNode newHead = new CommitNode(message, currHeadID, blobsToAdd);
        String newHeadID = sha1(FileIO.serialize(newHead));

        for (byte[] fileContent : filesToCopy.values()) {
            writeFileToBlob(sha1(fileContent), fileContent);
        }

        // Clear staging area, Update current branch pointer, update commitTree
        stagingArea.clearItems();
        currBranch.updateBranchHeadID(newHeadID);
        commitTree.updateAllCommitsID(newHeadID);

        // Update all modified ser, add commit to .gitlet/.commits
        writeFileToGitlet("commitTree", commitTree);
        writeFileToBranch(currBranch.getBranchName(), currBranch);
        writeFileToCommit(newHeadID, newHead);
        writeFileToGitlet("stagingArea", stagingArea);

    }

    @Override
    public void rm(String fileName) {
        if (!checkInit()) {
            System.out.println("A gitlet version-control "
                    + "system does not exist in the current directory.");
            return;
        }
        Staging stagingArea = (Staging) FileIO.deserialize(readFileFromGitlet("stagingArea"));
        CommitNode currHead = (CommitNode) FileIO.deserialize(readFileFromCommit(getCurrHeadID()));
        HashMap<String, String> currBlobs = currHead.getBlobs();

        if (!checkFileExists(fileName)) {
            if (stagingArea.getFilesToAdd().containsKey(fileName)) {
                stagingArea.getFilesToAdd().remove(fileName);
                writeFileToGitlet("stagingArea", stagingArea);
                return;
            } else {
                stagingArea.addToFTR(fileName, null);
                writeFileToGitlet("stagingArea", stagingArea);
                return;
            }
        }
        // Check if the file has been staged / is being tracked by the head commit
        if (!currBlobs.containsKey(fileName)
                && !stagingArea.getFilesToAdd().containsKey(fileName)) {
            System.out.println("No reason to remove the file.");
            return;
        }
        // Remove from FilesToAdd on the staging area
        if (stagingArea.getFilesToAdd().containsKey(fileName)) {
            stagingArea.getFilesToCopy().remove(stagingArea.getFilesToAdd().get(fileName));
            stagingArea.getFilesToAdd().remove(fileName);
            writeFileToGitlet("stagingArea", stagingArea);
            return;
        }

        // Add to list of files to remove in the staging area
        stagingArea.getFilesToRemove().put(fileName,
                    sha1(readFileFromWorkingDir(fileName)));
        // Remove from working directory if tracked in the current commit
        if (currBlobs.containsKey(fileName)) {
            File delete = new File(pwd, "/" + fileName);
            restrictedDelete(delete);
        }

        writeFileToGitlet("stagingArea", stagingArea);
    }

    @Override
    public void log() {
        if (!checkInit()) {
            System.out.println("Not in an initialized gitlet directory.");
            return;
        }

        String currHeadID = getCurrHeadID();
        CommitNode curr;

        while (currHeadID != null) {
            curr = (CommitNode) FileIO.deserialize(readFileFromCommit(currHeadID));
            System.out.println("===");
            System.out.println("Commit " + currHeadID);
            System.out.println(curr.getTimeStamp());
            System.out.println(curr.getLogMessage());
            System.out.println();

            currHeadID = curr.getParentID();
        }
    }

    @Override
    public void globalLog() {
        if (!checkInit()) {
            System.out.println("Not in an initialized gitlet directory.");
            return;
        }

        Commit commitTree = (Commit) FileIO.deserialize(readFileFromGitlet("commitTree"));
        HashSet<String> allCommitsID = commitTree.getAllCommitsID();
        CommitNode curr;

        for (String commitID : allCommitsID) {
            curr = (CommitNode) FileIO.deserialize(readFileFromCommit(commitID));
            System.out.println("===");
            System.out.println("Commit " + commitID);
            System.out.println(curr.getTimeStamp());
            System.out.println(curr.getLogMessage());
            System.out.println();

        }
    }

    @Override
    public void find(String message) {
        if (!checkInit()) {
            System.out.println("Not in an initialized gitlet directory.");
            return;
        }

        Commit commitTree = (Commit) FileIO.deserialize(readFileFromGitlet("commitTree"));
        HashSet<String> allCommitsID = commitTree.getAllCommitsID();
        CommitNode curr;
        int count = 0;

        for (String commitID : allCommitsID) {
            curr = (CommitNode) FileIO.deserialize(readFileFromCommit(commitID));
            if (curr.getLogMessage().equals(message)) {
                System.out.println(commitID);
                count++;
            }
        }

        if (count == 0) {
            System.out.println("Found no commit with that message.");
        }
    }

    @Override
    public void status() {
        if (!checkInit()) {
            System.out.println("Not in an initialized gitlet directory.");
            return;
        }
        Branch currBranch = (Branch) FileIO.deserialize(readFileFromGitlet("branches"));
        Staging currstaging = (Staging) FileIO.deserialize(readFileFromGitlet("stagingArea"));
        System.out.println("=== Branches ===");
        ArrayList<String> helper = new ArrayList<>();
        currBranch.getBranchNames().forEach((String branchName) -> helper.add(branchName));
        helper.sort((String input1, String input2) -> input1.compareTo(input2));
        helper.forEach((String name) -> {
            if (name.equals(currBranch.getCurrBranchName())) {
                System.out.println("*" + name);
            } else {
                System.out.println(name);
            }
        });
        helper.clear();
        System.out.println();
        System.out.println("=== Staged Files ===");
        for (String name : currstaging.getFilesToAdd().keySet()) {
            helper.add(name);
        }
        helper.sort((String input1, String input2) -> input1.compareTo(input2));
        helper.forEach((String name) -> System.out.println(name)
        );
        helper.clear();
        System.out.println();
        System.out.println("=== Removed Files ===");
        for (String name : currstaging.getFilesToRemove().keySet()) {
            helper.add(name);
        }
        helper.sort((String input1, String input2) -> input1.compareTo(input2));
        helper.forEach((String name) -> System.out.println(name)
        );
        helper.clear();
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
        System.out.println();
    }

    @Override
    public void checkout1(String fileName) {
        if (!checkInit()) {
            System.out.println("Not in an initialized gitlet directory.");
            return;
        } else {
            CommitNode currHead = (CommitNode) FileIO.deserialize(
                    readFileFromCommit(getCurrHeadID()));
            if (!currHead.getBlobs().containsKey(fileName)) {
                System.out.println("File does not exist in that commit.");
                return;
            } else {
                writeFileToWorkingDir(fileName,
                        readFileFromBlob(currHead.getBlobs().get(fileName)));
                return;
            }
        }
    }

    @Override
    public void checkout2(String commitId, String fileName) {
        if (!checkInit()) {
            System.out.println("Not in an initialized gitlet directory");
            return;
        }

        CommitNode currNode;
        if (commitId.length() < 40) {
            File f = new File(pwd, "/.gitlet/.commits/.");
            List<String> allCommits = Utils.plainFilenamesIn(f.getAbsolutePath());

            String realCommitID = null;
            for (String name : allCommits) {
                if (name.toLowerCase().contains(commitId.toLowerCase())) {
                    int iend = name.indexOf(".");
                    realCommitID = name.substring(0, iend);
                    break;
                }
            }

            if (realCommitID == null) {
                System.out.println("No commit with that id exists.");
                return;
            } else {
                currNode = (CommitNode) FileIO.deserialize(readFileFromCommit(realCommitID));
            }

        } else {
            Commit currCommit = (Commit) FileIO.deserialize(readFileFromGitlet("commitTree"));
            if (!currCommit.getAllCommitsID().contains(commitId)) {
                System.out.println("No commit with that id exists.");
                return;
            }
            currNode = (CommitNode) FileIO.deserialize(readFileFromCommit(commitId));
        }

        if (!currNode.getBlobs().containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            return;
        } else {
            writeFileToWorkingDir(fileName,
                    readFileFromBlob(currNode.getBlobs().get(fileName)));
            return;
        }

    }

    @Override
    public void checkout3(String branchName) {
        if (!checkInit()) {
            System.out.println("Not in an initialized gitlet directory.");
            return;
        } else {
            Branch currBranch = (Branch) FileIO.deserialize(readFileFromGitlet("branches"));
            if (!currBranch.getBranchNames().contains(branchName)) {
                System.out.println("No such branch exists.");
                return;
            } else if (branchName.equals(currBranch.getCurrBranchName())) {
                System.out.println("No need to checkout the current branch.");
                return;
            }
            BranchNode givenBranch = (BranchNode) FileIO.deserialize(
                    readFileFromBranch(branchName));
            CommitNode givenCommit = (CommitNode) FileIO.deserialize(readFileFromCommit(
                    givenBranch.getBranchHeadID()));
            CommitNode currCommit = (CommitNode) FileIO.deserialize(readFileFromCommit(
                    getCurrHeadID()));
            for (String fileName : givenCommit.getBlobs().keySet()) {
                if (checkFileExists(fileName) && !currCommit.getBlobs().containsKey(fileName)
                        && !readFileFromWorkingDir(fileName).equals(
                                readFileFromBlob(givenCommit.getBlobs().get(fileName)))) {
                    System.out.println("There is an untracked file in "
                            + "the way; delete it or add it first.");
                    System.exit(0);
                }
            }
            for (String fileName : givenCommit.getBlobs().keySet()) {
                writeFileToWorkingDir(fileName, readFileFromBlob(
                        givenCommit.getBlobs().get(fileName)));
            }
            for (String fileName : currCommit.getBlobs().keySet()) {
                if (!givenCommit.getBlobs().containsKey(fileName)) {
                    File f = new File(pwd, fileName);
                    f.delete();
                }
            }
            currBranch.updateCurrBranch(branchName);
            Staging currStaging = (Staging) FileIO.deserialize(readFileFromGitlet("stagingArea"));
            currStaging.clearItems();
            writeFileToGitlet("branches", currBranch);
            writeFileToGitlet("stagingArea", currStaging);
        }

    }

    @Override
    public void branch(String bname) {
        if (!checkInit()) {
            System.out.println("Not in an initialized gitlet directory");
            return;
        }

        Branch allBranches = (Branch) FileIO.deserialize(readFileFromGitlet("branches"));
        BranchNode currBranch = (BranchNode)
                FileIO.deserialize(readFileFromBranch(allBranches.getCurrBranchName()));

        // Check: A branch with that name does not currently exist
        if (allBranches.getBranchNames().contains(bname)) {
            System.out.println("A branch with that name already exists.");
            return;
        }

        String currHeadID = getCurrHeadID();
        allBranches.getBranchNames().add(bname);
        BranchNode newBranch = new BranchNode(bname, currHeadID, currBranch.getSplitHistory());
        currBranch.updateSplitHistory(bname, currHeadID);
        newBranch.updateSplitHistory(currBranch.getBranchName(), currHeadID);

        writeFileToBranch(currBranch.getBranchName(), currBranch);
        writeFileToBranch(bname, newBranch);
        writeFileToGitlet("branches", allBranches);
    }

    @Override
    public void rmBranch(String bname) {
        if (!checkInit()) {
            System.out.println("Not in an initialized gitlet directory");
            return;
        }

        Branch allBranches = (Branch) FileIO.deserialize(readFileFromGitlet("branches"));

        if (!allBranches.getBranchNames().contains(bname)) {
            System.out.println("A branch with that name does not exist.");
            return;
        } else if (allBranches.getCurrBranchName().equals(bname)) {
            System.out.println("Cannot remove the current branch.");
            return;
        }

        allBranches.getBranchNames().remove(bname);
        restrictedDelete("./gitlet/.branches/" + bname + ".ser");

        writeFileToGitlet("branches", allBranches);
    }

    @Override
    public void reset(String commitID) {
        if (!checkInit()) {
            System.out.println("Not in an initialized gitlet directory");
            return;
        }

        Staging stagingArea = (Staging) FileIO.deserialize(readFileFromGitlet("stagingArea"));
        HashMap<String, String> filesToAdd = stagingArea.getFilesToAdd();
        Commit commitTree = (Commit) FileIO.deserialize(readFileFromGitlet("commitTree"));
        CommitNode currCommit = (CommitNode)
                FileIO.deserialize(readFileFromCommit(getCurrHeadID()));
        HashMap<String, String> currBlobs = currCommit.getBlobs();
        File file = new File(pwd, ".");
        List<String> allFiles = Utils.plainFilenamesIn(file.getAbsolutePath());

        // No commit exists with the provided ID
        if (!commitTree.getAllCommitsID().contains(commitID)) {
            System.out.println("No commit with that id exists.");
            return;
        }

        // Check if any working files are currently untracked in the branch
        for (String fileName : allFiles) {
            if (!filesToAdd.containsKey(fileName)) {
                if (!currBlobs.containsKey(fileName)) {
                    System.out.println("There is an untracked file in the way;"
                            + " delete it or add it first.");
                    return;
                }
            }
        }

        Branch allBranches = (Branch) FileIO.deserialize(readFileFromGitlet("branches"));
        BranchNode currBranch = (BranchNode)
                FileIO.deserialize(readFileFromBranch(allBranches.getCurrBranchName()));
        CommitNode resetCommit = (CommitNode) FileIO.deserialize(readFileFromCommit(commitID));
        HashMap<String, String> resetBlobs = resetCommit.getBlobs();

        for (String fileName: allFiles) {
            File delete = new File(pwd, "/" + fileName);
            restrictedDelete(delete);
        }

        for (String fileName: resetBlobs.keySet()) {
            checkout2(commitID, fileName);
        }

        stagingArea.clearItems();
        currBranch.updateBranchHeadID(commitID);
        writeFileToGitlet("stagingArea", stagingArea);
        writeFileToBranch(currBranch.getBranchName(), currBranch);
    }

    @Override
    public void merge(String bname) {
        if (!checkInit()) {
            System.out.println("Not in an initialized gitlet directory");
            return;
        }
        Staging stagingArea = (Staging) FileIO.deserialize(readFileFromGitlet("stagingArea"));
        Branch allBranches = (Branch) FileIO.deserialize(readFileFromGitlet("branches"));
        String currCommitID = getCurrHeadID();
        CommitNode currCommit = (CommitNode) FileIO.deserialize(readFileFromCommit(currCommitID));
        // Returns split commit id if all cases pass
        String mergeChecker = mergeChecks(bname, stagingArea, currCommit);
        if (mergeChecker == null) {
            return;
        }

        BranchNode givenBranch = (BranchNode) FileIO.deserialize(readFileFromBranch(bname));
        BranchNode currBranch = (BranchNode)
                FileIO.deserialize(readFileFromBranch(allBranches.getCurrBranchName()));
        String givenCommitID = givenBranch.getBranchHeadID();
        CommitNode splitPoint = (CommitNode) FileIO.deserialize(readFileFromCommit(mergeChecker));
        CommitNode givenCommit = (CommitNode) FileIO.deserialize(readFileFromCommit(givenCommitID));
        HashMap<String, String> spBlobs = splitPoint.getBlobs();
        HashMap<String, String> currBlobs = currCommit.getBlobs();
        HashMap<String, String> givenBlobs = givenCommit.getBlobs();

        boolean mergeConflict = false;
        for (String fileName : spBlobs.keySet()) {
            String sphash = spBlobs.get(fileName);
            // Stage files modified in given branch but not in curr branch from split point
            if (!currBlobs.containsKey(fileName) && !givenBlobs.containsKey(fileName)) {
                int i = 0;
            } else if (!currBlobs.containsKey(fileName)) {
                if (!givenBlobs.get(fileName).equals(sphash)) {
                    mergeConflict = true;
                    writeMergeFile(fileName, currBlobs, givenBlobs);
                }
            } else if (!givenBlobs.containsKey(fileName)) {
                if (currBlobs.get(fileName).equals(sphash)) {
                    rm(fileName);
                } else if (!currBlobs.get(fileName).equals(sphash)) {
                    mergeConflict = true;
                    writeMergeFile(fileName, currBlobs, givenBlobs);
                }
            } else {
                if (currBlobs.get(fileName).equals(sphash)
                        && !givenBlobs.get(fileName).equals(sphash)) {
                    checkout2(givenCommitID, fileName);
                    add(fileName);
                } else if (!currBlobs.get(fileName).equals(spBlobs.get(fileName))
                        && givenBlobs.get(fileName).equals(sphash)) {
                    int i = 0;
                } else if (!currBlobs.get(fileName).equals(sphash)
                        && !givenBlobs.get(fileName).equals(sphash)
                        && !currBlobs.get(fileName).equals(givenBlobs.get(fileName))) {
                    mergeConflict = true;
                    writeMergeFile(fileName, currBlobs, givenBlobs);
                }
            }
        }
        for (String fileName : currBlobs.keySet()) {
            if (!spBlobs.containsKey(fileName)) {
                if (givenBlobs.containsKey(fileName)
                        && !givenBlobs.get(fileName).equals(currBlobs.get(fileName))) {
                    mergeConflict = true;
                    writeMergeFile(fileName, currBlobs, givenBlobs);
                }
            }
        }
        for (String fileName : givenBlobs.keySet()) {
            if (!spBlobs.containsKey(fileName)) {
                if (!currBlobs.containsKey(fileName)) {
                    checkout2(givenCommitID, fileName);
                    add(fileName);
                }
            }
        }
        mergeEnd(mergeConflict, givenBranch.getBranchName(), currBranch.getBranchName());
    }

    public void mergeEnd(boolean mergeConflict, String given, String curr) {
        if (mergeConflict) {
            System.out.println("Encountered a merge conflict.");
            return;
        } else {
            commit("Merged " + curr + " with " + given + ".");
        }
    }

    public void writeMergeFile(String fileName, HashMap<String, String> currBlobs,
                               HashMap<String, String> givenBlobs) {
        String conflictHead = "<<<<<<< HEAD\n", conflictMid = "=======\n",
                conflictEnd = ">>>>>>>\n";
        byte[] conflictHeadBytes = conflictHead.getBytes(), conflictMidBytes =
                conflictMid.getBytes(), conflictEndBytes = conflictEnd.getBytes();
        int chbl = conflictHeadBytes.length, cmbl = conflictMidBytes.length,
                cebl = conflictEndBytes.length;
        if (!currBlobs.containsKey(fileName)) {
            byte[] givenBlobBytes = readFileFromBlob(givenBlobs.get(fileName));
            int gbbl = givenBlobBytes.length;
            byte[] newFileBytes = new byte[gbbl + chbl + cmbl + cebl];
            int i, j;
            for (i = 0; i < chbl; i++) {
                newFileBytes[i] = conflictHeadBytes[i];
            }
            j = i;
            for (; i < j + cmbl; i++) {
                newFileBytes[i] = conflictMidBytes[i - chbl];
            }
            j = i;
            for (; i < j + gbbl; i++) {
                newFileBytes[i] = givenBlobBytes[i - chbl - cmbl];
            }
            j = i;
            for (; i < j + cebl; i++) {
                newFileBytes[i] = conflictEndBytes[i - gbbl - chbl - cmbl];
            }
            writeFileToWorkingDir(fileName, newFileBytes);
        } else if (!givenBlobs.containsKey(fileName)) {
            byte[] currBlobBytes = readFileFromBlob(currBlobs.get(fileName));
            int cbbl = currBlobBytes.length;
            byte[] newFileBytes = new byte[cbbl + chbl + cmbl + cebl];
            int i, j;
            for (i = 0; i < chbl; i++) {
                newFileBytes[i] = conflictHeadBytes[i];
            }
            j = i;
            for (; i < j + cbbl; i++) {
                newFileBytes[i] = currBlobBytes[i - chbl];
            }
            j = i;
            for (; i < j + cmbl; i++) {
                newFileBytes[i] = conflictMidBytes[i - chbl - cbbl];
            }
            j = i;
            for (; i < j + cebl; i++) {
                newFileBytes[i] = conflictEndBytes[i - cbbl - chbl - cmbl];
            }
            writeFileToWorkingDir(fileName, newFileBytes);
        } else {
            byte[] currBlobBytes = readFileFromBlob(currBlobs.get(fileName));
            byte[] givenBlobBytes = readFileFromBlob(givenBlobs.get(fileName));
            int cbbl = currBlobBytes.length;
            int gbbl = givenBlobBytes.length;
            byte[] newFileBytes = new byte[cbbl + gbbl + chbl + cmbl + cebl];
            int i, j;
            for (i = 0; i < chbl; i++) {
                newFileBytes[i] = conflictHeadBytes[i];
            }
            j = i;
            for (; i < j + cbbl; i++) {
                newFileBytes[i] = currBlobBytes[i - chbl];
            }
            j = i;
            for (; i < j + cmbl; i++) {
                newFileBytes[i] = conflictMidBytes[i - chbl - cbbl];
            }
            j = i;
            for (; i < j + gbbl; i++) {
                newFileBytes[i] = givenBlobBytes[i - chbl - cbbl - cmbl];
            }
            j = i;
            for (; i < j + cebl; i++) {
                newFileBytes[i] = conflictEndBytes[i - chbl - cbbl - cmbl - gbbl];
            }
            writeFileToWorkingDir(fileName, newFileBytes);
        }
    }

    public String mergeChecks(String bname, Staging s, CommitNode curr) {
        // Check that there are no staged additions/removals
        if (!s.getFilesToAdd().isEmpty() || !s.getFilesToRemove().isEmpty()) {
            System.out.println("You have uncommitted changes.");
            return null;
        }

        // Check that a branch with the given name exists
        Branch allBranches = (Branch) FileIO.deserialize(readFileFromGitlet("branches"));
        if (!allBranches.getBranchNames().contains(bname)) {
            System.out.println("A branch with that name does not exist.");
            return null;
        }

        // Check that the given branch is not the current branch
        if (allBranches.getCurrBranchName().equals(bname)) {
            System.out.println("Cannot merge a branch with itself.");
            return null;
        }

        File file = new File(pwd, ".");
        List<String> allFiles = Utils.plainFilenamesIn(file.getAbsolutePath());
        HashMap<String, String> filesToAdd = s.getFilesToAdd();
        HashMap<String, String> currBlobs = curr.getBlobs();
        for (String fileName : allFiles) {
            if (!filesToAdd.containsKey(fileName)) {
                if (!currBlobs.containsKey(fileName)) {
                    System.out.println("There is an untracked file in the way;"
                            + " delete it or add it first.");
                    return null;
                }
            }
        }

        BranchNode givenBranch = (BranchNode) FileIO.deserialize(readFileFromBranch(bname));
        BranchNode currBranch = (BranchNode)
                FileIO.deserialize(readFileFromBranch(allBranches.getCurrBranchName()));
        String splitPointID = getSplitPoint(currBranch, givenBranch);
        if (splitPointID.equals(givenBranch.getBranchHeadID())) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return null;
        } else if (splitPointID.equals(currBranch.getBranchHeadID())) {
            System.out.println("Current branch fast-forwarded.");
            currBranch.updateBranchHeadID(givenBranch.getBranchHeadID());
            writeFileToBranch(currBranch.getBranchName(), currBranch);
            return null;
        }
        CommitNode split = (CommitNode) FileIO.deserialize(readFileFromCommit(splitPointID));
        // Check that there are no changes to the files
        HashMap<String, String> spBlobs = split.getBlobs();
        boolean checkForChange = true;
        if (spBlobs.size() == currBlobs.size()) {
            for (String fileName : spBlobs.keySet()) {
                if (!currBlobs.containsKey(fileName)
                        || !currBlobs.get(fileName).equals(spBlobs.get(fileName))) {
                    checkForChange = false;
                    break;
                }
            }
        }
        if (checkForChange) {
            System.out.println("No changes added to the commit.");
            return null;
        }

        return splitPointID;
    }
}
