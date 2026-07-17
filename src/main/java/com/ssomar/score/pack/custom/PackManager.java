package com.ssomar.score.pack.custom;

import com.ssomar.score.SCore;
import com.ssomar.score.pack.spigot.InjectSpigot;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PackManager {

    @Getter
    private Map<UUID, PackSettings> packs;

    private static PackManager instance;

    private PackManager() {
        packs = new HashMap<>();
    }

    public void addPack(PackSettings pack) {
        // copy the pack in a cache folder to avoid the pack to be deleted
        File cacheFolder = new File(SCore.dataFolder, "textures-cache");
        if (!cacheFolder.exists()) {
            cacheFolder.mkdirs();
        }
        File actualPackFile = pack.getFile();
        if (!actualPackFile.exists()) return;
        File cachePackFile = new File(cacheFolder, actualPackFile.getName());
        if (cachePackFile.exists()) {
            cachePackFile.delete();
        }

        // copy the file to the cache folder
        try {
            Files.copy(actualPackFile.toPath(), cachePackFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            pack.setFilePath(cachePackFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (pack.isDeleteInitialFile()) {
            actualPackFile.delete();
        }

        // Recompute hash from the cached file
        pack.recomputeHash();

        packs.put(pack.getUuid(), pack);
        InjectSpigot.INSTANCE.registerInjector(pack.getInjector());
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            try {
                player.addResourcePack(pack.getUuid(), pack.getHostedPath(), pack.getHash(), pack.getCustomPromptMessage(), pack.isForce());
            } catch (Exception | Error e) {
                // Version not supported
            }
        }
    }

    public void removePack(UUID uuid) {
        PackSettings pack = packs.get(uuid);
        if (pack != null) {
            packs.remove(uuid);
            InjectSpigot.INSTANCE.unregisterInjector(pack.getInjector());
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                try {
                    player.removeResourcePack(uuid);
                } catch (Exception | Error e) {
                    // Version not supported
                }
            }
        }
    }

    public void removeAllPacks() {
        for (PackSettings pack : packs.values()) {
            InjectSpigot.INSTANCE.unregisterInjector(pack.getInjector());
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                try {
                    player.removeResourcePack(pack.getUuid());
                } catch (Exception | Error e) {
                    // Version not supported
                }
            }
        }
        packs.clear();
    }

    public static PackManager getInstance() {
        if (instance == null) instance = new PackManager();
        return instance;
    }
}
