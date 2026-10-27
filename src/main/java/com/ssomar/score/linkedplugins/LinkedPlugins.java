package com.ssomar.score.linkedplugins;

import com.ssomar.executableblocks.api.ExecutableBlocksAPI;
import com.ssomar.score.SCore;
import com.ssomar.score.api.executableitems.ExecutableItemsAPI;
import com.ssomar.score.sobject.SObject;
import com.ssomar.score.splugin.SPlugin;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LinkedPlugins {

    @Nullable
    public static SObject getSObject(SPlugin sPlugin, String objectID) {

        switch (sPlugin.getName().toUpperCase()) {

            // TODO: Add  plugins here
            case "EXECUTABLEBLOCKS":
                if (SCore.hasExecutableBlocks) {
                    return (SObject) ExecutableBlocksAPI.getExecutableBlocksManager().getExecutableBlock(objectID).get();
                }
                break;

            case "EXECUTABLEITEMS":
                if (SCore.hasExecutableItems) {
                    return (SObject) ExecutableItemsAPI.getExecutableItemsManager().getExecutableItem(objectID).get();
                }
                break;

            default:
                return null;
        }

        return null;
    }
}
