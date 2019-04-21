package com.atharion.lobby.filters;

import com.atharion.commons.eventbus.EventBus;
import com.atharion.commons.filters.Filter;
import com.atharion.lobby.AtharionLobby;
import com.sllibrary.bukkit.Services;
import com.sllibrary.bukkit.event.filter.EventHandlers;
import com.sllibrary.bukkit.scheduler.Ticks;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class NoVisibilityFilter implements Filter<World> {

    private static final String SCOREBOARD_TEAM_NAME = "invisibles";

    private static final int DURATION = (int) Ticks.from(1, TimeUnit.HOURS);

    private Team invisibles;

    public NoVisibilityFilter() {
        if ((invisibles = Bukkit.getScoreboardManager().getMainScoreboard().getTeam(SCOREBOARD_TEAM_NAME)) != null) {
            invisibles.unregister();
        }
        this.invisibles = Bukkit.getScoreboardManager().getMainScoreboard().registerNewTeam(SCOREBOARD_TEAM_NAME);
        this.invisibles.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        this.invisibles.setCanSeeFriendlyInvisibles(true);
    }

    @Override
    public Consumer<World> filter() {
        return world -> {
            Services.load(EventBus.class)
                    .subscribe(PlayerJoinEvent.class,
                            playerJoinEvent -> playerJoinEvent.getPlayer().getWorld().equals(world),
                            playerJoinEvent -> {
                                Player player = playerJoinEvent.getPlayer();
                                JavaPlugin.getPlugin(AtharionLobby.class).getBossBar().addPlayer(player);
                                player.teleport(world.getSpawnLocation());
                                player.setGameMode(GameMode.ADVENTURE);
                                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, NoVisibilityFilter.DURATION, 0, true, false), true);
                                this.invisibles.addEntry(playerJoinEvent.getPlayer().getName());
                            }
                    );
            
            Services.load(EventBus.class)
                    .subscribe(PlayerQuitEvent.class,
                            playerQuitEvent -> {
                                Player player = playerQuitEvent.getPlayer();
                                return player.getWorld().equals(world) && this.invisibles.hasEntry(playerQuitEvent.getPlayer().getName());
                            },
                            playerQuitEvent -> this.invisibles.removeEntry(playerQuitEvent.getPlayer().getName())
                    );
            
        };
    }

    @Override
    public boolean tickable() {
        return false;
    }

    @Override
    public void close() {
        this.invisibles.unregister();
    }
}
