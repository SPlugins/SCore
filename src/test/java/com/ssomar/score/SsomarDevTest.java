package com.ssomar.score;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class SsomarDevTest {
    /**
     * Used to stop developers from shipping unintentional bad code.
     * <br/><br/>
     * (The real reason this exists is I might push a test "if (true) return ..." statement
     * to the branch and there's a teeny tiny chance Ssomar might miss it. -Special70)
     * @throws IOException
     */
    @org.junit.jupiter.api.Test
    void noConstantIfTrueAllowed() throws IOException {
        Path src = Paths.get("src/main/java");
        try (Stream<Path> files = Files.walk(src)) {
            files.filter(f -> f.toString().endsWith(".java"))
                    .forEach(f -> {
                        try {
                            String code = new String(Files.readAllBytes(f), StandardCharsets.UTF_8);

                            // Remove block comments /* ... */
                            code = code.replaceAll("(?s)/\\*.*?\\*/", "");

                            // Remove line comments // ...
                            code = code.replaceAll("//.*", "");

                            assertFalse(code.contains("if (true)"),
                                    "Someone forgot to remove their forced if=true statement! Forbidden pattern found in: " + f);
                        } catch (IOException e) {
                            fail(e);
                        }
                    });
        }
    }
}