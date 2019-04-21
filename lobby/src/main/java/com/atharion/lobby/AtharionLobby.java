package com.atharion.lobby;

import com.atharion.commons.*;
import com.atharion.commons.filters.world.WorldSettings;
import com.atharion.commons.lang.Lang;
import com.atharion.commons.npc.NpcRepository;
import com.atharion.commons.scenes.Scene;
import com.atharion.commons.states.machines.Machine;
import com.atharion.commons.states.machines.TimedMachine;
import com.atharion.commons.util.Locations;
import com.atharion.commons.util.Potions;
import com.atharion.commons.util.RandomPositions;
import com.atharion.lobby.commands.IntroBoxSpawnCommand;
import com.atharion.lobby.commands.IntroNpcSpawnCommand;
import com.atharion.lobby.commands.NpcCommand;
import com.atharion.lobby.commands.SpawnPointCommand;
import com.atharion.lobby.filters.*;
import com.atharion.lobby.gadgets.handler.GadgetHandler;
import com.atharion.lobby.introduction.machine.IntroductionMachine;
import com.atharion.lobby.introduction.states.ComaState;
import com.atharion.lobby.introduction.states.GreetingState;
import com.atharion.lobby.introduction.states.RideHorseState;
import com.atharion.lobby.npcs.LadyAliceNpc;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sllibrary.bukkit.Commands;
import com.sllibrary.bukkit.Events;
import com.sllibrary.bukkit.Services;
import com.sllibrary.bukkit.bossbar.BossBar;
import com.sllibrary.bukkit.bossbar.BossBarFactory;
import com.sllibrary.bukkit.internal.SLLibraryImplementationPlugin;
import com.sllibrary.bukkit.item.ItemStacks;
import com.sllibrary.bukkit.plugin.ExtendedJavaPlugin;
import com.sllibrary.bukkit.plugin.ap.Plugin;
import com.sllibrary.bukkit.serialize.Point;
import com.sllibrary.bukkit.serialize.Position;
import com.sllibrary.bukkit.text.Text;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@SLLibraryImplementationPlugin
@Plugin(
        name = "AtharionLobby",
        authors = "slees",

        version = "beta",

        hardDepends = "SLLibrary"
)
public class AtharionLobby extends ExtendedJavaPlugin {

    @Getter
    private GadgetHandler gadgetHandler;

    @Getter
    private BossBar bossBar;

    @Getter
    private Set<Point> spawnPoints = new HashSet<>();

    @Getter @Setter
    private Location npcSpawn;

    @Getter
    private NpcRepository npcRepository;

    @Getter @Setter
    private Point box;

    @Override
    protected void enable() {
        this.saveDefaultConfig();
        this.npcRepository = new NpcRepository(this);
        this.npcRepository.register(new LadyAliceNpc(JavaPlugin.getPlugin(AtharionCommons.class)));
        this.npcRepository.adaptProperties();
        this.getConfig().getStringList("spawnpoints").stream()
                .map(Locations::deserialize)
                .filter(Objects::nonNull)
                .map(Point::of)
                .forEach(this.spawnPoints::add);
        this.npcSpawn = Locations.deserialize(this.getConfig().getString("npc"));
        if (this.getConfig().getString("box") != null) {
            this.box = Point.of(Locations.deserialize(this.getConfig().getString("box")));
        }
        this.registerCommands();
        this.registerSettings();
        this.registerGadgets();
        this.bossBar = Services.load(BossBarFactory.class).newBossBar();
        bossBar.title("&eTurn your account &fPlatinum&e for only &6$7.99&e!");

        Events.subscribe(PlayerChatEvent.class)
                .handler(playerChatEvent -> {

                    if (playerChatEvent.getMessage().equals("intro")) {
                        Machine machine = new IntroductionMachine(Lists.newArrayList(
                                new ComaState(playerChatEvent.getPlayer(), this.box.getPosition()),
                                new RideHorseState(playerChatEvent.getPlayer(), RandomPositions.select(this.spawnPoints.stream().map(Point::getPosition).collect(Collectors.toSet())), Position.of(playerChatEvent.getPlayer().getWorld().getSpawnLocation())),
                                new GreetingState(Position.of(this.npcSpawn), Position.of(this.npcSpawn.getWorld().getSpawnLocation()), playerChatEvent.getPlayer())
                        ), playerChatEvent.getPlayer());
                        machine.start();
                    }
                });

        Commands.create()
                .assertPlayer(Lang.COMMAND_INVALID_STATE.toString())
                .assertOp(Lang.COMMAND_NO_PERMISSION.toString())
                .handler(context -> {
                    Potions.removeAll(context.sender());
                    context.sender().sendActionBar("SLUUUURRRPP");
                }).register("milk");

        Commands.create()
                .assertOp(Lang.COMMAND_NO_PERMISSION.toString())
                .assertUsage("<message>", "&4Usage: /broadcastraw <message>")
                .handler(context -> Bukkit.broadcastMessage(context.args().asList().stream().map(Text::colorize).collect(Collectors.joining(" ")).replace("\\n", "\n")))
                .register("broadcastraw");

        new NpcCommand(this.npcRepository);
    }

    @Override
    protected void disable() {
        this.getConfig().set("spawnpoints", this.spawnPoints.stream()
                .map(Point::toLocation)
                .map(Locations::serialize)
                .collect(Collectors.toList()));
        this.getConfig().set("npc", Locations.serialize(this.npcSpawn));
        this.getConfig().set("box", Locations.serialize(this.box.toLocation()));
        this.saveConfig();
    }

    private void registerCommands() {
        new SpawnPointCommand(this);
        new IntroNpcSpawnCommand(this);
        new IntroBoxSpawnCommand(this);
    }

    private void registerGadgets() {
        this.gadgetHandler = new GadgetHandler(this);
    }

    private void registerSettings() {
        WorldSettings worldSettings = new WorldSettings("world");
        worldSettings.addFilter(new GlobalMusicFilter(this, Arrays.asList("lobbytheme", "lobbytheme2", "lobbytheme3")));
        worldSettings.addFilter(new NoBlockInteractionFilter());
        worldSettings.addFilter(new NoChatFilter());
        worldSettings.addFilter(new NoDamageFilter());
        worldSettings.addFilter(new NoFoodLevelChangeFilter());
        worldSettings.addFilter(new NoInventoryInteractionFilter());
        worldSettings.addFilter(new NoItemDropFilter());
        worldSettings.addFilter(new NoPickupItemFilter());
        worldSettings.addFilter(new NoVisibilityFilter());

        worldSettings.initialize(this);
    }
}
