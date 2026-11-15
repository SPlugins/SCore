package com.ssomar.score.sobject;

import com.google.common.io.ByteStreams;
import com.ssomar.score.SCore;
import com.ssomar.score.splugin.SPlugin;
import com.ssomar.score.utils.logging.Utils;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

public abstract class SObjectWithFileLoader<T extends SObjectWithFile> {

    private final SPlugin sPlugin;
    private final String defaultObjectsPath;
    private final String defaultObjectsPathWithoutSlash;
    private final SObjectManager<T> sObjectManager;
    @Getter
    private final int maxFreeObjects;
    private final Logger logger;
    @Getter
    private Map<String, String> randomIdsDefaultObjects;

    @Getter
    private int cpt;

    private String objectName;

    public SObjectWithFileLoader(SPlugin sPlugin, String defaultObjectsPath, SObjectWithFileManager<T> sObjectManager, int maxFreeObjects) {
        this.sPlugin = sPlugin;
        this.logger = sPlugin.getPlugin().getServer().getLogger();
        this.defaultObjectsPath = defaultObjectsPath;
        if (defaultObjectsPath.startsWith("/")) this.defaultObjectsPathWithoutSlash = defaultObjectsPath.substring(1);
        else this.defaultObjectsPathWithoutSlash = defaultObjectsPath;
        this.sObjectManager = sObjectManager;
        sObjectManager.setFileLoader(this);
        this.maxFreeObjects = maxFreeObjects;
        this.cpt = 0;

        objectName = sPlugin.getObjectName();
        /* For plugins that have multiple object splugin.getOjectName can't work */
        if (objectName == null) objectName = sObjectManager.getObjectName();
        objectName = objectName.toLowerCase();
    }

    public static File searchFileOfObjectInFolder(String id, File folder) {
        List<String> listFiles = Arrays.asList(folder.list());
        Collections.sort(listFiles);

        for (String s : listFiles) {
            File fileEntry = new File(folder + "/" + s);
            if (fileEntry.isDirectory()) {
                File result = null;
                if ((result = searchFileOfObjectInFolder(id, fileEntry)) == null)
                    continue;
                else
                    return result;
            } else {
                if (!fileEntry.getName().contains(".yml") || fileEntry.getName().contains(".txt"))
                    continue;
                if (fileEntry.getName().equals(".yml")) continue;

                String currentId = fileEntry.getName().split(".yml")[0];
                if (id.equals(currentId))
                    return fileEntry;
            }
        }
        return null;
    }

    public abstract void load();

    /* public abstract Map<String, List<String>> getPremiumDefaultObjectsName();

    public abstract Map<String, List<String>> getFreeDefaultObjectsName(); */

    /* public Map<String, List<String>> getAllDefaultObjectsName() {
        Map<String, List<String>> defaultObjects = getFreeDefaultObjectsName();
        Map<String, List<String>> premDefaultObjects = getPremiumDefaultObjectsName();
        for (String key : defaultObjects.keySet()) {
            List<String> mergeList = defaultObjects.get(key);
            mergeList.addAll(premDefaultObjects.get(key));
            defaultObjects.put(key, mergeList);
        }
        return defaultObjects;
    }*/

    public void createDefaultObjectsFile(Boolean isPremiumLoading) {
        createDefaultObjectsFile(isPremiumLoading, false);
    }

    /* public void createDefaultObjectsFile(Boolean isPremiumLoading, boolean exists) {

        if (!exists)
            logger.severe(sPlugin.getNameDesign() + " CANT LOAD YOUR " + objectName.toUpperCase() + ", FOLDER '" + objectName + "' not found !");
        logger.severe(sPlugin.getNameDesign() + " DEFAULT " + objectName.toUpperCase() + " CREATED !");

        Map<String, List<String>> defaultObjects;
        if (!isPremiumLoading) defaultObjects = this.getFreeDefaultObjectsName();
        else defaultObjects = this.getAllDefaultObjectsName();


        for (String folder : defaultObjects.keySet()) {

            File fileFolder = new File(getConfigsPath() + "/" + folder);

            fileFolder.mkdirs();

            for (String id : defaultObjects.get(folder)) {
                try {
                    File pdfile = new File(getConfigsPath() + "/" + folder + "/" + id + ".yml");
                    InputStream in = this.getClass().getResourceAsStream(defaultObjectsPath + folder + "/" + id + ".yml");

                    if (!pdfile.exists()) {
                        sPlugin.getPlugin().getDataFolder().mkdirs();
                        pdfile.getParentFile().mkdirs();
                        pdfile.createNewFile();
                    } else
                        continue;

                    OutputStream out = new FileOutputStream(pdfile);
                    byte[] buffer = new byte[1024];
                    int current = 0;

                    while ((current = in.read(buffer)) > -1)
                        out.write(buffer, 0, current);

                    out.close();
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }*/

    public List<String> getObjectsShortPath() {
        //CodeSource src = sPlugin.getClass().getProtectionDomain().getCodeSource();
        List<String> list = new ArrayList<String>();

        /*  OLD METHOD THAT DIESNT WORK SOMETIMES FOR SOME REASON , ISK WHY */
        /* if (defaultObjectsPathWithoutSlash.isEmpty()) {
            Utils.sendConsoleMsg(sPlugin.getNameDesign() + " &cNo default &6" + objectName.toUpperCase() + "&c found in &6" + defaultObjectsPathWithoutSlash);
            return list;
        }
         try {
            if (src != null) {
                URL jar = src.getLocation();
                ZipInputStream zip = new ZipInputStream(jar.openStream());
                ZipEntry ze = null;
                while ((ze = zip.getNextEntry()) != null) {

                    String entryName = ze.getName();
                    if (entryName.endsWith(".yml") && entryName.contains(defaultObjectsPathWithoutSlash)) {
                        //SsomarDev.testMsg("entryName: " + entryName, true);
                        String value = entryName.replace(defaultObjectsPathWithoutSlash, "");
                        list.add(value);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return list;
        }*/
        if (defaultObjectsPath.isEmpty()) {
            Utils.sendConsoleMsg(sPlugin.getNameDesign() + " &cNo default &6" + objectName.toUpperCase() + "&c found in &6" + defaultObjectsPath);
            return list;
        }
        try {
            String defaultObjectsPathWithoutFirstSlash = defaultObjectsPath.startsWith("/") ? defaultObjectsPath.substring(1) : defaultObjectsPath;
            Enumeration<URL> resources = sPlugin.getClass().getClassLoader().getResources(defaultObjectsPathWithoutFirstSlash);
            List<URL> urls = Collections.list(resources);

            // Add check for empty urls list
            if (urls.isEmpty()) {
                Utils.sendConsoleMsg(sPlugin.getNameDesign() + " &cNo resources found at path: &6" + defaultObjectsPathWithoutFirstSlash);
                return list;
            }

            URL resource = urls.get(0);
            if (resource.getProtocol().equals("jar")) {
                String path = resource.getPath();
                String jarPath = path.substring(5, path.indexOf("!"));
                String decodedPath = URLDecoder.decode(jarPath, "UTF-8");
                File file = new File(decodedPath);
                try (JarFile jarFile = new JarFile(file)) {
                    Enumeration<JarEntry> entries = jarFile.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        if (entry.getName().startsWith(defaultObjectsPathWithoutFirstSlash) && !entry.isDirectory()) {
                            //System.out.println(entry.getName());
                            list.add(entry.getName().replace(defaultObjectsPathWithoutFirstSlash, ""));
                        }
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        return list;
    }

    public void createDefaultObjectsFile(Boolean isPremiumLoading, boolean exists) {

        if (!exists) {
            Utils.sendConsoleMsg(sPlugin.getNameDesign() + " &cCANT LOAD YOUR &6" + objectName.toUpperCase() + "&c, FOLDER '" + objectName + "' not found !");
            Utils.sendConsoleMsg(sPlugin.getNameDesign() + " &7It will generate &e" + getObjectsShortPath().size() + "&7 default " + objectName + " from jar :&e " + defaultObjectsPath);
            File fileFolder = new File(getConfigsPath());
            fileFolder.mkdirs();
        }

        for (String id : getObjectsShortPath()) {
            copyDefaultFile(defaultObjectsPath + id, defaultObjectsPathWithoutSlash, isPremiumLoading);
        }


        Utils.sendConsoleMsg(sPlugin.getNameDesign() + " &7DEFAULT &6" + objectName.toUpperCase() + "&7 CREATED !");

    }

    public void copyDefaultFile(String pathFile, String defaultPath, Boolean isPremiumLoading) {

        String value = pathFile.replace(defaultPath, "");
        // get id after the last /
        String id = value.substring(value.lastIndexOf("/") + 1);
        if (!isPremiumLoading && !id.startsWith("Free")) return;

        // _v1_8__1_20_1  accepted between 1.8 and 1.20.1
        if (id.contains("_v1_")) {
            String version = id.split("_v1_")[1];
            String minVersion = "1_" + version.split("__")[0];
            String maxVersion = "";
            if (version.contains("__")) maxVersion = version.split("__")[1];

            if (!SCore.isVersionBetween(minVersion, maxVersion)) return;
        }

        try {
            File pdfile = new File(getConfigsPath() + "/" + value);
            InputStream in = this.getClass().getResourceAsStream(pathFile);

            if (!pdfile.exists()) {
                sPlugin.getPlugin().getDataFolder().mkdirs();
                pdfile.getParentFile().mkdirs();
                pdfile.createNewFile();
            } else return;
            OutputStream out = new FileOutputStream(pdfile);
            byte[] buffer = new byte[1024];
            int current = 0;

            while ((current = in.read(buffer)) > -1)
                out.write(buffer, 0, current);

            Utils.sendConsoleMsg(sPlugin.getNameDesign() + " &6" + value + " &7was created !");

            out.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void loadDefaultPremiumObjects() {

        /* SET RANDOM ID TO NOT INTERFER WITH OTHER EI and to make it, One time session (will not work after a restart) because only for test*/
        randomIdsDefaultObjects = new HashMap<>();
        for (String value : getObjectsShortPath()) {
            String id = value.substring(value.lastIndexOf("/") + 1).replace(".yml", "");
            if (id.startsWith("Free")) continue;
            randomIdsDefaultObjects.put(id, UUID.randomUUID().toString());

            InputStream in = this.getClass().getResourceAsStream(defaultObjectsPath + value);
            BufferedReader reader = null;
            reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            Optional<T> oOpt;
            if (!(oOpt = this.getObjectByReader(reader, id, false, randomIdsDefaultObjects)).isPresent()) continue;

            T o = oOpt.get();
            o.setId(randomIdsDefaultObjects.get(o.getId()));
            sObjectManager.addDefaultLoadedObject(o);
        }
    }

    public void loadDefaultEncodedPremiumObjects(Map<String, List<String>> defaultObjectsName) {

        /* SET RANDOM ID TO NOT INTERFER WITH OTHER EI and to make it, One time session (will not work after a restart) because only for test*/
        if (randomIdsDefaultObjects == null) randomIdsDefaultObjects = new HashMap<>();
        for (String folder : defaultObjectsName.keySet()) {
            for (String id : defaultObjectsName.get(folder)) {
                randomIdsDefaultObjects.put(id, UUID.randomUUID().toString());
            }
        }

        for (String folder : defaultObjectsName.keySet()) {
            for (String id : defaultObjectsName.get(folder)) {

                InputStream in = this.getClass().getResourceAsStream(defaultObjectsPath + folder + "/" + id + ".pack");
                InputStream decodedIn = null;
                int length = -1;
                try {
                    length = in.available();
                    byte[] fileBytes = new byte[length];
                    in.read(fileBytes, 0, fileBytes.length);
                    in.close();
                    byte[] decoded = Base64.getDecoder().decode(fileBytes);
                    decodedIn = new ByteArrayInputStream(decoded);

                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }

                BufferedReader reader = null;
                reader = new BufferedReader(new InputStreamReader(decodedIn, StandardCharsets.UTF_8));
                Optional<T> oOpt;
                if (!(oOpt = this.getObjectByReader(reader, id, false, randomIdsDefaultObjects)).isPresent()) continue;

                T o = oOpt.get();
                o.setId(randomIdsDefaultObjects.get(o.getId()));
                sObjectManager.addDefaultLoadedObject(o);
            }
        }
    }

    public void loadObjectByFile(String filePath, boolean isPremiumLoading) {
        try {
            File fileEntry = new File(filePath);
            String fileName = fileEntry.getName();
            if (!fileName.contains(".yml") || fileName.contains(".txt") || fileName.equals(".yml")) return;
            String id = fileName.split(".yml")[0];

            if (!isPremiumLoading && cpt >= maxFreeObjects) {
                Utils.sendConsoleMsg(sPlugin.getNameDesign() + " &cERROR, REQUIRE PREMIUM: to add more than " + maxFreeObjects + " " + objectName + " you need the premium version");
                return;
            }

            Optional<T> oOpt;
            if (!(oOpt = this.getObjectByFile(fileEntry, id, true)).isPresent()) {
                Utils.sendConsoleMsg(sPlugin.getNameDesign() + " &cERROR, the file " + filePath + " can't be loaded !");
                return;
            }
            sObjectManager.addLoadedObject(oOpt.get());
            cpt++;
            //Utils.sendConsoleMsg(sPlugin.getNameDesign() + " &e" + id + " &7was loaded !");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadObjectsInFolder(File folder, boolean isPremiumLoading) {
        List<String> listFiles = Arrays.asList(folder.list());
        Collections.sort(listFiles);

        for (String s : listFiles) {
            String folderPath = folder.getPath();
            File fileEntry = new File(folderPath + "/" + s);
            if (fileEntry.isDirectory()) {
                loadObjectsInFolder(fileEntry, isPremiumLoading);
            } else {
                /*
                Runnable runnable3 = new Runnable() {
                    @Override
                    public void run() {
                        loadObjectByFile(folder.getPath() + "/" + s, isPremiumLoading);
                    }
                };
                runnable3.runTaskLater(SCore.plugin, cpt* 10L);*/
                loadObjectByFile(folderPath + "/" + s, isPremiumLoading);
            }
        }
    }

    public List<String> getAllFoldersName() {
        List<String> listFiles = Arrays.asList(new File(getConfigsPath()).list());
        Collections.sort(listFiles);
        List<String> foldersName = new ArrayList<>();

        for (String s : listFiles) {
            File fileEntry = new File(getConfigsPath() + "/" + s);
            if (fileEntry.isDirectory()) {
                foldersName.add(s);
            }
        }
        return foldersName;
    }

    public List<String> getObjectsNameInFolder(File folder) {
        List<String> listFiles = Arrays.asList(folder.list());
        Collections.sort(listFiles);
        List<String> objectsName = new ArrayList<>();

        for (String s : listFiles) {
            File fileEntry = new File(folder.getPath() + "/" + s);
            if (fileEntry.isDirectory()) {
                objectsName.addAll(getObjectsNameInFolder(fileEntry));
            } else {
                if (!fileEntry.getName().contains(".yml"))
                    continue;
                String currentId = fileEntry.getName().split(".yml")[0];
                objectsName.add(currentId);
            }
        }
        return objectsName;
    }


    public File searchFileOfObject(String id) {

        List<String> listFiles = Arrays.asList(new File(getConfigsPath()).list());
        Collections.sort(listFiles);

        for (String s : listFiles) {
            File fileEntry = new File(getConfigsPath() + "/" + s);
            //System.out.println("::::::::::::::" +fileEntry.getAbsolutePath());
            if (fileEntry.isDirectory()) {
                File result = null;
                if ((result = searchFileOfObjectInFolder(id, fileEntry)) == null)
                    continue;
                else
                    return result;
            } else {
                if (!fileEntry.getName().contains(".yml") || fileEntry.getName().contains(".txt"))
                    continue;
                String currentId = fileEntry.getName().split(".yml")[0];
                if (id.equals(currentId))
                    return fileEntry;

            }
        }
        return null;
    }

    public File searchFolder(String folderName) {
        return searchFolder(new File(getConfigsPath()), folderName);
    }

    public File searchFolder(File folder, String folderName) {
        if (folder == null || !folder.isDirectory()) return null;
        List<String> listFiles = Arrays.asList(folder.list());
        Collections.sort(listFiles);

        for (String s : listFiles) {
            /* relative to the folder searched: from the root, a sub folder (folder:packs/swords) was never found */
            File fileEntry = new File(folder, s);
            if (fileEntry.isDirectory()) {
                if (fileEntry.getName().equals(folderName))
                    return fileEntry;
                else {
                    File result = null;
                    if ((result = searchFolder(fileEntry, folderName)) == null)
                        continue;
                    else
                        return result;
                }
            }
        }
        return null;
    }

    public Optional<T> getObjectByFile(File file, String id, boolean showError) {
        try {
            if (this.CreateBackupFilIfNotValid(file)) return Optional.empty();
            configVersionsConverter(file);
            FileConfiguration objectConfig = (FileConfiguration) YamlConfiguration.loadConfiguration(file);
            return getObject(file, objectConfig, id, showError);
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public Optional<T> getObjectByReader(Reader reader, String id, boolean showError, Map<String, String> wordsToReplace) {
        try {
            FileConfiguration firstConfig = (FileConfiguration) YamlConfiguration.loadConfiguration(reader);
            String toStr = firstConfig.saveToString();
            for (String word : wordsToReplace.keySet()) {
                toStr = toStr.replaceAll(word, wordsToReplace.get(word));
            }
            YamlConfiguration config = new YamlConfiguration();
            config.loadFromString(toStr);

            return getDefaultObject((FileConfiguration) config, id, showError);
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public abstract void configVersionsConverter(File file);

    public Optional<T> getObjectById(String id, boolean showError) {
        return getObjectByFile(searchFileOfObject(id), id, showError);
    }

    public Optional<T> getObject(File file, FileConfiguration objectConfig, String id, boolean showError) {
        return getObject(objectConfig, id, showError, !sPlugin.isLotOfWork(), file.getPath());
    }

    public Optional<T> getObject(FileConfiguration objectConfig, String id, boolean showError) {
        return getObject(objectConfig, id, showError, !sPlugin.isLotOfWork(), searchFileOfObject(id).getPath());
    }

    public Optional<T> getDefaultObject(FileConfiguration objectConfig, String id, boolean showError) {
        return getObject(objectConfig, id, showError, true, "");
    }

    public abstract Optional<T> getObject(FileConfiguration objectConfig, String id, boolean showError, boolean isPremiumLoading, String path);


    public boolean CreateBackupFilIfNotValid(File file) {

        YamlConfiguration loader = new YamlConfiguration();

        @SuppressWarnings("unused")
        FileConfiguration config;
        try {
            loader.load(file);
            config = (FileConfiguration) loader;
            return false;
        } catch (Exception e) {
            sPlugin.getPlugin().getLogger().severe("Error when loading " + file.getName() + ", your config is not made correctly ! this website can help you to resolve your problem: https://codebeautify.org/yaml-validator ");
            File fileBackup = new File(file.getParent() + "/" + file.getName() + ".txt");

            int i = 1;
            while (fileBackup.exists()) {
                fileBackup = new File(file.getParent() + "/" + file.getName() + i + ".txt");
                i++;
            }

            if (!fileBackup.exists()) {
                try {
                    fileBackup.getParentFile().mkdir();
                    fileBackup.createNewFile();
                    InputStream is = new FileInputStream(file);
                    OutputStream os = new FileOutputStream(fileBackup);
                    ByteStreams.copy(is, os);
                    is.close();
                    os.close();
                } catch (Exception e2) {
                    throw new RuntimeException("Unable to create the backup file: " + file.getName(), e2);
                }

            }
            return true;
        }
    }

    public List<String> getAllObjects() {
        ArrayList<String> result = new ArrayList<>();
        if (new File(getConfigsPath()).exists()) {
            List<String> listFiles = Arrays.asList(new File(getConfigsPath()).list());
            Collections.sort(listFiles);

            for (String s : listFiles) {
                File fileEntry = new File(getConfigsPath() + "/" + s);
                if (fileEntry.isDirectory()) result.addAll(getAllObjectsOfFolder(fileEntry));
                else {
                    if (!fileEntry.getName().contains(".yml")) continue;
                    String id = fileEntry.getName().split(".yml")[0];

                    result.add(id);
                }
            }
        }
        return result;
    }

    public List<String> getAllObjectsLowerCases() {
        ArrayList<String> result = new ArrayList<>();
        for (String s : getAllObjects()) {
            result.add(s.toLowerCase());
        }
        return result;
    }

    public List<String> getAllObjectsOfFolder(File folder) {
        ArrayList<String> result = new ArrayList<>();

        List<String> listFiles = Arrays.asList(folder.list());
        Collections.sort(listFiles);

        for (String s : listFiles) {
            File fileEntry = new File(folder + "/" + s);
            if (fileEntry.isDirectory()) result.addAll(getAllObjectsOfFolder(fileEntry));
            else {
                if (!fileEntry.getName().contains(".yml")) continue;
                String[] split = fileEntry.getName().split(".yml");
                if (split.length == 0) continue; // to avoid stupid issue with files named .yml
                String id = split[0];

                result.add(id);
            }
        }

        return result;
    }

    /* Reload all the configuration and load those that arent loaded */
    public void reload() {
        load();
    }

    public boolean reloadFolder(String folderName) {
        File folder = searchFolder(folderName);
        if (folder == null) return false;
        List<String> listFiles = getObjectsNameInFolder(folder);
        for (String s : listFiles) {
            sObjectManager.reloadObject(s);
        }
        return true;
    }

    public List<T> getAllLoadedObjectsOfFolder(String folderName) {
        List<T> result = new ArrayList<>();
        File folder = searchFolder(folderName);
        if (folder == null) return result;
        List<String> listFiles = getObjectsNameInFolder(folder);
        for (String s : listFiles) {
            sObjectManager.getLoadedObjectWithID(s).ifPresent(result::add);
        }
        return result;
    }

    public void resetCpt() {
        this.cpt = 0;
    }

    public String getConfigsPath() {
        return sPlugin.getPlugin().getDataFolder() + "/" + objectName + "/";
    }
}
