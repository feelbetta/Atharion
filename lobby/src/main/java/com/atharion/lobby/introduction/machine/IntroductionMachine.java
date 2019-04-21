package com.atharion.lobby.introduction.machine;

import com.atharion.commons.states.State;
import com.atharion.commons.states.machines.TimedGameMachine;
import com.atharion.commons.states.machines.TimedMachine;
import com.atharion.commons.util.Potions;
import com.atharion.lobby.AtharionLobby;
import com.comphenix.protocol.PacketType;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.sllibrary.bukkit.Events;
import com.sllibrary.bukkit.event.filter.EventFilters;
import com.sllibrary.bukkit.event.filter.EventHandlers;
import com.sllibrary.bukkit.protocol.Protocol;
import com.sllibrary.bukkit.utils.Players;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

public class IntroductionMachine extends TimedGameMachine {

    private final Player player;

    public IntroductionMachine(@Nonnull List<? extends State> states, @Nonnull Player player) {
        super(states);
        this.player = Objects.requireNonNull(player, "player");
    }

    @Override
    public void onInterrupt() {
        Players.resetWalkSpeed(this.player);
        Potions.removeAll(this.player);
        this.end();
    }

    @Nonnull
    @Override
    public void onUpdate() {
        Players.forEach(viewer -> {
            Players.hide(viewer, this.player);
            Players.hide(this.player, viewer);
        });
    }

    @Nonnull
    @Override
    public Duration getUpdateDuration() {
        return Duration.ofSeconds(1);
    }

    @Override
    public void onStart() {
        Events.subscribe(AsyncPlayerChatEvent.class)
                .handler(event -> event.getRecipients().removeIf(this.player::equals))
                .bindWith(this);
        Events.subscribe(PlayerCommandPreprocessEvent.class)
                .filter(EventFilters.playerIs(this.player))
                .handler(EventHandlers.cancel())
                .bindWith(this);
        Events.subscribe(PlayerItemHeldEvent.class)
                .filter(EventFilters.playerIs(this.player))
                .handler(EventHandlers.cancel())
                .bindWith(this);
        Protocol.subscribe(PacketType.Play.Client.STEER_VEHICLE)
                .filter(packetEvent -> packetEvent.getPlayer().equals(this.player))
                .handler(EventHandlers.cancel())
                .bindWith(this);

        JavaPlugin.getPlugin(AtharionLobby.class).getNpcRepository().getEntities().forEach(atharionNpc -> atharionNpc.hide(this.player));
    }

    @Override
    public void onEnd() {
        Players.forEach(viewer -> {
            Players.show(viewer, this.player);
            Players.show(this.player, viewer);
        });
        JavaPlugin.getPlugin(AtharionLobby.class).getNpcRepository().getEntities().forEach(atharionNpc -> atharionNpc.show(this.player));
        this.player.getInventory().clear();
        JavaPlugin.getPlugin(AtharionLobby.class).getGadgetHandler().applyGadgets(this.player.getInventory());
        Potions.apply(this.player, PotionEffectType.INVISIBILITY, 2);
    }
}
