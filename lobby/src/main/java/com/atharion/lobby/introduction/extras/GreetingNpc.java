package com.atharion.lobby.introduction.extras;

import com.atharion.lobby.npcs.Skins;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.LookClose;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import javax.annotation.Nonnull;
import java.util.UUID;

public class GreetingNpc extends IntroductionNpc {

    public GreetingNpc(@Nonnull Location origin, @Nonnull Location targetLocation) {
        super(origin, targetLocation);
    }

    @Override
    public String getName() {
        return "&6Lady Alice";
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.PLAYER;
    }

    @Override
    public boolean hasReachedTarget() {
        return this.npc.getEntity().getLocation().distanceSquared(this.targetLocation) < 9;
    }

    @Override
    public void setupMeta(@Nonnull NPC npc) {
        npc.data().set(NPC.COLLIDABLE_METADATA, false);
        npc.data().set(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_METADATA, Skins.LADY_ALICE.getTextures().getValue());
        npc.data().set(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_SIGN_METADATA, Skins.LADY_ALICE.getTextures().getSignature());
        npc.data().set("cached-skin-uuid-name", "null");
        npc.data().set("player-skin-name", "null");
        npc.data().set("cached-skin-uuid", UUID.randomUUID().toString());
        npc.data().set(NPC.PLAYER_SKIN_USE_LATEST, false);
        npc.setProtected(true);

        npc.addTrait(LookClose.class);

        LookClose lookClose = npc.getTrait(LookClose.class);
        lookClose.setRange(10);
        lookClose.setRealisticLooking(true);
        lookClose.lookClose(true);

        npc.getNavigator().getDefaultParameters()
                .avoidWater(true)
                .useNewPathfinder(true)
                .range((float) this.origin.distance(this.targetLocation) + 10);
    }
}
