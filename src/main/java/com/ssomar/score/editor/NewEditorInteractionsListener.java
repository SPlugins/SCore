package com.ssomar.score.editor;

import com.ssomar.score.utils.backward_compatibility.SnapshotUtils;
import com.ssomar.score.features.custom.activators.group.ActivatorsFeatureEditor;
import com.ssomar.score.features.custom.activators.group.ActivatorsFeatureEditorManager;
import com.ssomar.score.features.custom.aroundblock.group.AroundBlockGroupFeatureEditor;
import com.ssomar.score.features.custom.aroundblock.group.AroundBlockGroupFeatureEditorManager;
import com.ssomar.score.features.custom.attributes.group.AttributesGroupFeatureEditor;
import com.ssomar.score.features.custom.attributes.group.AttributesGroupFeatureEditorManager;
import com.ssomar.score.features.custom.blocksAttacksFeatures.DamageReductionFeatures.group.DamageReductionGroupFeatureEditor;
import com.ssomar.score.features.custom.blocksAttacksFeatures.DamageReductionFeatures.group.DamageReductionGroupFeatureEditorManager;
import com.ssomar.score.features.custom.conditions.block.parent.BlockConditionsFeatureEditor;
import com.ssomar.score.features.custom.conditions.block.parent.BlockConditionsFeatureEditorManager;
import com.ssomar.score.features.custom.conditions.custom.parent.CustomConditionsFeatureEditor;
import com.ssomar.score.features.custom.conditions.custom.parent.CustomConditionsFeatureEditorManager;
import com.ssomar.score.features.custom.conditions.entity.parent.EntityConditionsFeatureEditor;
import com.ssomar.score.features.custom.conditions.entity.parent.EntityConditionsFeatureEditorManager;
import com.ssomar.score.features.custom.conditions.item.parent.ItemConditionsFeatureEditor;
import com.ssomar.score.features.custom.conditions.item.parent.ItemConditionsFeatureEditorManager;
import com.ssomar.score.features.custom.conditions.placeholders.group.PlaceholderConditionGroupFeatureEditor;
import com.ssomar.score.features.custom.conditions.placeholders.group.PlaceholderConditionGroupFeatureEditorManager;
import com.ssomar.score.features.custom.conditions.placeholders.placeholder.PlaceholderConditionFeatureEditor;
import com.ssomar.score.features.custom.conditions.placeholders.placeholder.PlaceholderConditionFeatureEditorManager;
import com.ssomar.score.features.custom.conditions.player.parent.PlayerConditionsFeatureEditor;
import com.ssomar.score.features.custom.conditions.player.parent.PlayerConditionsFeatureEditorManager;
import com.ssomar.score.features.custom.conditions.world.parent.WorldConditionsFeatureEditor;
import com.ssomar.score.features.custom.conditions.world.parent.WorldConditionsFeatureEditorManager;
import com.ssomar.score.features.custom.detailedslots.DetailedSlotsEditor;
import com.ssomar.score.features.custom.detailedslots.DetailedSlotsEditorManager;
import com.ssomar.score.features.custom.enchantments.group.EnchantmentsGroupFeatureEditor;
import com.ssomar.score.features.custom.enchantments.group.EnchantmentsGroupFeatureEditorManager;
import com.ssomar.score.features.custom.entities.group.EntityTypeGroupFeatureEditor;
import com.ssomar.score.features.custom.entities.group.EntityTypeGroupFeatureEditorManager;
import com.ssomar.score.features.custom.firework.explosion.group.FireworkExplosionGroupFeatureEditor;
import com.ssomar.score.features.custom.firework.explosion.group.FireworkExplosionGroupFeatureEditorManager;
import com.ssomar.score.features.custom.ifhas.executableitems.group.HasExecutableItemGroupFeatureEditor;
import com.ssomar.score.features.custom.ifhas.executableitems.group.HasExecutableItemGroupFeatureEditorManager;
import com.ssomar.score.features.custom.ifhas.items.group.HasItemGroupFeatureEditor;
import com.ssomar.score.features.custom.ifhas.items.group.HasItemGroupFeatureEditorManager;
import com.ssomar.score.features.custom.materialwithgroupsandtags.group.MaterialAndTagsGroupFeatureEditor;
import com.ssomar.score.features.custom.materialwithgroupsandtags.group.MaterialAndTagsGroupFeatureEditorManager;
import com.ssomar.score.features.custom.othereicooldowns.group.OtherEICooldownGroupFeatureEditor;
import com.ssomar.score.features.custom.othereicooldowns.group.OtherEICooldownGroupFeatureEditorManager;
import com.ssomar.score.features.custom.particles.group.ParticlesGroupFeatureEditor;
import com.ssomar.score.features.custom.particles.group.ParticlesGroupFeatureEditorManager;
import com.ssomar.score.features.custom.patterns.group.PatternsGroupFeatureEditor;
import com.ssomar.score.features.custom.patterns.group.PatternsGroupFeatureEditorManager;
import com.ssomar.score.features.custom.patterns.subgroup.PatternFeatureEditor;
import com.ssomar.score.features.custom.patterns.subgroup.PatternFeatureEditorManager;
import com.ssomar.score.features.custom.patterns.subpattern.SubPatternFeatureEditor;
import com.ssomar.score.features.custom.patterns.subpattern.SubPatternFeatureEditorManager;
import com.ssomar.score.features.custom.potioneffects.group.PotionEffectGroupFeatureEditor;
import com.ssomar.score.features.custom.potioneffects.group.PotionEffectGroupFeatureEditorManager;
import com.ssomar.score.features.custom.required.executableitems.group.RequiredExecutableItemGroupFeatureEditor;
import com.ssomar.score.features.custom.required.executableitems.group.RequiredExecutableItemGroupFeatureEditorManager;
import com.ssomar.score.features.custom.required.items.group.RequiredItemGroupFeatureEditor;
import com.ssomar.score.features.custom.required.items.group.RequiredItemGroupFeatureEditorManager;
import com.ssomar.score.features.custom.required.magic.group.RequiredMagicGroupFeatureEditor;
import com.ssomar.score.features.custom.required.magic.group.RequiredMagicGroupFeatureEditorManager;
import com.ssomar.score.features.custom.toolrules.group.ToolRulesGroupFeatureEditor;
import com.ssomar.score.features.custom.toolrules.group.ToolRulesGroupFeatureEditorManager;
import com.ssomar.score.features.custom.variables.base.group.VariablesGroupFeatureEditor;
import com.ssomar.score.features.custom.variables.base.group.VariablesGroupFeatureEditorManager;
import com.ssomar.score.features.custom.variables.update.group.VariableUpdateGroupFeatureEditor;
import com.ssomar.score.features.custom.variables.update.group.VariableUpdateGroupFeatureEditorManager;
import com.ssomar.score.features.editor.GenericFeatureParentEditor;
import com.ssomar.score.features.editor.GenericFeatureParentEditorManager;
import com.ssomar.score.features.editor.GenericFeatureParentEditorReloaded;
import com.ssomar.score.features.editor.GenericFeatureParentEditorReloadedManager;
import com.ssomar.score.features.menu_organisation.MenuGroupEditor;
import com.ssomar.score.features.menu_organisation.MenuGroupEditorManager;
import com.ssomar.score.hardness.hardness.HardnessEditor;
import com.ssomar.score.hardness.hardness.HardnessEditorManager;
import com.ssomar.score.menu.GUI;
import com.ssomar.score.projectiles.SProjectileEditor;
import com.ssomar.score.projectiles.SProjectileEditorManager;
import com.ssomar.score.projectiles.features.visualItemFeature.VisualItemFeatureEditor;
import com.ssomar.score.projectiles.features.visualItemFeature.VisualItemFeatureEditorManager;
import com.ssomar.score.scheduler.ScheduleFeaturesEditor;
import com.ssomar.score.scheduler.ScheduleFeaturesEditorManager;
import com.ssomar.score.sobject.menu.NewSObjectsManagerEditor;
import com.ssomar.score.sobject.menu.SObjectsEditorAbstract;
import com.ssomar.score.sobject.menu.defaultobjects.NewDefaultObjectsEditor;
import com.ssomar.score.sobject.menu.defaultobjects.NewDefaultObjectsEditorManager;
import com.ssomar.score.variables.VariableEditor;
import com.ssomar.score.variables.VariableEditorManager;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class NewEditorInteractionsListener implements Listener {

    @EventHandler
    public void onInvClick(InventoryCreativeEvent e) {
        //SsomarDev.testMsg(">> click1: "+e.getClick());
        if (!(e.getWhoClicked() instanceof Player) || e.getClick() != ClickType.MIDDLE) return;
        
        Player player = (Player) e.getWhoClicked();
        try {
            if (e.getClickedInventory().getType().equals(InventoryType.PLAYER)) return;
        } catch (NullPointerException error) {
            return;
        }

        int slot = e.getSlot();
        ItemStack itemS = e.getView().getItem(slot);
        //boolean notNullItem = itemS!=null;

        try {
            if (SnapshotUtils.getHolderNoSnapshot(e.getInventory()) instanceof GUI)
                this.manage(player, itemS, "", e);
        } catch (NullPointerException error) {
            error.printStackTrace();
        }
    }

    @EventHandler
    public void onInvClick(InventoryClickEvent e) {

        //SsomarDev.testMsg(">> click2: "+e.getClick());
        if (!(e.getWhoClicked() instanceof Player)) return;

        Player player = (Player) e.getWhoClicked();
        try {
            if (e.getClickedInventory().getType().equals(InventoryType.PLAYER)) return;
        } catch (NullPointerException error) {
            return;
        }

        int slot = e.getSlot();
        ItemStack itemS = e.getClickedInventory().getItem(slot);
        //boolean notNullItem = itemS!=null;

        try {
            if (SnapshotUtils.getHolderNoSnapshot(e.getInventory()) instanceof GUI)
                this.manage(player, itemS, "", e);
        } catch (NullPointerException error) {
            error.printStackTrace();
        }
    }

    public void manage(Player player, ItemStack itemS, String guiType, InventoryClickEvent e) {

        e.setCancelled(true);

        if (itemS == null) return;

        if (!itemS.hasItemMeta()) return;

        if (!GUI.hasIdentifier(itemS)) return;

        InventoryHolder holder = SnapshotUtils.getHolderNoSnapshot(e.getInventory());

        if (holder instanceof SObjectsEditorAbstract) {
            NewSObjectsManagerEditor.getInstance().clicked(player, itemS, e.getClick());
            return;
        }
        else if (holder instanceof NewDefaultObjectsEditor) {
            NewDefaultObjectsEditorManager.getInstance().clicked(player, itemS, e.getClick());
            return;
        }  else if (holder instanceof RequiredItemGroupFeatureEditor) {
            RequiredItemGroupFeatureEditorManager.getInstance().clicked(player, itemS, e.getClick());
            return;
        }  else if (holder instanceof RequiredExecutableItemGroupFeatureEditor) {
            RequiredExecutableItemGroupFeatureEditorManager.getInstance().clicked(player, itemS, e.getClick());
            return;
        }  else if (holder instanceof RequiredMagicGroupFeatureEditor) {
            RequiredMagicGroupFeatureEditorManager.getInstance().clicked(player, itemS, e.getClick());
            return;
        } else if (holder instanceof GenericFeatureParentEditor) {
            GenericFeatureParentEditor genericFeatureParentEditor = (GenericFeatureParentEditor) holder;
            genericFeatureParentEditor.click(player, itemS, e.getClick());
            return;
        }
        else if (holder instanceof GenericFeatureParentEditorReloaded) {
            GenericFeatureParentEditorReloaded genericFeatureParentEditorReloaded = (GenericFeatureParentEditorReloaded) holder;
            genericFeatureParentEditorReloaded.click(player, itemS, e.getClick());
            return;
        }
        else if (holder instanceof EnchantmentsGroupFeatureEditor) {
            EnchantmentsGroupFeatureEditorManager.getInstance().clicked(player, itemS, e.getClick());
            return;
        }else if (holder instanceof AttributesGroupFeatureEditor) {
            AttributesGroupFeatureEditorManager.getInstance().clicked(player, itemS, e.getClick());
            return;
        }
        else if(holder instanceof MenuGroupEditor){
            MenuGroupEditorManager.getInstance().clicked(player, itemS, e.getClick());
            return;
        }
        else if (holder instanceof DamageReductionGroupFeatureEditor) {
            DamageReductionGroupFeatureEditorManager.getInstance().clicked(player, itemS, e.getClick());
            return;
        }
        else if (holder instanceof FireworkExplosionGroupFeatureEditor) {
            FireworkExplosionGroupFeatureEditorManager.getInstance().clicked(player, itemS, e.getClick());
            return;
        }
        else if (holder instanceof ActivatorsFeatureEditor) {
            ActivatorsFeatureEditorManager.getInstance().clicked(player, itemS, e.getClick());
            return;
        } else if (holder instanceof BlockConditionsFeatureEditor) {
            BlockConditionsFeatureEditorManager.getInstance().clicked(player, itemS, e.getClick());
            return;
        } else if (holder instanceof EntityConditionsFeatureEditor) {
            EntityConditionsFeatureEditorManager.getInstance().clicked(player, itemS, e.getClick());
            return;
        } else if (holder instanceof WorldConditionsFeatureEditor) {
            WorldConditionsFeatureEditorManager.getInstance().clicked(player, itemS, e.getClick());
            return;
        } else if (holder instanceof ItemConditionsFeatureEditor) {
            ItemConditionsFeatureEditorManager.getInstance().clicked(player, itemS, e.getClick());
            return;
        } else if (holder instanceof PlayerConditionsFeatureEditor) {
            PlayerConditionsFeatureEditorManager.getInstance().clicked(player, itemS, e.getClick());
            return;
        } else if (holder instanceof CustomConditionsFeatureEditor) {
            CustomConditionsFeatureEditorManager.getInstance().clicked(player, itemS, e.getClick());
            return;
        } else if (holder instanceof PlaceholderConditionFeatureEditor) {
            PlaceholderConditionFeatureEditorManager.getInstance().clicked(player, itemS, e.getClick());
            return;
        } else if (holder instanceof PlaceholderConditionGroupFeatureEditor) {
            PlaceholderConditionGroupFeatureEditorManager.getInstance().clicked(player, itemS, e.getClick());
            return;
        }  else if (holder instanceof MaterialAndTagsGroupFeatureEditor) {
            MaterialAndTagsGroupFeatureEditorManager.getInstance().clicked(player, itemS, e.getClick());
            return;
        } else if (holder instanceof EntityTypeGroupFeatureEditor) {
            EntityTypeGroupFeatureEditorManager.getInstance().clicked(player, itemS, e.getClick());
            return;
        } else if (holder instanceof DetailedSlotsEditor) {
            DetailedSlotsEditorManager.getInstance().clicked(player, itemS, e.getClick());
            return;
        }  else if (holder instanceof PotionEffectGroupFeatureEditor) {
            PotionEffectGroupFeatureEditorManager.getInstance().clicked(player, itemS, e.getClick());
            return;
        } else if (holder instanceof SubPatternFeatureEditor) {
            SubPatternFeatureEditorManager.getInstance().clicked(player, itemS, e.getClick());
            return;
        } else if (holder instanceof PatternFeatureEditor) {
            PatternFeatureEditorManager.getInstance().clicked(player, itemS, e.getClick());
            return;
        } else if (holder instanceof PatternsGroupFeatureEditor) {
            PatternsGroupFeatureEditorManager.getInstance().clicked(player, itemS, e.getClick());
            return;
        }  else if (holder instanceof VariablesGroupFeatureEditor) {
            VariablesGroupFeatureEditorManager.getInstance().clicked(player, itemS, e.getClick());
            return;
        }  else if (holder instanceof VariableUpdateGroupFeatureEditor) {
            VariableUpdateGroupFeatureEditorManager.getInstance().clicked(player, itemS, e.getClick());
            return;
        }  else if (holder instanceof HasExecutableItemGroupFeatureEditor) {
            HasExecutableItemGroupFeatureEditorManager.getInstance().clicked(player, itemS, e.getClick());
            return;
        }  else if (holder instanceof HasItemGroupFeatureEditor) {
            HasItemGroupFeatureEditorManager.getInstance().clicked(player, itemS, e.getClick());
            return;
        }  else if(holder instanceof ScheduleFeaturesEditor){
            ScheduleFeaturesEditorManager.getInstance().clicked(player, itemS, e.getClick());
            return;
        }
         else if (holder instanceof AroundBlockGroupFeatureEditor) {
            AroundBlockGroupFeatureEditorManager.getInstance().clicked(player, itemS, e.getClick());
            return;
        } else if (holder instanceof SProjectileEditor) {
            SProjectileEditorManager.getInstance().clicked(player, itemS, e.getClick());
            return;
        }
        else if (holder instanceof HardnessEditor) {
            HardnessEditorManager.getInstance().clicked(player, itemS, e.getClick());
            return;
        }
        else if (holder instanceof VariableEditor) {
            VariableEditorManager.getInstance().clicked(player, itemS, e.getClick());
            return;
        }
        else if (holder instanceof VisualItemFeatureEditor) {
            VisualItemFeatureEditorManager.getInstance().clicked(player, itemS, e.getClick());
            return;
        }
         else if (holder instanceof ParticlesGroupFeatureEditor) {
            ParticlesGroupFeatureEditorManager.getInstance().clicked(player, itemS, e.getClick());
            return;
        }
        else if (holder instanceof OtherEICooldownGroupFeatureEditor) {
            OtherEICooldownGroupFeatureEditorManager.getInstance().clicked(player, itemS, e.getClick());
            return;
        }
        else if(holder instanceof ToolRulesGroupFeatureEditor){
            ToolRulesGroupFeatureEditorManager.getInstance().clicked(player, itemS, e.getClick());
            return;
        }

    }

    /*@EventHandler(priority = EventPriority.LOWEST)
    public void onChatEvent(AsyncPlayerChatPreviewEvent e) {
        SsomarDev.testMsg("ChatEventpreview > "+e.getMessage());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChatEvent(PlayerChatEvent e) {
        SsomarDev.testMsg("Chat sync > "+e.getMessage());
    }

     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onChatEvent2(PlayerCommandPreprocessEvent e) {
       if(e.getMessage().contains("/score interact ")) {
           String message = e.getMessage().replace("/score interact ", "");
           receiveMessage(e.getPlayer(), message, e);
       }
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onChatEvent(AsyncPlayerChatEvent e) {
        receiveMessage(e.getPlayer(), e.getMessage(), e);
    }

    public void receiveMessage(Player player, String message, Cancellable e){
        Player p = player;
        if (NewSObjectsManagerEditor.getInstance().getRequestWriting().containsKey(p)) {
            e.setCancelled(true);
            NewSObjectsManagerEditor.getInstance().receiveMessage(p, message);
        } else if (NewDefaultObjectsEditorManager.getInstance().getRequestWriting().containsKey(p)) {
            e.setCancelled(true);
            NewDefaultObjectsEditorManager.getInstance().receiveMessage(p, message);
        }  else if (RequiredItemGroupFeatureEditorManager.getInstance().getRequestWriting().containsKey(p)) {
            e.setCancelled(true);
            RequiredItemGroupFeatureEditorManager.getInstance().receiveMessage(p, message);
        }  else if (RequiredExecutableItemGroupFeatureEditorManager.getInstance().getRequestWriting().containsKey(p)) {
            e.setCancelled(true);
            RequiredExecutableItemGroupFeatureEditorManager.getInstance().receiveMessage(p, message);
        } else if (RequiredMagicGroupFeatureEditorManager.getInstance().getRequestWriting().containsKey(p)) {
            e.setCancelled(true);
            RequiredMagicGroupFeatureEditorManager.getInstance().receiveMessage(p, message);
        }
        else if (GenericFeatureParentEditorManager.getInstance().getRequestWriting().containsKey(p)) {
            e.setCancelled(true);
            GenericFeatureParentEditorManager.getInstance().receiveMessage(p, message);
        }
        else if (GenericFeatureParentEditorReloadedManager.getInstance().getRequestWriting().containsKey(p)) {
            e.setCancelled(true);
            GenericFeatureParentEditorReloadedManager.getInstance().receiveMessage(p, message);
        }
        else if (EnchantmentsGroupFeatureEditorManager.getInstance().getRequestWriting().containsKey(p)) {
            e.setCancelled(true);
            EnchantmentsGroupFeatureEditorManager.getInstance().receiveMessage(p, message);
        } else if (AttributesGroupFeatureEditorManager.getInstance().getRequestWriting().containsKey(p)) {
            e.setCancelled(true);
            AttributesGroupFeatureEditorManager.getInstance().receiveMessage(p, message);
        }
        else if (MenuGroupEditorManager.getInstance().getRequestWriting().containsKey(p)) {
            e.setCancelled(true);
            MenuGroupEditorManager.getInstance().receiveMessage(p, message);
        }
        else if (DamageReductionGroupFeatureEditorManager.getInstance().getRequestWriting().containsKey(p)) {
            e.setCancelled(true);
            DamageReductionGroupFeatureEditorManager.getInstance().receiveMessage(p, message);
        }

        else if (FireworkExplosionGroupFeatureEditorManager.getInstance().getRequestWriting().containsKey(p)) {
            e.setCancelled(true);
            FireworkExplosionGroupFeatureEditorManager.getInstance().receiveMessage(p, message);
        }
        else if (ActivatorsFeatureEditorManager.getInstance().getRequestWriting().containsKey(p)) {
            e.setCancelled(true);
            ActivatorsFeatureEditorManager.getInstance().receiveMessage(p, message);
        } else if (BlockConditionsFeatureEditorManager.getInstance().getRequestWriting().containsKey(p)) {
            e.setCancelled(true);
            BlockConditionsFeatureEditorManager.getInstance().receiveMessage(p, message);
        } else if (EntityConditionsFeatureEditorManager.getInstance().getRequestWriting().containsKey(p)) {
            e.setCancelled(true);
            EntityConditionsFeatureEditorManager.getInstance().receiveMessage(p, message);
        } else if (WorldConditionsFeatureEditorManager.getInstance().getRequestWriting().containsKey(p)) {
            e.setCancelled(true);
            WorldConditionsFeatureEditorManager.getInstance().receiveMessage(p, message);
        } else if (ItemConditionsFeatureEditorManager.getInstance().getRequestWriting().containsKey(p)) {
            e.setCancelled(true);
            ItemConditionsFeatureEditorManager.getInstance().receiveMessage(p, message);
        } else if (PlayerConditionsFeatureEditorManager.getInstance().getRequestWriting().containsKey(p)) {
            e.setCancelled(true);
            PlayerConditionsFeatureEditorManager.getInstance().receiveMessage(p, message);
        } else if (CustomConditionsFeatureEditorManager.getInstance().getRequestWriting().containsKey(p)) {
            e.setCancelled(true);
            CustomConditionsFeatureEditorManager.getInstance().receiveMessage(p, message);
        } else if (PlaceholderConditionFeatureEditorManager.getInstance().getRequestWriting().containsKey(p)) {
            e.setCancelled(true);
            PlaceholderConditionFeatureEditorManager.getInstance().receiveMessage(p, message);
        } else if (PlaceholderConditionGroupFeatureEditorManager.getInstance().getRequestWriting().containsKey(p)) {
            e.setCancelled(true);
            PlaceholderConditionGroupFeatureEditorManager.getInstance().receiveMessage(p, message);
        } else if (MaterialAndTagsGroupFeatureEditorManager.getInstance().getRequestWriting().containsKey(p)) {
            e.setCancelled(true);
            MaterialAndTagsGroupFeatureEditorManager.getInstance().receiveMessage(p, message);
        } else if (EntityTypeGroupFeatureEditorManager.getInstance().getRequestWriting().containsKey(p)) {
            e.setCancelled(true);
            EntityTypeGroupFeatureEditorManager.getInstance().receiveMessage(p, message);
        }  else if (DetailedSlotsEditorManager.getInstance().getRequestWriting().containsKey(p)) {
            e.setCancelled(true);
            DetailedSlotsEditorManager.getInstance().receiveMessage(p, message);
        }  else if (PotionEffectGroupFeatureEditorManager.getInstance().getRequestWriting().containsKey(p)) {
            e.setCancelled(true);
            PotionEffectGroupFeatureEditorManager.getInstance().receiveMessage(p, message);
        }  else if (SubPatternFeatureEditorManager.getInstance().getRequestWriting().containsKey(p)) {
            e.setCancelled(true);
            SubPatternFeatureEditorManager.getInstance().receiveMessage(p, message);
        } else if (PatternFeatureEditorManager.getInstance().getRequestWriting().containsKey(p)) {
            e.setCancelled(true);
            PatternFeatureEditorManager.getInstance().receiveMessage(p, message);
        } else if (PatternsGroupFeatureEditorManager.getInstance().getRequestWriting().containsKey(p)) {
            e.setCancelled(true);
            PatternsGroupFeatureEditorManager.getInstance().receiveMessage(p, message);
        } else if (VariablesGroupFeatureEditorManager.getInstance().getRequestWriting().containsKey(p)) {
            e.setCancelled(true);
            VariablesGroupFeatureEditorManager.getInstance().receiveMessage(p, message);
        } else if (VariableUpdateGroupFeatureEditorManager.getInstance().getRequestWriting().containsKey(p)) {
            e.setCancelled(true);
            VariableUpdateGroupFeatureEditorManager.getInstance().receiveMessage(p, message);
        } else if (HasExecutableItemGroupFeatureEditorManager.getInstance().getRequestWriting().containsKey(p)) {
            e.setCancelled(true);
            HasExecutableItemGroupFeatureEditorManager.getInstance().receiveMessage(p, message);
        } else if (HasItemGroupFeatureEditorManager.getInstance().getRequestWriting().containsKey(p)) {
            e.setCancelled(true);
            HasItemGroupFeatureEditorManager.getInstance().receiveMessage(p, message);
        } else if (ScheduleFeaturesEditorManager.getInstance().getRequestWriting().containsKey(p)) {
            e.setCancelled(true);
            ScheduleFeaturesEditorManager.getInstance().receiveMessage(p, message);
        }
         else if (AroundBlockGroupFeatureEditorManager.getInstance().getRequestWriting().containsKey(p)) {
            e.setCancelled(true);
            AroundBlockGroupFeatureEditorManager.getInstance().receiveMessage(p, message);
        }
        else if (SProjectileEditorManager.getInstance().getRequestWriting().containsKey(p)) {
            e.setCancelled(true);
            SProjectileEditorManager.getInstance().receiveMessage(p, message);
        }
        else if (HardnessEditorManager.getInstance().getRequestWriting().containsKey(p)) {
            e.setCancelled(true);
            HardnessEditorManager.getInstance().receiveMessage(p, message);
        }
        else if (VariableEditorManager.getInstance().getRequestWriting().containsKey(p)) {
            e.setCancelled(true);
            VariableEditorManager.getInstance().receiveMessage(p, message);
        }
        else if (VisualItemFeatureEditorManager.getInstance().getRequestWriting().containsKey(p)) {
            e.setCancelled(true);
            VisualItemFeatureEditorManager.getInstance().receiveMessage(p, message);
        }

        else if (ParticlesGroupFeatureEditorManager.getInstance().getRequestWriting().containsKey(p)) {
            e.setCancelled(true);
            ParticlesGroupFeatureEditorManager.getInstance().receiveMessage(p, message);
        }else if (OtherEICooldownGroupFeatureEditorManager.getInstance().getRequestWriting().containsKey(p)) {
            e.setCancelled(true);
            OtherEICooldownGroupFeatureEditorManager.getInstance().receiveMessage(p, message);
        }
        else if(ToolRulesGroupFeatureEditorManager.getInstance().getRequestWriting().containsKey(p)){
            e.setCancelled(true);
            ToolRulesGroupFeatureEditorManager.getInstance().receiveMessage(p, message);
        }
    }
}
