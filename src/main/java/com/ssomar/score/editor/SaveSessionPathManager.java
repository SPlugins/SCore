package com.ssomar.score.editor;

import com.ssomar.score.SsomarDev;
import com.ssomar.score.menu.GUI;
import org.bukkit.entity.Player;

import java.util.*;

public class SaveSessionPathManager {

    private final Map<Player, List<SessionPath>> playerSessionPath;

    private static final class SessionPath {
        private final GUI gui;
        private final NewGUIManager<?> manager;

        private SessionPath(GUI gui, NewGUIManager<?> manager) {
            this.gui = gui;
            this.manager = manager;
        }
    }

    private static SaveSessionPathManager instance;

    public SaveSessionPathManager() {
        this.playerSessionPath = new HashMap<>();
    }

    public void addPlayerSessionPath(Player p, GUI path) {
        addPlayerSessionPath(p, path, null);
    }

    public void addPlayerSessionPath(Player p, GUI path, NewGUIManager<?> manager) {
        SessionPath entry = new SessionPath(path, manager);
        if(this.playerSessionPath.containsKey(p)) {
            this.playerSessionPath.get(p).add(entry);
        }
        else {
            this.playerSessionPath.put(p, new ArrayList<>(Collections.singletonList(entry)));
        }
        // If the player has more than 12 paths, keep only the last 12
        if(this.playerSessionPath.get(p).size() > 12) {
            List<SessionPath> paths = this.playerSessionPath.get(p);
            List<SessionPath> newPaths = new ArrayList<>();
            for(int i = paths.size()-12; i < paths.size(); i++) {
                newPaths.add(paths.get(i));
            }
            this.playerSessionPath.put(p, newPaths);
        }
        //SsomarDev.testMsg("ADD GUI > "+path.getInventory().getHolder(), true);
    }

    public void clearPlayerSessionPath(Player p) {
        if(this.playerSessionPath.containsKey(p)) {
            this.playerSessionPath.get(p).clear();
        }
    }

    public GUI getLastPlayerSessionPath(Player p) {
        if(this.playerSessionPath.containsKey(p)) {
            List<SessionPath> paths = this.playerSessionPath.get(p);
            if(paths.size() > 0) {
                return paths.get(paths.size()-1).gui;
            }
        }
        return null;
    }

    public GUI getLastBeforePlayerSessionPath(Player p) {
        SessionPath entry = getLastBeforePlayerSession(p);
        return entry == null ? null : entry.gui;
    }

    /**
     * Same as {@link #getLastBeforePlayerSessionPath(Player)} but also re-registers the GUI as the
     * active one of its owning manager. Every nested editor opened through the same singleton manager
     * overwrites that manager's cache slot, so without this the restored parent GUI would keep
     * resolving clicks against the child that was just closed.
     */
    public GUI restoreLastBeforePlayerSessionPath(Player p) {
        SessionPath entry = getLastBeforePlayerSession(p);
        if(entry == null) return null;
        if(entry.manager != null) entry.manager.restoreInCache(p, entry.gui);
        return entry.gui;
    }

    private SessionPath getLastBeforePlayerSession(Player p) {
        if(this.playerSessionPath.containsKey(p)) {
            List<SessionPath> paths = this.playerSessionPath.get(p);
            if(paths.size() > 1) {
                SsomarDev.testMsg("GET GUI > "+paths.get(paths.size()-2).gui.getInventory().getHolder(), true);
                return paths.get(paths.size()-2);
            }
        }
        return null;
    }

    public void removeLastPlayerSessionPath(Player p) {
        if(this.playerSessionPath.containsKey(p)) {
            List<SessionPath> paths = this.playerSessionPath.get(p);
            if(paths.size() > 1) {
                //SsomarDev.testMsg("REMOVE GUI > "+paths.get(paths.size()-1).getInventory().getHolder(), true);
                paths.remove(paths.size()-1);
            }
        }
    }

    public static SaveSessionPathManager getInstance() {
        if(instance == null) instance = new SaveSessionPathManager();
        return instance;
    }
}
