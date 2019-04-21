package com.atharion.lobby.filters;

import com.atharion.commons.eventbus.EventBus;
import com.atharion.commons.filters.Filter;
import com.sllibrary.bukkit.Services;
import com.sllibrary.bukkit.event.filter.EventHandlers;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.function.Consumer;

public class NoBlockInteractionFilter implements Filter<World> {

    @Override
    public Consumer<World> filter() {
        return world -> Services.load(EventBus.class)
                .subscribe(PlayerInteractEvent.class,
                        playerInteractEvent -> {
                            Player player = playerInteractEvent.getPlayer();
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
