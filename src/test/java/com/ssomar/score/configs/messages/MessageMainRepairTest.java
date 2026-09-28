package com.ssomar.score.configs.messages;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MessageMainRepairTest {

    @Test
    void doubledQuoteIsRepairedAndTheRestIsKept(@TempDir Path dir) throws Exception {
        File file = dir.resolve("Locale_EN.yml").toFile();
        String broken = "custom: \"&amy own text\"\nerrorUPDItem: \"&cYou\"\"ve reached max uses for &e%item%\"\n";
        Files.write(file.toPath(), broken.getBytes(StandardCharsets.UTF_8));
        assertThrows(InvalidConfigurationException.class, () -> new YamlConfiguration().loadFromString(broken), "the shipped file does not parse");

        MessageMain.repairDoubledQuotes(file);

        YamlConfiguration repaired = YamlConfiguration.loadConfiguration(file);
        assertEquals("&cYou've reached max uses for &e%item%", repaired.getString("errorUPDItem"));
        assertEquals("&amy own text", repaired.getString("custom"));
    }

    @Test
    void validFileIsNotTouched(@TempDir Path dir) throws Exception {
        File file = dir.resolve("Locale_FR.yml").toFile();
        Files.write(file.toPath(), "a: \"b\"\n".getBytes(StandardCharsets.UTF_8));
        long before = file.lastModified();
        MessageMain.repairDoubledQuotes(file);
        assertEquals(before, file.lastModified());
        assertEquals("a: \"b\"\n", new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8));
    }
}
