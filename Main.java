package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.ArrayDeque;

import static gitlet.Utils.error;

/** Gitlet Main File
 *  @author Tyler Corliss
 */
public class Main implements Serializable {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */


    // Main takes in the arguments to gitlet.
    @SuppressWarnings("unchecked")
    public static void main(String... args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        }
        // Initialization
        if (args[0].equals("init")) {
            try {
                init();
            } catch (java.io.IOException excep) {
                throw error(excep.getMessage());
            }

        } else if (args[0].equals("add")) {
            add(args[1]);
        } else if (args[0].equals("commit")) {
            setupPersistance();
            if (args[1].equals("")) {
                System.out.println("Please enter a commit message.");
                return;
            }
            hEAD = Utils.readContentsAsString(headBranch);
            branchTosha1 = (Utils.readObject(headSave, TreeMap.class));
            try {
                commit(args[1], branchTosha1.get(hEAD), null);
            } catch (java.io.IOException excep) {
                throw error(excep.getMessage());
            }
        } else if (args[0].equals("checkout")) {
            setupPersistance();
            hEAD = Utils.readContentsAsString(headBranch);
            branchTosha1 = (Utils.readObject(headSave, TreeMap.class));
            File f = Utils.join(commits, branchTosha1.get(hEAD));
            Commit h = Utils.readObject(f, Commit.class);
            if (args[1].equals("--")) {
                checkoutv1(args[2], h);
            } else if (args.length == 2) {
                checkoutv3(args[1]);
            } else if (args[2].equals("--")) {
                checkoutv2(args[1], args[3]);
            } else {
                System.out.println("Incorrect operands.");
                return;
            }
        } else if (args[0].equals("log")) {
            log();
        } else if (args[0].equals("branch")) {
            branch(args[1]);
        } else {
            mainv2(args);
        }
    }
    // Split Main into second section for easier readability of commands list.
    public static void mainv2(String... args) {
        if (args[0].equals("rm")) {
            try {
                rm(args[1]);
            } catch (java.io.IOException excep) {
                throw error(excep.getMessage());
            }
        } else if (args[0].equals("global-log")) {
            globalLog();
        } else if (args[0].equals("rm-branch")) {
            rmBranch(args[1]);
        } else if (args[0].equals("reset")) {
            reset(args[1]);
        } else if (args[0].equals("find")) {
            find(args[1]);
        } else if (args[0].equals("status")) {
            if (!Utils.join
                    (System.getProperty("user.dir"), ".gitlet").exists()) {
                System.out.println("Not in an initialized Gitlet directory.");
                return;
            }
            status();
        } else if (args[0].equals("merge")) {
            merge(args[1]);
        } else {
            System.out.println("No command with that name exists.");
        }
    }


    /** first file. */
    private static File cWD;
    /** current head file. */
    private static String hEAD;
    // return current head file
    public static String getHead() {
        return hEAD;
    }

    /** overall files. */
    private static File dotGitlet;
    /** staged files for addition. */
    private static File stagedAddition;
    /** staged for removal. */
    private static File stagedRemoval;
    /** current commits list. */
    private static File commits;

    public static File comFolder() {
        return commits;
    }

    /** blobs file. */
    private static File blobs;
    /** saved head file. */
    private static File headSave;
    /** map for hash function. */
    private static TreeMap<String, String> branchTosha1 = new TreeMap<>();

    public static TreeMap<String, String> branchTosha() {
        return branchTosha1;
    }

    /** the branch head. */
    private static File headBranch;
    // This saves all important locations and values for the next runs.
    public static void setupPersistance() {
        cWD = new File(System.getProperty("user.dir"));

        dotGitlet = Utils.join(cWD, ".gitlet");

        headSave = Utils.join(dotGitlet, "head");

        stagedAddition = Utils.join(dotGitlet, ".stagedAddition");

        stagedRemoval = Utils.join(dotGitlet, ".stagedRemoval");

        commits = Utils.join(dotGitlet, ".commits");

        blobs = Utils.join(dotGitlet, ".blobs");

        headBranch = Utils.join(dotGitlet, "headBranch");
    }
    // Saves commits to files
    public static void saveCommit(Commit save) throws IOException {
        File thisCommit = Utils.join(commits,
                Utils.sha1((Object) Utils.serialize(save)));
        boolean sdf = thisCommit.createNewFile();
        Utils.writeObject(thisCommit, save);
    }
    // Initializes all files.
    public static void init() throws IOException {
        setupPersistance();
        if (dotGitlet.exists()) {
            System.out.println("A Gitlet version-control "
                    + "system already exists in the current directory.");
            return;
        }
        boolean afd = dotGitlet.mkdir();
        boolean adsfsdff = stagedRemoval.mkdir();
        boolean sewefd = stagedAddition.mkdir();
        boolean sdfsf = commits.mkdir();
        boolean dsfd = blobs.mkdir();
        boolean sdaf = headSave.createNewFile();
        boolean sfd = headBranch.createNewFile();
        Commit initial = new Commit("initial commit", null, null);
        branchTosha1.put("master", Utils.sha1
                ((Object) Utils.serialize(initial)));
        hEAD = "master";
        Utils.writeContents(headBranch, hEAD);
        try {
            Utils.writeObject(headSave, branchTosha1);
            saveCommit(initial);
        } catch (java.io.IOException excep) {
            throw error(excep.getMessage());
        }
    }
    // The git add function.
    @SuppressWarnings("unchecked")
    public static void add(String fileName) {
        setupPersistance();
        boolean exists = false;
        for (File i: Objects.requireNonNull(cWD.listFiles())) {
            if (i.getName().equals(fileName)) {
                exists = true;
            }
        }
        if (!exists) {
            System.out.println("File does not exist.");
            return;
        }
        hEAD = Utils.readContentsAsString(headBranch);
        branchTosha1 = (Utils.readObject(headSave, TreeMap.class));
        File f = Utils.join(commits, branchTosha1.get(hEAD));
        Commit h = Utils.readObject(f, Commit.class);

        byte[] arr = Utils.readContents(Utils.join(cWD, fileName));
        String vals = Utils.sha1((Object) arr);
        if (h.getFileNameToBlob().containsKey(fileName)
                && h.getFileNameToBlob().get(fileName).equals(vals)) {
            boolean did = Utils.join(stagedRemoval, fileName).delete();
        } else {
            File newBlob = Utils.join(stagedAddition, fileName);

            String blog = Utils.readContentsAsString(Utils.join(cWD, fileName));
            Utils.writeContents(newBlob, blog);
        }
    }
    // The commit function
    @SuppressWarnings({"unchecked"})
    public static void commit(String msg, String par,
                              String mergeParent) throws IOException {
        setupPersistance();
        hEAD = Utils.readContentsAsString(headBranch);
        branchTosha1 = (Utils.readObject(headSave, TreeMap.class));
        File f = Utils.join(commits, branchTosha1.get(hEAD));
        Commit h = Utils.readObject(f, Commit.class);
        Commit next = new Commit(msg, par, mergeParent);
        if (Objects.requireNonNull
                (Utils.plainFilenamesIn(stagedAddition)).isEmpty()
                && Objects.requireNonNull
                (Utils.plainFilenamesIn(stagedRemoval)).isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        }

        for (File i: Objects.requireNonNull(stagedAddition.listFiles())) {
            String name = i.getName();
            String value = Utils.sha1((Object) Utils.readContents(i));
            byte[] contentsOfFile = Utils.readContents
                    (Utils.join(stagedAddition, name));
            File a = Utils.join(blobs, value);
            boolean worked = a.createNewFile();
            next.putName(name, value);
            Utils.writeContents(a, (Object) contentsOfFile);
            boolean r = Utils.join(stagedAddition, name).delete();
        }

        for (File i: Objects.requireNonNull(stagedRemoval.listFiles())) {
            next.removeName(i.getName());
            boolean c = i.delete();
        }

        branchTosha1.put(hEAD, Utils.sha1((Object)
                Utils.serialize(next)));
        Utils.writeObject(headSave, branchTosha1);
        saveCommit(next);
    }
    // These two checkout functions all base off of the regular checkout, but are for if a checkout is filename or ID
    public static void checkoutv1(String fileName, Commit com) {
        setupPersistance();
        hEAD = Utils.readContentsAsString(headBranch);

        if (!com.filenametoblob().containsKey(fileName)) {
            System.out.println("File does not exist in that commit");
            return;
        }

        String blob = com.filenametoblob().get(fileName);
        String contents = Utils.readContentsAsString(Utils.join(blobs, blob));
        Utils.writeContents(Utils.join(cWD, fileName), contents);
    }
    public static void checkoutv2(String commitID, String fileName) {
        setupPersistance();
        hEAD = Utils.readContentsAsString(headBranch);


        boolean exists = false;
        for (File i: Objects.requireNonNull(commits.listFiles())) {
            if (i.getName().equals(commitID)) {
                exists = true;
            }
        }
        if (!exists) {
            System.out.println("No commit with that id exists.");
            return;
        }

        Commit current = Utils.readObject
                (Utils.join(commits, commitID), Commit.class);
        checkoutv1(fileName, current);
    }

    //Untracked file check
    @SuppressWarnings("unchecked")
    public static void untrackedHelper(Commit com) {
        setupPersistance();
        hEAD = Utils.readContentsAsString(headBranch);
        branchTosha1 = (Utils.readObject(headSave, TreeMap.class));
        File f = Utils.join(commits, branchTosha1.get(hEAD));
        Commit head = Utils.readObject(f, Commit.class);

        for (String i: com.filenametoblob().keySet()) {
            if (!head.filenametoblob().containsKey(i)
                    && Utils.join(cWD, i).exists()) {
                System.out.println("There is an untracked file in the way;"
                        + " delete it, or add and commit it first.");
            }
        }
    }
    //third checkout, this one changes branch to the branch that you wish to checkout.
    @SuppressWarnings("unchecked")
    public static void checkoutv3(String branch) {
        setupPersistance();
        hEAD = Utils.readContentsAsString(headBranch);

        branchTosha1 = (Utils.readObject(headSave, TreeMap.class));
        File f = Utils.join(commits, branchTosha1.get(hEAD));
        Commit cur = Utils.readObject(f, Commit.class);



        if (branch.equals(hEAD)) {
            System.out.println("No need to checkout the current branch.");
            return;
        }

        branchTosha1 = (Utils.readObject(headSave, TreeMap.class));

        if (!branchTosha1.containsKey(branch)) {
            System.out.println("No such branch exists.");
            return;
        }

        File x = Utils.join(commits, branchTosha1.get(branch));
        Commit next = Utils.readObject(x, Commit.class);
        untrackedHelper(next);

        for (String j: next.filenametoblob().keySet()) {
            File l = Utils.join(cWD, j);
            byte[] vals = Utils.readContents(Utils.join
                    (blobs, next.filenametoblob().get(j)));
            Utils.writeContents(l, (Object) vals);
        }

        for (String b: cur.filenametoblob().keySet()) {
            if (!next.filenametoblob().containsKey(b)) {
                Utils.restrictedDelete(Utils.join(cWD, b));
            }
        }

        for (File s: Objects.requireNonNull(stagedAddition.listFiles())) {
            boolean k = s.delete();
        }

        for (File n: Objects.requireNonNull(stagedRemoval.listFiles())) {
            boolean d = n.delete();
        }

        hEAD = branch;
        Utils.writeContents(headBranch, hEAD);
    }

    // shows a log of everything important within the program for testing.
    @SuppressWarnings("unchecked")
    public static void log() {
        setupPersistance();
        hEAD = Utils.readContentsAsString(headBranch);

        branchTosha1 = (Utils.readObject(headSave, TreeMap.class));
        File f = Utils.join(commits, branchTosha1.get(hEAD));
        Commit h = Utils.readObject(f, Commit.class);
        Commit current = h;
        while (current.getParent(0) != null) {
            System.out.println("===");
            System.out.println("commit "
                    + Utils.sha1((Object)
                    Utils.serialize(current)));
            System.out.println("Date: " + current.getTimestamp());
            System.out.println(current.getMessage());
            System.out.println();
            current = Utils.readObject
                    (Utils.join(commits, current.getPar()), Commit.class);
        }
        System.out.println("===");
        System.out.println("commit " + Utils.sha1((Object)
                Utils.serialize(current)));
        System.out.println("Date: " + current.getTimestamp());
        System.out.println(current.getMessage());
        System.out.println();
    }
    public static void globalLog() {
        setupPersistance();
        hEAD = Utils.readContentsAsString(headBranch);
        for (String x: Objects.requireNonNull
                (Utils.plainFilenamesIn(commits))) {
            Commit a = Utils.readObject(Utils.join(commits, x), Commit.class);
            System.out.println("===");
            System.out.println("commit " + x);
            System.out.println("Date: " + a.getTimestamp());
            System.out.println(a.getMessage());
            System.out.println();
        }
    }
    // removes the branch that is inputted.
    @SuppressWarnings("unchecked")
    public static void rmBranch(String branch) {
        setupPersistance();
        hEAD = Utils.readContentsAsString(headBranch);
        if (hEAD.equals(branch)) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        branchTosha1 = (Utils.readObject(headSave, TreeMap.class));
        String x = branchTosha1.remove(branch);
        if (x == null) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        Utils.writeObject(headSave, branchTosha1);
    }
    // resets the commit with given commitID
    @SuppressWarnings("unchecked")
    public static void reset(String commitID) {
        setupPersistance();
        hEAD = Utils.readContentsAsString(headBranch);
        branchTosha1 = (Utils.readObject(headSave, TreeMap.class));
        File b = Utils.join(commits, branchTosha1.get(hEAD));
        boolean boo = Utils.join(commits, commitID).exists();
        if (!boo) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Commit h = Utils.readObject(b, Commit.class);
        File f = Utils.join(commits, commitID);
        Commit next = Utils.readObject(f, Commit.class);
        untrackedHelper(next);

        for (String j: next.filenametoblob().keySet()) {
            File l = Utils.join(cWD, j);
            byte[] vals = Utils.readContents
                    (Utils.join(blobs, next.filenametoblob().get(j)));
            Utils.writeContents(l, (Object) vals);
        }

        for (String x: h.filenametoblob().keySet()) {
            if (!next.filenametoblob().containsKey(x)) {
                Utils.restrictedDelete(Utils.join(cWD, x));
            }
        }

        for (File s: Objects.requireNonNull(stagedAddition.listFiles())) {
            boolean l = s.delete();
        }

        for (File n: Objects.requireNonNull(stagedRemoval.listFiles())) {
            boolean u = n.delete();
        }
        branchTosha1.put(hEAD, commitID);
        Utils.writeObject(headSave, branchTosha1);
    }
    // removes a file and stages for removal
    @SuppressWarnings("unchecked")
    public static void rm(String fileName) throws IOException {
        setupPersistance();
        hEAD = Utils.readContentsAsString(headBranch);

        branchTosha1 = (Utils.readObject(headSave, TreeMap.class));
        File f = Utils.join(commits, branchTosha1.get(hEAD));
        Commit h = Utils.readObject(f, Commit.class);
        boolean did = false;

        File val = Utils.join(stagedAddition, fileName);

        if (val.exists()) {
            boolean p = val.delete();
            did = true;
        } else if (h.filenametoblob().containsKey(fileName)) {
            File x = Utils.join(cWD, fileName);
            boolean b = x.delete();
            File a = Utils.join(stagedRemoval, fileName);
            Utils.writeContents(a, "1");
            did = true;
        }
        if (!did) {
            System.out.println("No reason to remove the file.");
        }
    }

    //creates a new branch
    @SuppressWarnings("unchecked")
    public static void branch(String name) {
        setupPersistance();
        hEAD = Utils.readContentsAsString(headBranch);

        branchTosha1 = (Utils.readObject(headSave, TreeMap.class));
        for (String i: branchTosha1.keySet()) {
            if (i.equals(name)) {
                System.out.println("A branch with that name already exists.");
                return;
            }
        }
        branchTosha1.put(name, branchTosha1.get(hEAD));
        Utils.writeObject(headSave, branchTosha1);
    }
    // this is to find a certain commit with the given message.
    public static void find(String msg) {
        setupPersistance();
        hEAD = Utils.readContentsAsString(headBranch);

        boolean found = false;

        for (File i: Objects.requireNonNull(commits.listFiles())) {
            Commit x = Utils.readObject(i, Commit.class);
            if (x.getMessage().equals(msg)) {
                System.out.println(i.getName());
                found = true;
            }
        }
        if (!found) {
            System.out.println("Found no commit with that message.");
        }
    }
    // similar to log
    @SuppressWarnings("unchecked")
    public static void status() {
        setupPersistance();
        hEAD = Utils.readContentsAsString(headBranch);
        branchTosha1 = (Utils.readObject(headSave, TreeMap.class));
        File f = Utils.join(commits, branchTosha1.get(hEAD));
        Commit h = Utils.readObject(f, Commit.class);

        System.out.println("=== Branches ===");
        for (String i: branchTosha1.keySet()) {
            if (i.equals(hEAD)) {
                System.out.println("*" + hEAD);
            } else {
                System.out.println(i);
            }
        }
        System.out.println();

        System.out.println("=== Staged Files ===");
        if (stagedAddition.listFiles() != null) {
            for (File j: Objects.requireNonNull(stagedAddition.listFiles())) {
                System.out.println(j.getName());
            }
        }
        System.out.println();

        System.out.println("=== Removed Files ===");
        if (stagedRemoval.listFiles() != null) {
            for (File k: Objects.requireNonNull(stagedRemoval.listFiles())) {
                System.out.println(k.getName());
            }
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
        System.out.println();
    }

    public static boolean isEqual(String com1, String com2) {
        if (com1 == null && com2 == null) {
            return true;
        }
        if (com1 == null) {
            return false;
        }
        return com1.equals(com2);
    }

    // Merges two branches given by the user
    @SuppressWarnings("unchecked")
    public static void merge(String branch) {
        setupPersistance();
        hEAD = Utils.readContentsAsString(headBranch);
        branchTosha1 = (Utils.readObject(headSave, TreeMap.class));
        File f = Utils.join(commits, branchTosha1.get(hEAD));
        String headName = branchTosha1.get(hEAD);
        Commit h = Utils.readObject(f, Commit.class);
        if (!Utils.plainFilenamesIn(stagedAddition).isEmpty()) {
            System.out.println("You have uncommitted changes.");
            return;
        }
        if (!branchTosha1.containsKey(branch)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (hEAD.equals(branch)) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }
        File b = Utils.join(commits, branchTosha1.get(branch));
        String branchName = branchTosha1.get(branch);
        Commit given = Utils.readObject(b, Commit.class);
        untrackedHelper(given);
        String x = splitPoint(headName, branchName);
        File o = Utils.join(commits, x);
        Commit split = Utils.readObject(o, Commit.class);
        boolean flag = false;
        for (String i: given.filenametoblob().keySet()) {
            String j = given.filenametoblob().get(i);
            if (h.filenametoblob().get(i) == null
                    && !isEqual(j, split.filenametoblob().get(i))) {
                if (split.filenametoblob().get(i) == null) {
                    checkoutv2(branchTosha1.get(branch), i);
                    add(i);
                } else {
                    flag = true;
                    String write = "<<<<<<< HEAD\n";
                    write += "=======\n";
                    write += Utils.readContentsAsString(Utils.join(blobs, j));
                    write += ">>>>>>>\n";
                    Utils.writeContents(Utils.join(cWD, i), write);
                    add(i);
                }
            }
        }

        mergev2(h, split, given, branch, flag);
    }

    public static void mergev2(Commit h, Commit split, Commit given,
                               String branch, Boolean flag) {
        for (String headFileName: h.filenametoblob().keySet()) {
            String e = h.filenametoblob().get(headFileName);
            if (isEqual(e, split.filenametoblob().get(headFileName))) {
                if (!isEqual(e, given.filenametoblob().get(headFileName))) {
                    if (given.filenametoblob().get(headFileName) != null) {
                        checkoutv2(branchTosha1.get(branch), headFileName);
                        add(headFileName);
                    } else {
                        try {
                            rm(headFileName);
                        } catch (java.io.IOException excep) {
                            throw error(excep.getMessage());
                        }
                    }
                }
            } else if (!isEqual(e, given.filenametoblob().get(headFileName))
                    && !isEqual(given.filenametoblob().get(headFileName),
                    split.filenametoblob().get(headFileName))) {
                flag = true;
                String write2 = "<<<<<<< HEAD\n";
                write2 += Utils.readContentsAsString(Utils.join(blobs, e));
                write2 += "=======\n";
                if (given.filenametoblob().get(headFileName) != null) {
                    write2 += Utils.readContentsAsString
                            (Utils.join(blobs,
                                    given.filenametoblob().get(headFileName)));
                }
                write2 += ">>>>>>>\n";
                Utils.writeContents(Utils.join(cWD, headFileName), write2);
                add(headFileName);
            }
        }
        if (flag) {
            System.out.println("Encountered a merge conflict.");
        }
        try {
            commit("Merged " + branch + " into " + hEAD + ".",
                    branchTosha1.get(hEAD), branchTosha1.get(branch));
        } catch (java.io.IOException excep) {
            throw error(excep.getMessage());
        }
    }

    public static String splitPoint(String head, String given) {
        Set<String> givenSet = new HashSet<>();
        ArrayDeque<String> work = new ArrayDeque<>();
        work.push(given);
        while (!work.isEmpty()) {
            String node = work.remove();
            givenSet.add(node);
            for (int i = 0; i < 2; i += 1) {
                Commit nodeToCommit = Utils.readObject
                        (Utils.join(commits, node), Commit.class);
                if (nodeToCommit.getParent(i) != null) {
                    work.push(nodeToCommit.getParent(i));
                }
            }
        }

        ArrayDeque<String> workh = new ArrayDeque<>();
        workh.push(head);
        while (!workh.isEmpty()) {
            String nodeh = workh.remove();
            if (givenSet.contains(nodeh)) {
                if (nodeh.equals(given)) {
                    System.out.println("Given branch"
                            + " is an ancestor of the current branch.");
                    System.exit(0);
                } else if (nodeh.equals(head)) {
                    System.out.println("Current branch fast-forwarded.");
                }
                return nodeh;
            }
            for (int i = 0; i < 2; i += 1) {
                Commit nodeToCommith = Utils.readObject
                        (Utils.join(commits, nodeh), Commit.class);
                if (nodeToCommith.getParent(i) != null) {
                    workh.push(nodeToCommith.getParent(i));
                }
            }
        }
        return "";
    }
}
