package com.atharion.lobby.gadgets.handler;

import com.atharion.commons.eventbus.EventBus;
import com.atharion.lobby.AtharionLobby;
import com.atharion.lobby.gadgets.Gadget;
import com.atharion.lobby.gadgets.types.BombGadget;
import com.atharion.lobby.gadgets.types.DonationGadget;
import com.atharion.lobby.gadgets.types.RealmSelectorGadget;
import com.sllibrary.bukkit.Events;
import com.sllibrary.bukkit.Services;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.EnumDirection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Door;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;

public class GadgetHandler {

    private static final Set<Gadget> GADGETS = new HashSet<>();

    public GadgetHandler(@Nonnull AtharionLobby atharionLobby) {
        Objects.requireNonNull(atharionLobby, "plugin instance");
        GadgetHandler.GADGETS.add(new BombGadget(atharionLobby));
        GadgetHandler.GADGETS.add(new DonationGadget());
        GadgetHandler.GADGETS.add(new RealmSelectorGadget());

        Services.load(EventBus.class)
                .subscribe(PlayerInteractEvent.class,
                        playerInteractEvent -> playerInteractEvent.getHand() == EquipmentSlot.HAND && this.isRightClick(playerInteractEvent.getAction()) && this.isGadget(playerInteractEvent.getItem()),

                        playerInteractEvent -> GadgetHandler.GADGETS.stream()
                                .filter(gadget -> gadget.getIdentifier().getItemMeta().getLore().equals(playerInteractEvent.getItem().getItemMeta().getLore()))
                                .findFirst()
                                .ifPresent(gadget -> {
                                    playerInteractEvent.setCancelled(true);
                                    if (!gadget.canUse(playerInteractEvent.getPlayer())) {
                                        return;
                                    }
                                    gadget.onUse(playerInteractEvent.getPlayer());
                                }));

        Services.load(EventBus.class)
                .subscribe(PlayerJoinEvent.class,
                        playerJoinEvent -> this.applyGadgets(playerJoinEvent.getPlayer().getInventory()));

        Services.load(EventBus.class)
                .subscribe(PlayerItemHeldEvent.class,
                        playerItemHeldEvent -> {
                            int previousSlot = playerItemHeldEvent.getPreviousSlot();
                            int newSlot = playerItemHeldEvent.getNewSlot();

                            Player player = playerItemHeldEvent.getPlayer();
                            Inventory inventory = player.getInventory();

                            ItemStack previousSlotItem = inventory.getItem(previousSlot);
                            ItemStack newSlotItem = inventory.getItem(newSlot);

                            if (this.isGadget(previousSlotItem)) {
                                previousSlotItem.removeEnchantment(Enchantment.DURABILITY);
                            }
                            if (this.isGadget(newSlotItem)) {
                                newSlotItem.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
                            }
                            player.updateInventory();
                        });
    }

    public void applyGadgets(@Nonnull Inventory inventory) {
        Objects.requireNonNull(inventory, "inventory");
        GADGETS.forEach(gadget -> inventory.setItem(gadget.getSlot(), gadget.getIdentifier()));
    }

    private boolean isGadget(@Nullable ItemStack itemStack) {
        return itemStack != null && itemStack.getType() != Material.AIR && itemStack.getItemMeta().hasLore() && GadgetHandler.GADGETS.stream()
                .map(Gadget::getIdentifier)
                .map(ItemStack::getItemMeta)
                .map(ItemMeta::getLore)
                .anyMatch(itemStack.getItemMeta().getLore()::equals);
    }

    private boolean isRightClick(@Nonnull Action action) {
        Objects.requireNonNull(action, "action");
        return action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK;
    }
}
