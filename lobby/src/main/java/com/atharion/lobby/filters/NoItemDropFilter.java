package com.atharion.lobby.filters;

import com.atharion.commons.eventbus.EventBus;
import com.atharion.commons.filters.Filter;
import com.sllibrary.bukkit.Services;
import com.sllibrary.bukkit.event.filter.EventHandlers;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerDropItemEvent;

import java.util.function.Consumer;

public class NoItemDropFilter implements Filter<World> {

    @Override
    public Consumer<World> filter() {
        return world -> Services.load(EventBus.class)
                .subscribe(PlayerDropItemEvent.class,
                        playerDropItemEvent -> {
                            Player player = playerDropItemEvent.getPlayer();
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
