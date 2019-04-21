package com.atharion.lobby.filters;

import com.atharion.commons.eventbus.EventBus;
import com.atharion.commons.filters.Filter;
import com.sllibrary.bukkit.Services;
import com.sllibrary.bukkit.event.filter.EventHandlers;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;

import java.util.function.Consumer;

public class NoInventoryInteractionFilter implements Filter<World> {

    @Override
    public Consumer<World> filter() {
        return world -> {
            Services.load(EventBus.class)
                    .subscribe(InventoryClickEvent.class,
                            inventoryClickEvent -> {
                                Player player = (Player) inventoryClickEvent.getWhoClicked();
                                return player.getWorld().equals(world) && !player.isOp();
                            },
                            inventoryClickEvent -> inventoryClickEvent.setCancelled(true)
                    );
        };
    }

    @Override
    public boolean tickable() {
        return false;
    }
}
