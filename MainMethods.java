package gitlet;

/**
 * Created by James on 7/17/2017.
 */
public class MainMethods {
    public static void methodRuns(String... args) {
        Repo gitlet = new Repo();
        int numArgs = args.length;
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        }
        if (args[0].equals("init")) {
            if (numArgs != 1) {
                System.out.println("Incorrect operands.");
                return;
            }
            gitlet.init();
        } else if (args[0].equals("add")) {
            if (numArgs == 1) {
                System.out.println("Incorrect operands.");
                return;
            }
            for (int i = 1; i < numArgs; i++) {
                gitlet.add(args[i]);
            }
        } else if (args[0].equals("commit")) {
            if (numArgs != 2) {
                System.out.println("Incorrect operands.");
                return;
            }
            gitlet.commit(args[1]);
        } else if (args[0].equals("rm")) {
            if (numArgs == 1) {
                System.out.println("Incorrect operands.");
                return;
            }
            for (int i = 1; i < numArgs; i++) {
                gitlet.rm(args[i]);
            }
        } else if (args[0].equals("log")) {
            if (numArgs != 1) {
                System.out.println("Incorrect operands.");
                return;
            }
            gitlet.log();
        } else if (args[0].equals("global-log")) {
            if (numArgs != 1) {
                System.out.println("Incorrect operands.");
                return;
            }
            gitlet.globalLog();
        } else if (args[0].equals("find")) {
            if (numArgs != 2) {
                System.out.println("Incorrect operands.");
                return;
            }
            gitlet.find(args[1]);
        } else {
            methodRun2(args);
        }
    }

    public static void methodRun2(String... args) {
        Repo gitlet = new Repo();
        int numArgs = args.length;

        if (args[0].equals("status")) {
            if (numArgs != 1) {
                System.out.println("Incorrect operands.");
                return;
            }
            gitlet.status();
        } else if (args[0].equals("checkout")) {
            if (args.length > 4 || args.length < 2) {
                System.out.println("Incorrect operands.");
                return;
            }
            if (args.length == 2) {
                gitlet.checkout3(args[1]);
            } else if (args.length == 3) {
                if (!args[1].equals("--")) {
                    System.out.println("Incorrect operands.");
                    return;
                }
                gitlet.checkout1(args[2]);
            } else if (args.length == 4) {
                if (!args[2].equals("--")) {
                    System.out.println("Incorrect operands.");
                    return;
                }
                gitlet.checkout2(args[1], args[3]);
            }
        } else if (args[0].equals("branch")) {
            if (numArgs != 2) {
                System.out.println("Incorrect operands.");
                return;
            }
            gitlet.branch(args[1]);
        } else if (args[0].equals("rm-branch")) {
            if (numArgs != 2) {
                System.out.println("Incorrect operands.");
                return;
            }
            gitlet.rmBranch(args[1]);
        } else if (args[0].equals("reset")) {
            if (numArgs != 2) {
                System.out.println("Incorrect operands.");
                return;
            }
            gitlet.reset(args[1]);
        } else if (args[0].equals("merge")) {
            if (numArgs != 2) {
                System.out.println("Incorrect operands.");
                return;
            }
            gitlet.merge(args[1]);
        } else {
            System.out.println("No command with that name exists.");
            return;
        }
    }
}
