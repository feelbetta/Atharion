package com.atharion.lobby.introduction.states;

import com.atharion.commons.states.ConditionalState;
import com.atharion.lobby.conversations.ConversationLang;
import com.atharion.lobby.introduction.extras.GreetingNpc;
import com.atharion.lobby.lang.Lang;
import com.sllibrary.bukkit.Events;
import com.sllibrary.bukkit.event.filter.EventFilters;
import com.sllibrary.bukkit.serialize.Position;
import com.sllibrary.bukkit.terminable.TerminableConsumer;
import com.sllibrary.bukkit.utils.CollectionUtils;
import com.sllibrary.bukkit.utils.Players;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import javax.annotation.Nonnull;
import java.util.Objects;

public class GreetingState extends ConditionalState {

    private final GreetingNpc greetingNpc;

    private final Player player;

    private int current = 0;
    private int second = 0;

    public GreetingState(@Nonnull Position origin, @Nonnull Position targetLocation, @Nonnull Player player) {
        this.greetingNpc = new GreetingNpc(origin.toLocation(), targetLocation.toLocation());
        this.player = Objects.requireNonNull(player, "player");
    }

    @Override
    public boolean getCondition() {
        return this.greetingNpc.hasReachedTarget() && CollectionUtils.isLastElement(ConversationLang.INTRODUCTION_GREETER_WELCOME.getText(), ConversationLang.INTRODUCTION_GREETER_WELCOME.get(this.current));
    }

    @Override
    public void onUpdate() {
        this.player.closeInventory();
        if (!this.greetingNpc.hasReachedTarget()) {
            return;
        }
        this.second++;
        if (this.second % 4 != 0) {
            return;
        }
        this.player.sendMessage(ConversationLang.INTRODUCTION_GREETER_WELCOME.get(this.current));
        this.current++;
        this.player.playSound(this.player.getLocation(), Sound.BLOCK_NOTE_CHIME, 2, 15);
    }

    @Override
    public void onStart() {
        this.player.getInventory().setHeldItemSlot(2);
        Events.subscribe(PlayerMoveEvent.class)
                .filter(event -> event.getPlayer().equals(this.player))
                .filter(EventFilters.ignoreSameBlockAndY())
                .handler(event -> event.setTo(event.getFrom()))
                .bindWith(this);
        Events.subscribe(PlayerQuitEvent.class)
                .handler(event -> {
                    this.greetingNpc.closeAndReportException();
                    this.onEnd();
                });

        this.greetingNpc.bindWith(this);
        this.greetingNpc.navigate();
    }

    @Override
    public void onEnd() {
        Players.resetWalkSpeed(this.player);
    }
}
