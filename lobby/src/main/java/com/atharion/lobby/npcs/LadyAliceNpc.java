package com.atharion.lobby.npcs;

import com.atharion.commons.AtharionCommons;
import com.atharion.commons.lang.Lang;
import com.atharion.commons.npc.AtharionNpc;
import com.atharion.commons.npc.NPCType;
import com.atharion.commons.npc.traits.AnimatedHeadTrait;
import com.atharion.commons.npc.traits.ParticleTrait;
import com.atharion.commons.skins.AnimatedGameSkin;
import com.atharion.lobby.guis.RealmSelectorGui;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sllibrary.bukkit.hologram.individual.HologramLine;
import com.sllibrary.bukkit.npc.CitizensNpcFactory;
import com.sllibrary.bukkit.text.Text;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

public class LadyAliceNpc extends AtharionNpc {

    public LadyAliceNpc(@Nonnull AtharionCommons atharionCommons) {
        super(atharionCommons);
    }

    @Nonnull
    @Override
    public AnimatedGameSkin getSkin() {
        return AnimatedGameSkin.of(
                Skins.LADY_ALICE.getTextures()
        );
    }

    @Nonnull
    @Override
    public String getName() {
        return "&6Lady Alice";
    }

    @Nonnull
    @Override
    public List<HologramLine> getDisplayName() {
        return Lists.newArrayList(
                HologramLine.fixed(this.getName())
        );
    }

    @Nonnull
    @Override
    public NPCType getType() {
        return NPCType.HELP;
    }

    @Nonnull
    @Override
    public Set<Trait> getTraits() {
        ParticleTrait particleTrait = new ParticleTrait(this);
        particleTrait.addParticle(Particle.HEART, 1);

        AnimatedHeadTrait animatedHeadTrait = new AnimatedHeadTrait();
        animatedHeadTrait.setAnimatedGameSkin(this.getSkin());
        animatedHeadTrait.setTicks(3);
        return Sets.newHashSet(
                particleTrait,
                animatedHeadTrait
        );
    }

    @Nonnull
    @Override
    public BiConsumer<CitizensNpcFactory.ClickType, Player> interact() {
        return (clickType, player) -> {
            switch (clickType) {
                case INTERACT:
                    new RealmSelectorGui(player).open();
                    break;
                case HIT:
                    player.sendMessage(Text.colorize(this.getName() + Lang.COLON + " &fYou don't hit women, do you?"));
            }
        };
    }

    @Override
    public void update() {

    }
}
