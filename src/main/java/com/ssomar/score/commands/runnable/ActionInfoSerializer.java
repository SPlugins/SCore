package com.ssomar.score.commands.runnable;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.*;
import java.util.Base64;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ActionInfoSerializer {

    /**
     * Write the object to a Base64 string.
     */
    public static String toString(Serializable o) throws IOException {;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        oos.close();
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    /**
     * Read the object from Base64 string.
     */
    public static Object fromString(String s) throws IOException, ClassNotFoundException {
        byte[] data = Base64.getDecoder().decode(s);
        ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(data));
        Object o = ois.readObject();
        ois.close();
        return o;
    }

}
