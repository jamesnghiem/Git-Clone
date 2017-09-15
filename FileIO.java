package gitlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

public class FileIO implements Serializable {

    // Thanks StackOverflow
    public static byte[] serialize(Object obj) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
                oos.writeObject(obj);
            } catch (IOException e) {
                System.out.println("Serialization failed");
                return null;
            }
            return bos.toByteArray();
        } catch (IOException e) {
            System.out.println("Serialization failed");
            return null;
        }
    }

    public static Object deserialize(byte[] bytes) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes)) {
            try (ObjectInputStream ois = new ObjectInputStream(bis)) {
                return ois.readObject();
            } catch (ClassNotFoundException e) {
                System.out.println("Deserialization failed");
                return null;
            }
        } catch (IOException e) {
            System.out.println("Deserialization failed");
            return null;
        }
    }
}
