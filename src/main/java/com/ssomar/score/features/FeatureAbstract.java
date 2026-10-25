package com.ssomar.score.features;

import com.google.common.base.Charsets;
import com.ssomar.score.SCore;
import com.ssomar.score.config.GeneralConfig;
import com.ssomar.score.menu.GUI;
import com.ssomar.score.utils.scheduler.RunnableManager;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.Optional;

@Getter
public abstract class FeatureAbstract<FINAL_VALUE_CLASS, FEATURE_CLASS> implements FeatureInterface<FINAL_VALUE_CLASS, FEATURE_CLASS>, Serializable {


    private FeatureSettingsInterface featureSettings;

    @Setter
    private transient FeatureParentInterface parent;
    @Setter
    private boolean isPremium;

    private volatile boolean saveLaunched;

    public FeatureAbstract(FeatureParentInterface parent, FeatureSettingsInterface featureSettings) {
        this.parent = parent;
        if (parent == null && (this instanceof FeatureParentInterface)) {
            this.parent = (FeatureParentInterface) this;
        }
        this.featureSettings = featureSettings;
        if (parent != null) {
            this.isPremium = parent.isPremium();
        } else this.isPremium = true;
    }

    @Override
    public FeatureSettingsInterface getFeatureSettings() {
        return featureSettings;
    }

    @Override
    public String getName() {
        return featureSettings.getName();
    }

    @Override
    public String getEditorName() {
        return featureSettings.getEditorName();
    }

    public String[] getEditorDescription() {
        return featureSettings.getEditorDescription();
    }

    public Material getEditorMaterial() {
        return featureSettings.getEditorMaterial();
    }

    public boolean isRequirePremium() {
        return featureSettings == null || featureSettings.isRequirePremium();
    }


    public static void space(Player p) {
       p.sendMessage("");
    }

    public <M> FeatureReturnCheckPremium<M> checkPremium(String featureTypeName, M value, Optional<M> defaultValue, boolean isPremiumLoading) {
        if (this.isRequirePremium() && !isPremiumLoading) {
            if (defaultValue.isPresent()) {
                if (!defaultValue.get().equals(value)) {
                    return new FeatureReturnCheckPremium<>("&cERROR, Couldn't load the " + featureTypeName + " value of " + this.getName() + " from config, value: " + value + " &7&o" + getParent().getParentInfo() + " &6>> Because it's a premium feature ! (reset to default: " + defaultValue.get() + ")", defaultValue.get());
                }
            } else {
                return new FeatureReturnCheckPremium<>("&cERROR, Couldn't load the " + featureTypeName + " value of " + this.getName() + " from config, value: " + value + " &7&o" + getParent().getParentInfo() + " &6>> Because it's a premium feature !", null);
            }
        }
        return new FeatureReturnCheckPremium<>();
    }

    @Override
    public boolean isTheFeatureClickedParentEditor(String featureClicked) {
        //SsomarDev.testMsg("featureClicked: "+featureClicked + " | getEditorName(): "+getEditorName(), true);
        return featureClicked.equals(getEditorName());
    }

    @Override
    public void initAndUpdateItemParentEditor(GUI gui, int slot) {
        initItemParentEditor(gui, slot).updateItemParentEditor(gui);
    }

    /* Save will be make with a delay of 2 minutes minimum */
    public void save(boolean saveInstantly) {
        long delay = 20 * 120;
        if (saveInstantly) delay = 0;

        if (!saveLaunched || saveInstantly) {
            saveLaunched = true;
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    ConfigurationSection config = parent.getConfigurationSection();
                    save(config);
                    while (config.getParent() != null) {
                        config = config.getParent();
                    }
                    writeInFile(config);
                    saveLaunched = false;
                    RunnableManager.getInstance().removeTask(this);
                }
            };
            if (delay == 0) runnable.run();
            else {
                RunnableManager.getInstance().addTask(runnable);
                SCore.schedulerHook.runTask(runnable, delay);
            }
        }
    }

    public void save() {
        save(true);
    }


    public void writeInFile(ConfigurationSection config) {
        try {
            File file = parent.getFile();
            if (file.exists()) {
                Writer writer = new OutputStreamWriter(new FileOutputStream(file), Charsets.UTF_8);

                try {
                    writer.write(((FileConfiguration) config).saveToString());
                } finally {
                    writer.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public abstract FeatureInterface clone(FeatureParentInterface newParent);

    public FeatureParentInterface getParent() {
        //System.out.println("passe getParent > "+(this instanceof FeatureParentInterface)+ " >> "+(parent == this));
        if (this instanceof FeatureParentInterface && parent == this) return (FeatureParentInterface) this;
        else return parent;
    }

    public <B> Optional<B> getParent(B clazz) {
        FeatureParentInterface parent = getParent();
        while (parent != null && parent instanceof FeatureInterface && ((FeatureAbstract) parent).getParent() != parent) {
            Class classs = ((Object) clazz).getClass();
            if (parent.getClass() == classs) {
                return Optional.of((B) parent);
            } else {
                parent = ((FeatureAbstract) parent).getParent();
                parent.reload();
            }
        }
        return Optional.empty();
    }

    public boolean isSavingOnlyIfDiffDefault() {
        return (GeneralConfig.getInstance().isVerbosityMinimal() || getFeatureSettings().getSavingVerbosityLevel() == SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT) && !GeneralConfig.getInstance().isVerbosityMaximal();
    }
}
