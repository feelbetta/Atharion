package com.atharion.lobby.filters;

import com.atharion.commons.eventbus.EventBus;
import com.atharion.commons.filters.Filter;
import com.sllibrary.bukkit.Services;
import com.sllibrary.bukkit.event.filter.EventHandlers;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.function.Consumer;

public class NoChatFilter implements Filter<World> {

    @Override
    public Consumer<World> filter() {
        return world -> Services.load(EventBus.class)
                .subscribe(AsyncPlayerChatEvent.class,
                        asyncPlayerChatEvent -> {
                            Player player = asyncPlayerChatEvent.getPlayer();
                            return player.getWorld().equals(world) && !player.isOp();
                        },
                        EventHandlers.cancel()
                );
    }

    @Override
    public boolean tickable() {
        return false;
    }
}
