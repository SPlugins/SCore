package com.ssomar.score.projectiles.loader;

import com.google.common.base.Charsets;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.*;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConfigConverter {


    public static Map<String, String> getWordsToReplace() {

        Map<String, String> replace = new java.util.HashMap<>();
        replace.put("potionSettings", "potionFeatures");
        return replace;
    }

    public static void updateTo(File file) {

        String fileContent = "";
        // Read the file content
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder contentBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                contentBuilder.append(line).append(System.lineSeparator());
            }
            // Remove the last line separator
            if (contentBuilder.length() > 0) {
                contentBuilder.setLength(contentBuilder.length() - System.lineSeparator().length());
            }
            fileContent = contentBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        try {
            Writer writer = new OutputStreamWriter(new FileOutputStream(file), Charsets.UTF_8);

            try {
                for (Map.Entry<String, String> entry : getWordsToReplace().entrySet()) {
                    fileContent = fileContent.replace(entry.getKey(), entry.getValue());
                }
                writer.write(fileContent);
            } finally {
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
