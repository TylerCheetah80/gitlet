package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.Formatter;
import java.util.Map;
import java.util.TreeMap;


public class Commit implements Serializable {
    /** stuff. */
    private String message;
    /** stuff. */
    private String timestamp;
    /** stuff. */
    private String[] blobs;
    /** stuff. */
    private String parent;

    public String getPar() {
        return parent;
    }

    /** stuff. */
    private String mergeParent;
    /** stuff. */
    private TreeMap<String, String> fileNameToBlob = new TreeMap<>();

    public TreeMap<String, String> filenametoblob() {
        return fileNameToBlob;
    }

    public Commit(String msg, String par, String mergeparent) {
        this.message = msg;
        this.parent = par;
        this.mergeParent = mergeparent;

        if (this.parent == null) {
            this.timestamp = "Wed Dec 31 16:00:00 1969 -0800";
        } else {
            Date current = new Date();
            Formatter b = new Formatter();
            b.format("%ta %<tb %<te %<tT %<tY %<tz", current);
            String c = b.toString();
            this.timestamp = c;
        }


        if (par != null) {
            File f = Utils.join(Main.comFolder(),
                    Main.branchTosha().get(Main.getHead()));
            Commit h = Utils.readObject(f, Commit.class);

            fileNameToBlob.putAll(h.fileNameToBlob);
        }
    }

    public String getMessage() {
        return this.message;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public String getParent(int val) {
        if (val == 0) {
            return this.parent;
        } else {
            return this.mergeParent;
        }
    }

    public Map<String, String> getFileNameToBlob() {
        return fileNameToBlob;
    }

    public void putName(String name, String hashcode) {
        fileNameToBlob.put(name, hashcode);
    }

    public void removeName(String name) {
        fileNameToBlob.remove(name);
    }
}
