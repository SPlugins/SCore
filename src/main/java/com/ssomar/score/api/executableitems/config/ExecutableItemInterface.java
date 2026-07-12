package com.ssomar.score.api.executableitems.config;

import com.ssomar.score.api.executableitems.events.AddItemInPlayerInventoryEvent;
import com.ssomar.score.features.types.ColoredStringFeature;
import com.ssomar.score.sobject.SObjectBuildable;
import com.ssomar.score.sobject.SObjectInterface;
import com.ssomar.score.sobject.SObjectWithActivators;
import com.ssomar.score.sobject.SObjectWithVariables;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * An ExecutableItem configuration: everything defined in the item's configuration file
 * (display, activators, variables...), with helpers to build, give and drop the item.
 * <p>
 * Obtain instances through
 * {@link com.ssomar.score.api.executableitems.ExecutableItemsAPI#getExecutableItemsManager()}.
 */
public interface ExecutableItemInterface extends SObjectInterface, SObjectWithActivators, SObjectBuildable, SObjectWithVariables {

    /**
     * To place at the end of your item builder: adds the infos required for the item
     * to be recognized as an ExecutableItem. It applies the lore / name of the
     * ExecutableItem over yours (but keeps your customModelData tag).
     *
     * @param item    the item to add the ExecutableItem infos to
     * @param creator the optional player considered as the owner/creator of the item
     * @return the item with the ExecutableItem infos applied
     */
    ItemStack addExecutableItemInfos(ItemStack item, Optional<Player> creator);

    /**
     * Check whether a player has the permission of this ExecutableItem.
     *
     * @param player    the player to check
     * @param showError true to send an error message to the player when the permission is missing
     * @return true if the player has the permission of this item
     */
    boolean hasItemPerm(@NotNull Player player, boolean showError);

    /**
     * Build the ExecutableItem
     *
     * @param amount  The amount of the ExecutableItem
     * @param usage   The optional custom usage of the ExecutableItem, otherwise it will use the default one
     * @param creator The optional creator of the ExecutableItem
     * @param variables The initialization of the variables of the ExecutableItem
     * @return The ExecutableItem
     */
    ItemStack buildItem(int amount, Optional<Integer> usage, Optional<Player> creator, Map<String, String> variables);

    /**
     * Build the ExecutableItem
     *
     * @param amount  The amount of the ExecutableItem
     * @param usage   The optional custom usage of the ExecutableItem, otherwise it will use the default one
     * @param creator The optional creator of the ExecutableItem
     * @return The ExecutableItem
     */
    ItemStack buildItem(int amount, Optional<Integer> usage, Optional<Player> creator);


    /**
     * Build the ExecutableItem
     *
     * @param amount  The amount of the ExecutableItem
     * @param creator The optional creator of the ExecutableItem
     * @return The ExecutableItem with default usage.
     */
    ItemStack buildItem(int amount, Optional<Player> creator);

    /**
     * Build the ExecutableItem
     *
     * @param amount  The amount of the ExecutableItem
     * @param creator The optional creator of the ExecutableItem
     * @param settings The settings of the ExecutableItem :
     *                 The variables of the ExecutableItem
     *                 - key "Variables" | Value Map{@literal <}String -{@literal >} variableId, String -{@literal >}  variableValue>
     *                 - key "Usage" | Value Integer -{@literal >} usage
     * @return The ExecutableItem with default usage.
     */
    ItemStack buildItem(int amount, Optional<Player> creator, Map<String, Object> settings);

    /**
     * @return true If the item has the feature to keep the EI on death, false otherwise
     **/
    boolean hasKeepItemOnDeath();

    /**
     * Put this ExecutableItem in cooldown for a player (all activators).
     *
     * @param player    the player to apply the cooldown to
     * @param cooldown  the cooldown duration
     * @param isInTicks true if the duration is in ticks, false for seconds
     */
    void addCooldown(Player player, int cooldown, boolean isInTicks);

    /**
     * Put a specific activator of this ExecutableItem in cooldown for a player.
     *
     * @param player      the player to apply the cooldown to
     * @param cooldown    the cooldown duration
     * @param isInTicks   true if the duration is in ticks, false for seconds
     * @param activatorID the activator to put in cooldown
     */
    void addCooldown(Player player, int cooldown, boolean isInTicks, String activatorID);

    /**
     * Put this ExecutableItem in global (server-wide) cooldown.
     *
     * @param cooldown  the cooldown duration
     * @param isInTicks true if the duration is in ticks, false for seconds
     */
    void addGlobalCooldown(int cooldown, boolean isInTicks);

    /**
     * Put a specific activator of this ExecutableItem in global (server-wide) cooldown.
     *
     * @param cooldown    the cooldown duration
     * @param isInTicks   true if the duration is in ticks, false for seconds
     * @param activatorID the activator to put in cooldown
     */
    void addGlobalCooldown(int cooldown, boolean isInTicks, String activatorID);

    /**
     * Drop this ExecutableItem on the ground.
     *
     * @param location where to drop the item
     * @param amount   the amount to drop
     * @return the dropped item entity
     */
    Item dropItem(Location location, int amount);

    /**
     * Give this ExecutableItem to a player (one item). Overflow is dropped at the player's feet.
     *
     * @param player the player to give the item to
     * @return the amount that did not fit in the inventory and was dropped
     */
    default int give(@NotNull Player player) {
        return give(player, 1);
    }

    /**
     * Give this ExecutableItem to a player. Overflow is dropped at the player's feet.
     * Fires {@link AddItemInPlayerInventoryEvent} when the item enters the inventory.
     *
     * @param player   the player to give the item to
     * @param quantity the amount to give
     * @return the amount that did not fit in the inventory and was dropped
     */
    default int give(@NotNull Player player, int quantity) {
        ItemStack itemStack = buildItem(quantity, Optional.of(player));
        int firstEmptySlot = player.getInventory().firstEmpty();
        Map<Integer, ItemStack> over = player.getInventory().addItem(itemStack);
        int out = 0;
        for (ItemStack toDrop : over.values()) {
            player.getWorld().dropItem(player.getLocation(), toDrop);
            out += toDrop.getAmount();
        }
        if (out == 0) {
            Bukkit.getPluginManager().callEvent(new AddItemInPlayerInventoryEvent(player, itemStack, firstEmptySlot));
        }
        return out;
    }

    /**
     * Get the default usage (number of uses) of this ExecutableItem configuration.
     *
     * @return the default usage count (-1 means infinite)
     */
    int getUsage();

    /**
     * Get the icon of this ExecutableItem (its built item form, e.g. for GUIs).
     *
     * @return the icon ItemStack
     */
    ItemStack getIconItem();

    /**
     * Check whether a specific activator of this ExecutableItem is in cooldown for a player.
     *
     * @param player      the player to check
     * @param activatorID the activator to check
     * @return true if the activator is currently in cooldown for this player
     */
    boolean isInCooldown(@NotNull Player player, @NotNull String activatorID);

    /**
     * Get the remaining cooldown of a specific activator of this ExecutableItem for a player.
     *
     * @param player      the player to check
     * @param activatorID the activator to check
     * @return the remaining time in seconds, or 0 if not in cooldown
     */
    double getCooldownRemaining(@NotNull Player player, @NotNull String activatorID);

    /**
     * Get the description (lore) of the ExecutableItem configuration.
     *
     * @return the description lines
     */
    List<String> getDescription();

    /**
     * Get the display name of the ExecutableItem configuration.
     *
     * @return the display name feature
     */
    ColoredStringFeature getDisplayName();

    /**
     * Set the default usage of this ExecutableItem configuration.
     *
     * @param usage the new usage count
     */
    void setUsage(int usage);
}
