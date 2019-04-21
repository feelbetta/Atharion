package com.atharion.lobby.introduction.states;

import com.atharion.commons.lang.Lang;
import com.atharion.commons.states.ConditionalState;
import com.atharion.commons.util.Potions;
import com.atharion.lobby.npcs.Skins;
import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.sllibrary.bukkit.Events;
import com.sllibrary.bukkit.event.filter.EventFilters;
import com.sllibrary.bukkit.event.filter.EventHandlers;
import com.sllibrary.bukkit.serialize.Position;
import com.sllibrary.bukkit.terminable.TerminableConsumer;
import com.sllibrary.bukkit.text.Text;
import com.sllibrary.bukkit.text.components.event.HoverEvent;
import com.sllibrary.bukkit.utils.CollectionUtils;
import com.sllibrary.bukkit.utils.Players;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

public class ComaState extends ConditionalState {

    private static final List<String> MESSAGES = Lists.newArrayList(
            Lang.CONVERSATION_PLAYER_SELF.format("The village should be just up ahead.."),
            Lang.CONVERSATION_PLAYER_SELF.format("Hopefully I can gather some supplies while I'm there.")
    );

    private int current = 0;
    private int second = 0;

    private final Player player;
    private final Position position;

    public ComaState(@Nonnull Player player, @Nonnull Position position) {
        this.player = Objects.requireNonNull(player, "player");
        this.position = Objects.requireNonNull(position, "position");
    }

    @Override
    public boolean getCondition() {
        return this.current == MESSAGES.size();
    }

    @Override
    public void onUpdate() {
        this.player.closeInventory();
        this.second++;
        if (this.second % 4 != 0) {
            return;
        }
        this.player.sendMessage(Text.colorize(MESSAGES.get(this.current)));
        this.player.playSound(this.player.getLocation(), Sound.BLOCK_NOTE_CHIME, 2, 15);
        this.current++;
    }

    @Override
    public void onStart() {
        Events.subscribe(PlayerJumpEvent.class)
                .filter(playerJumpEvent -> playerJumpEvent.getPlayer().equals(this.player))
                .handler(EventHandlers.cancel())
                .bindWith(this);
        Events.subscribe(PlayerMoveEvent.class)
                .filter(event -> event.getPlayer().equals(this.player))
                .filter(EventFilters.ignoreSameBlockAndY())
                .handler(event -> event.setTo(event.getFrom()))
                .bindWith(this);

        this.player.teleport(this.position.toLocation());
        this.player.getInventory().clear();
        this.player.setWalkSpeed(0.0F);

        Potions.apply(this.player, PotionEffectType.BLINDNESS, 2);

        PlayerProfile playerProfile = this.player.getPlayerProfile();
        playerProfile.setProperty(new ProfileProperty("textures", Skins.RANDOM_SKIN_ONE.getTextures().getValue(), Skins.RANDOM_SKIN_ONE.getTextures().getSignature()));
        this.player.setPlayerProfile(playerProfile);
        ((CraftServer) Bukkit.getServer()).getHandle().moveToWorld(((CraftPlayer) this.player).getHandle(), 0, true, this.player.getLocation(), true);
    }

    @Override
    public void onEnd() {
        Potions.removeAll(this.player);
        this.player.playSound(this.player.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 15, 15);
    }
}
