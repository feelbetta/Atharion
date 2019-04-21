package com.atharion.lobby.commands;

import com.atharion.commons.lang.Lang;
import com.atharion.lobby.AtharionLobby;
import com.destroystokyo.paper.ParticleBuilder;
import com.google.common.collect.ImmutableSet;
import com.sllibrary.bukkit.Commands;
import com.sllibrary.bukkit.Schedulers;
import com.sllibrary.bukkit.scheduler.Task;
import com.sllibrary.bukkit.serialize.Point;
import com.sllibrary.bukkit.terminable.Terminable;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

public class SpawnPointCommand implements Terminable {


    private static final Set<String> AVAILABLE_ARGUMENTS = ImmutableSet.of(
            "add", "a",
            "remove", "rem", "r",
            "show", "s"
    );

    private boolean isShowing;
    private Task task;

    public SpawnPointCommand(@Nonnull AtharionLobby atharionLobby) {
        this.bindWith(Objects.requireNonNull(atharionLobby, "atharionLobby"));
        Commands.create()
                .assertPlayer(Lang.COMMAND_INVALID_STATE.toString())
                .assertOp(Lang.COMMAND_NO_PERMISSION.toString())
                .assertUsage("<option>", "&6/spawnpoint &f<add|remove|show>")
                .assertArgument(0, s -> AVAILABLE_ARGUMENTS.contains(s.toLowerCase()), Lang.COMMAND_INVALID_ARGUMENTS.toString())
                .handler(context -> {

                    String option = context.rawArg(0).toLowerCase();

                    Point point = Point.of(context.sender().getLocation());

                    switch (option) {
                        case "add":
                        case "a":
                            if (this.isSpawnPoint(atharionLobby, point)) {
                                context.reply("&4This is already a spawn point.");
                                return;
                            }
                            atharionLobby.getSpawnPoints().add(Point.of(context.sender().getLocation()));
                            context.reply("&fYou have added your location as a spawn point.");
                            break;
                        case "remove":
                        case "rem":
                        case "r":
                            if (!this.isSpawnPoint(atharionLobby, point)) {
                                context.reply("&4This is not a spawnpoint.");
                                return;
                            }
                            this.getSpawnPoint(atharionLobby, context.sender().getLocation().getBlock()).ifPresent(atharionLobby.getSpawnPoints()::remove);
                            context.reply("&fYou have removed your location as a spawn point.");
                            break;
                        case "show":
                        case "s":
                            this.isShowing = !this.isShowing;
                            if (!this.isShowing) {
                                this.isShowing = false;
                                this.task.stop();
                                context.reply("&fYou are no longer viewing spawn points.");
                                return;
                            }
                            this.task = Schedulers.sync()
                                    .runRepeating(() -> {
                                        atharionLobby.getSpawnPoints().forEach(point1 -> {

                                            IntStream.range(0, 20)
                                                    .mapToObj(value -> {
                                                        return point1.toLocation().clone().add(0, value, 0);
                                                    }).forEach(location -> {
                                                        new ParticleBuilder(Particle.HEART)
                                                                .location(location)
                                                                .count(5)
                                                                .allPlayers()
                                                                .spawn();

                                                    });
                                        });
                                    }, 0, 20);
                            context.reply("&fYou are now viewing spawn points.");

                    }
                }).register("spawnpoint", "sp");
    }

    private Optional<Point> getSpawnPoint(@Nonnull AtharionLobby atharionLobby, @Nonnull Block block) {
        return atharionLobby.getSpawnPoints().stream()
                .filter(point -> point.toLocation().getBlock().equals(block))
                .findFirst();

    }

    private boolean isSpawnPoint(@Nonnull AtharionLobby atharionLobby, @Nonnull Point point) {
        Block block = point.toLocation().getBlock();
        return this.getSpawnPoint(atharionLobby, block).isPresent();
    }

    @Override
    public void close() {
        if (this.task != null && !this.task.isClosed()) {
            this.task.stop();
        }
    }
}
