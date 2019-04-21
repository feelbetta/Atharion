package com.atharion.lobby.filters;

import com.atharion.commons.eventbus.EventBus;
import com.atharion.commons.filters.Filter;
import com.sllibrary.bukkit.Services;
import com.sllibrary.bukkit.event.filter.EventHandlers;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

import java.util.function.Consumer;

public class NoPickupItemFilter implements Filter<World> {

    @Override
    public Consumer<World> filter() {
        return world -> Services.load(EventBus.class)
                .subscribe(EntityPickupItemEvent.class,
                        entityPickupItemEvent -> {
                            return entityPickupItemEvent.getEntity() instanceof Player && entityPickupItemEvent.getEntity().getWorld().equals(world) && !entityPickupItemEvent.getEntity().isOp();
                        },
                        EventHandlers.cancel()
                );
    }

    @Override
    public boolean tickable() {
        return false;
    }
}
