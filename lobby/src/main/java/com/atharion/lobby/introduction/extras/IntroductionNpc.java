package com.atharion.lobby.introduction.extras;

import com.sllibrary.bukkit.terminable.Terminable;
import com.sllibrary.bukkit.text.Text;
import com.sllibrary.bukkit.utils.Players;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.UUID;

public abstract class IntroductionNpc implements Terminable {

    protected final Location origin, targetLocation;

    protected NPC npc;

    public IntroductionNpc(@Nonnull Location origin, @Nonnull Location targetLocation) {
        this.origin = Objects.requireNonNull(origin, "origin");
        this.targetLocation = Objects.requireNonNull(targetLocation, "targetLocation");
    }

    public abstract String getName();

    public abstract EntityType getEntityType();

    public abstract void setupMeta(@Nonnull NPC npc);

    public void navigate() {
        this.npc = CitizensAPI.getNPCRegistry().createNPC(this.getEntityType(), Text.colorize(this.getName()));
        this.setupMeta(this.npc);
        this.npc.spawn(this.origin);
        this.npc.getNavigator().setTarget(this.targetLocation);
    }

    public boolean hasReachedTarget() {
        return this.npc != null && this.npc.isSpawned() && !this.npc.getNavigator().isNavigating();
    }

    public Location getCurrentLocation() {
        return this.npc == null ? null : this.npc.getEntity().getLocation();
    }

    @Override
    public void close() {
        Particle.CLOUD.builder()
                .offset(0.3D, this.npc.getEntity().getHeight() - 0.3, 0.3D)
                .allPlayers()
                .location(this.npc.getStoredLocation())
                .extra(0)
                .count(2)
                .spawn();
        this.npc.destroy();
        this.npc = null;
    }
}
