package gitlet;

import java.io.Serializable;
import java.util.HashMap;

public class Staging implements Serializable {
    // Contains (file name):(content hash) K:V pairs
    // Should be constructed ONCE per git init
    private HashMap<String, String> filesToAdd = new HashMap<>();
    // will contain hashID : actual byte of files
    private HashMap<String, byte[]> filesToCopy = new HashMap<>();
    private HashMap<String, String> filesToRemove = new HashMap<>();

    // Clears all items in the hashMaps after a commit call
    public void clearItems() {
        filesToAdd.clear();
        filesToRemove.clear();
        filesToCopy.clear();
    }

    // Add file to filesToAdd
    public void addToFTA(String name, String contentHash) {
        filesToAdd.put(name, contentHash);
    }

    // Add file to filesToRemove
    public void addToFTR(String name, String contentHash) {
        filesToRemove.put(name, contentHash);
    }

    public void addToFTC(String name, byte[] content) {
        filesToCopy.put(name, content);
    }

    public void removeFromFTA(String name) {
        filesToAdd.remove(name);
    }

    public void removeFromFTC(String name) {
        filesToCopy.remove(name);
    }

    public void removeFromFTR(String name) {
        filesToRemove.remove(name);
    }

    public HashMap<String, String> getFilesToAdd() {
        return filesToAdd;
    }

    public HashMap<String, String> getFilesToRemove() {
        return filesToRemove;
    }

    public HashMap<String, byte[]> getFilesToCopy() {
        return filesToCopy;
    }

}
