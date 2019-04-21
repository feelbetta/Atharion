package com.atharion.lobby.filters;

import com.atharion.commons.eventbus.EventBus;
import com.atharion.commons.filters.Filter;
import com.sllibrary.bukkit.Services;
import com.sllibrary.bukkit.event.filter.EventHandlers;
import org.bukkit.World;
import org.bukkit.event.entity.FoodLevelChangeEvent;

import java.util.function.Consumer;

public class NoFoodLevelChangeFilter implements Filter<World> {

    @Override
    public Consumer<World> filter() {
        return world -> Services.load(EventBus.class)
                .subscribe(FoodLevelChangeEvent.class,
                        foodLevelChangeEvent -> foodLevelChangeEvent.getEntity().getWorld().equals(world),
                        EventHandlers.cancel()
                );
    }

    @Override
    public boolean tickable() {
        return false;
    }
}
