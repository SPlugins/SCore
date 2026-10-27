package com.ssomar.score.languages.messages;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CompileEnum {

    public static void main(String[] args) throws FileNotFoundException {
        File file = new File("src/main/resources/languages/language_en.yml");
        InputStream inputStream = new FileInputStream(file);
        Yaml yaml = new Yaml();
        Map<String, Object> data = yaml.load(inputStream);
        read(data, "");
    }

    public static void read(Map<String, Object> map, String key){
        for (String str: map.keySet()) {
           if(map.get(str) instanceof Map){
               read((Map<String, Object>) map.get(str), key + "." + str);
           }
           else{
               String value = key + "." + str;
                value = value.substring(1, value.length());
               String enumName = value.replace(".", "_").toUpperCase();
               //System.out.println(enumName + "(\""+value+"\"),");
           }
        }
    }
}
