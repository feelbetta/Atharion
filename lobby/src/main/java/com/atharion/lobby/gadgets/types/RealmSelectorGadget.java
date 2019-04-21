package com.atharion.lobby.gadgets.types;

import com.atharion.lobby.gadgets.Gadget;
import com.atharion.lobby.guis.RealmSelectorGui;
import com.sllibrary.bukkit.Events;
import com.sllibrary.bukkit.item.ItemStacks;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

public class RealmSelectorGadget implements Gadget {

    @Nonnull
    @Override
    public String getName() {
        return "Realm Selector";
    }

    @Nonnull
    @Override
    public ItemStack getIdentifier() {
        return ItemStacks.of(Material.ENDER_PORTAL_FRAME)
                .name("&6Realms")
                .lore("&8Gadget")
                .lore(" ")
                .lore("&fRight Click &eto select a Realm!")
                .build();
    }

    @Override
    public int getSlot() {
        return 4;
    }

    @Override
    public boolean canUse(@Nonnull Player player) {
        return true;
    }

    @Override
    public void onUse(@Nonnull Player player) {
        new RealmSelectorGui(player).open();
        player.getInventory().setHeldItemSlot(0);
        Events.call(new PlayerItemHeldEvent(player, this.getSlot(), 0));
        player.playSound(player.getLocation(), Sound.AMBIENT_CAVE, 2, 20);
    }
}
