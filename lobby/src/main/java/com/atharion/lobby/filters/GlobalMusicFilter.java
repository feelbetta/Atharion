package com.atharion.lobby.filters;

import com.atharion.commons.eventbus.EventBus;
import com.atharion.commons.filters.Filter;
import com.atharion.commons.music.decoders.nbs.NBSDecoder;
import com.atharion.commons.music.MusicProvider;
import com.atharion.commons.music.tunes.players.TunePlayer;
import com.sllibrary.bukkit.Services;
import org.bukkit.World;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class GlobalMusicFilter implements Filter<World> {

    public static TunePlayer tunePlayer;

    public GlobalMusicFilter(Plugin plugin, List<String> songs) {
        this.tunePlayer = Services.load(MusicProvider.class)
                .newIndividualPlayer(songs.stream().map(s -> NBSDecoder.parse(new File(plugin.getDataFolder(), s + ".nbs"))).collect(Collectors.toList()));
        this.tunePlayer.setPlaying(true);
        this.tunePlayer.getProperties().setLooping(true);
    }

    @Override
    public Consumer<World> filter() {
        return world -> {
            EventBus eventBus = Services.load(EventBus.class);
/*            eventBus
                    .subscribe(PlayerJoinEvent.class,
                            playerJoinEvent -> Services.load(MusicProvider.class)
                                                    .setTunePlayer(playerJoinEvent.getPlayer(), this.tunePlayer)
                    );*/

            eventBus
                    .subscribe(PlayerQuitEvent.class,
                            playerQuitEvent -> Services.load(MusicProvider.class)
                                    .removeTunePlayer(playerQuitEvent.getPlayer())
                    );
/*            eventBus.subscribe(SongEndEvent.class,
                            songEndEvent -> {
                                SongPlayer songPlayer = songEndEvent.getSongPlayer();
                                Bukkit.broadcastMessage("Ended.");
                                if (!songPlayer.equals(this.songPlayer)) {
                                    Bukkit.broadcastMessage("Doesnt equal");
                                    return;
                                }
                                Bukkit.broadcastMessage("Does equal :D");
                                songPlayer.setAutoDestroy(false);
                                Schedulers.sync()
                                        .runLater(() -> songPlayer.setTick((short) 0),
                                                Ticks.from(10, TimeUnit.SECONDS));
                            }
                    );*/
        };
    }

    @Override
    public boolean tickable() {
        return false;
    }
}
