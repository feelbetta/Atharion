package com.atharion.lobby.introduction.extras;

import com.atharion.commons.npc.AtharionEntity;
import com.sllibrary.bukkit.terminable.Terminable;
import com.sllibrary.bukkit.text.Text;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.Controllable;
import net.citizensnpcs.trait.HorseModifiers;
import net.citizensnpcs.trait.MountTrait;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.UUID;

public class HorseNpc extends IntroductionNpc {

    private Player player;

    public HorseNpc(@Nonnull Location origin, @Nonnull Location targetLocation, Player player) {
        super(origin, targetLocation);
        this.player = player;
    }

    @Override
    public String getName() {
        return AtharionEntity.randomName();
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.HORSE;
    }

    @Override
    public void setupMeta(@Nonnull NPC npc) {
        npc.data().set(NPC.COLLIDABLE_METADATA, false);
        npc.data().set(NPC.NAMEPLATE_VISIBLE_METADATA, false);

        npc.setProtected(true);

        npc.addTrait(HorseModifiers.class);
        npc.addTrait(Controllable.class);
        npc.addTrait(MountTrait.class);

        HorseModifiers horseModifiers = npc.getTrait(HorseModifiers.class);
        horseModifiers.setCarryingChest(true);
        horseModifiers.setArmor(new ItemStack(Material.IRON_BARDING));
        horseModifiers.setColor(Horse.Color.CHESTNUT);
        horseModifiers.setStyle(Horse.Style.NONE);

        npc.getNavigator().getDefaultParameters()
                .stuckAction(null)
                .avoidWater(true)
                .useNewPathfinder(true)
                .baseSpeed(1.85F)
                .speedModifier(1.2F)
                .range((float) this.origin.distance(this.targetLocation) + 5);
    }

    @Override
    public void navigate() {
        super.navigate();
        this.npc.getTrait(Controllable.class).mount(this.player);
    }

    @Override
    public void close() {
        super.close();
        this.player = null;
    }
}
