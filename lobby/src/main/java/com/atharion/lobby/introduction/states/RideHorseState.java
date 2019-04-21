package com.atharion.lobby.introduction.states;

import com.atharion.commons.states.ConditionalState;
import com.atharion.lobby.conversations.ConversationLang;
import com.atharion.lobby.introduction.extras.HorseNpc;
import com.atharion.lobby.lang.Lang;
import com.sllibrary.bukkit.Events;
import com.sllibrary.bukkit.item.ItemStacks;
import com.sllibrary.bukkit.serialize.Position;
import com.sllibrary.bukkit.terminable.TerminableConsumer;
import com.sllibrary.bukkit.utils.Players;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import javax.annotation.Nonnull;
import java.util.Objects;

public class RideHorseState extends ConditionalState {

    private static final double RANGE_DISTANCE = 65.5D;

    private final Player player;

    private final HorseNpc horseNpc;

    private boolean inTargetRange;

    private final Position origin;

    public RideHorseState(@Nonnull Player player, @Nonnull Position origin, @Nonnull Position targetLocation) {
        this.player = Objects.requireNonNull(player, "player");
        this.horseNpc = new HorseNpc((this.origin = origin).toLocation(), targetLocation.toLocation(), this.player);
    }

    @Override
    public boolean getCondition() {
        return this.horseNpc.hasReachedTarget();
    }

    @Override
    public void onUpdate() {
        this.player.closeInventory();
        if (this.horseNpc.getCurrentLocation() == null || this.inTargetRange) {
            return;
        }
        if (this.horseNpc.getCurrentLocation().distance(this.player.getWorld().getSpawnLocation()) <= RANGE_DISTANCE) {
            this.inTargetRange = true;
            this.player.sendMessage(ConversationLang.INTRODUCTION_IN_VILLAGE_RANGE.toString());
            this.player.playSound(this.player.getLocation(), Sound.BLOCK_NOTE_CHIME, 2, 15);
        }
    }

    @Override
    public void onStart() {
        this.horseNpc.bindWith(this);
        Players.resetWalkSpeed(this.player);
        this.player.teleport(this.origin.toLocation());
        this.player.getInventory().setHeldItemSlot(3);
        this.player.getInventory().setItem(0, ItemStacks.of(Material.BREAD).build());
        this.player.getInventory().setItem(1, ItemStacks.of(Material.BREAD).amount(2).build());
        this.player.getInventory().setItem(3, ItemStacks.of(Material.COMPASS).build());
        this.player.getInventory().setItem(5, ItemStacks.of(Material.GLASS_BOTTLE).build());
        this.player.getInventory().setItem(6, ItemStacks.of(Material.POTION).potion(new PotionData(PotionType.WATER)).build());
        this.player.getInventory().setItem(7, ItemStacks.of(Material.GLASS_BOTTLE).build());
        this.player.getInventory().setItem(8, ItemStacks.of(Material.GLASS_BOTTLE).build());
        this.player.getInventory().setItemInOffHand(ItemStacks.of(Material.SHIELD).build());
        this.horseNpc.navigate();
        Events.subscribe(PlayerQuitEvent.class)
                .handler(event -> this.horseNpc.closeAndReportException())
                .bindWith(this);
    }

    @Override
    public void onEnd() {
        this.player.getVehicle().eject();
        this.player.playSound(this.player.getLocation(), Sound.ENTITY_HORSE_GALLOP, 2, 1);
    }
}