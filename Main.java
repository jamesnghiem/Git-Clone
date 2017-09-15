package gitlet;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @James
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {

        MainMethods.methodRuns(args);
    }
}
