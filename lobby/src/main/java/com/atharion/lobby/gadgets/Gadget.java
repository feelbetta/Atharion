package com.atharion.lobby.gadgets;

import com.sllibrary.bukkit.terminable.Terminable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

public interface Gadget extends Terminable {

    @Nonnull
    String getName();

    @Nonnull
    ItemStack getIdentifier();

    int getSlot();

    boolean canUse(@Nonnull Player player);

    void onUse(@Nonnull Player player);

    @Override
    default void close() throws Exception {

    }
}