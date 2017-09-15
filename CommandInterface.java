package gitlet;

public interface CommandInterface {

    /** Create a new gitlet version-control system */
    void init();

    /** Add copy of file to staging area */
    void add(String fileName);

    /** Saves snapshots of staging area files + files in current commit */


    void commit(String message);
    /** Untrack a file
     *  If file is in staging area, unstage it and keep in working directory
     *  else, remove from working directory
     */
    void rm(String fileName);

    /** Print information of each commit from current to initial commit*/
    void log();

    /** Print information of every commit - ordering does not matter*/
    void globalLog();
    /** Print ids of all commits with given commit message */
    void find(String message);

    /** Displays the following:
     * Existing branches (current branch marked with "*")
     * Staged files
     * Removed files
     * Modified files not staged for commit
     * Untracked files
     */
    void status();


    /** java gitlet.Main checkout[file name] */
    void checkout1(String filName);

    /** java gitlet.Main checkout commitId -- fileName*/
    void checkout2(String commitId, String fileName);
    /** java gitlet.Main checkout [branchName]*/
    void checkout3(String branchName);
    /** Creates new branch with given name
     *  and points branch to current head node
     */
    void branch(String name);

    /** Deletes branch with the given name as well as pointers
     *  Does NOT delete commits associated with branch
     */
    void rmBranch(String bname);

    /** Checks out all files tracked by a given commit and removes
     *  tracked files not present in the given commit
     *  Moves current branch's head to that commit
     */
    void reset(String commitID);

    /** TL;DR merges files from given branch to current branch */
    void merge(String branchName);


}
