package com.atharion.lobby.gadgets.types;

import com.atharion.commons.eventbus.EventBus;
import com.atharion.lobby.AtharionLobby;
import com.atharion.lobby.gadgets.Gadget;
import com.sllibrary.bukkit.Schedulers;
import com.sllibrary.bukkit.Services;
import com.sllibrary.bukkit.item.ItemStacks;
import com.sllibrary.bukkit.metadata.ExpiringValue;
import com.sllibrary.bukkit.metadata.Metadata;
import com.sllibrary.bukkit.metadata.MetadataKey;
import com.sllibrary.bukkit.metadata.MetadataMap;
import com.sllibrary.bukkit.scheduler.Ticks;
import com.sllibrary.bukkit.serialize.BlockPosition;
import com.sllibrary.bukkit.text.Text;
import com.sllibrary.bukkit.utils.Players;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BombGadget implements Gadget {

    private static final MetadataKey<Player> COOLDOWN = MetadataKey.create("gadget-bomb-cooldown", Player.class);

    private static final Set<Material> DISALLOWED_MATERIALS = EnumSet.of(
            Material.AIR,

            Material.SIGN,
            Material.SIGN_POST,
            Material.WALL_SIGN,

            Material.PORTAL,
            Material.ENDER_PORTAL,

            Material.ARMOR_STAND,

            Material.BANNER,
            Material.STANDING_BANNER,
            Material.WALL_BANNER,

            Material.CAULDRON,
            Material.CHEST,
            Material.TRAPPED_CHEST,

            Material.WATER,
            Material.STATIONARY_WATER,

            Material.LAVA,
            Material.STATIONARY_LAVA,

            Material.FIRE,

            Material.SOIL,

            Material.ITEM_FRAME,

            Material.SKULL,

            Material.BEDROCK,
            Material.BARRIER
    );

    private final Map<Entity, List<WorldBlock>> damagedBlocks = new HashMap<>();

    private static final int EXPLOSION_RADIUS = 2;
    private static final double VECTOR_LENGTH = 4.20;
    private static final double VECTOR_HEIGHT_DEVIATION = 1.4;

    private final Set<FallingBlock> fallingBlocks = new HashSet<>();

    public BombGadget(@Nonnull AtharionLobby atharionLobby) {
        Objects.requireNonNull(atharionLobby, "plugin instance");
        this.bindWith(atharionLobby);
        Services.load(EventBus.class)
                .subscribe(EntityExplodeEvent.class,
                        entityExplodeEvent -> !entityExplodeEvent.isCancelled(),
                        entityExplodeEvent -> {
                            entityExplodeEvent.setCancelled(true);

                            Location location = entityExplodeEvent.getLocation();

                            Set<Block> affectedBlocks = this.getNearbyBlocks(location, BombGadget.EXPLOSION_RADIUS);

                            if (affectedBlocks.isEmpty()) {
                                return;
                            }

                            location.getWorld().getNearbyEntities(location, BombGadget.EXPLOSION_RADIUS, 2, BombGadget.EXPLOSION_RADIUS).stream()
                                    .filter(Player.class::isInstance)
                                    .map(Player.class::cast)
                                    .forEach(player -> {
                                        player.setFireTicks((int) Ticks.from(5, TimeUnit.SECONDS));
                                        player.setVelocity(new Vector(0, 2.5, 0));
                                    });
                            location.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, location, 1);
                            location.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 2, 25);


                            List<WorldBlock> initialBlocks = new ArrayList<>(affectedBlocks).stream()
                                    .map(block -> {
                                        BlockPosition blockPosition = BlockPosition.of(block);
                                        MaterialData materialData = block.getState().getData();
                                        return new RegularBlock(blockPosition, materialData);

                                    }).sorted(Comparator.comparing(regularBlock -> regularBlock.getBlockPosition().getY()))
                                    .collect(Collectors.toList());

                            this.damagedBlocks.put(entityExplodeEvent.getEntity(), initialBlocks);

                            affectedBlocks.forEach(block -> {
                                FallingBlock fallingBlock = block.getWorld().spawnFallingBlock(block.getLocation(), block.getState().getData());
                                fallingBlock.setDropItem(false);
                                fallingBlock.setHurtEntities(false);
                                fallingBlock.setSilent(true);
                                fallingBlock.setInvulnerable(true);

                                Vector blockDirection = block.getLocation().toVector().subtract(location.toVector());

                                double divide = BombGadget.VECTOR_LENGTH / blockDirection.lengthSquared();
                                blockDirection.divide(new Vector(divide, divide, divide));
                                blockDirection.setY(Math.abs(blockDirection.getY()) + BombGadget.VECTOR_HEIGHT_DEVIATION);

                                fallingBlock.setVelocity(blockDirection.normalize());

                                this.fallingBlocks.add(fallingBlock);
                                block.setTypeIdAndData(Material.BARRIER.getId(), (byte) 0, false);

                                fallingBlock.setFireTicks(9999);

                            });

                            Iterator<WorldBlock> regeneration = initialBlocks.iterator();
                            Schedulers.sync()
                                    .runRepeating(task -> {
                                        if (regeneration.hasNext()) {
                                            WorldBlock worldBlock = regeneration.next();
                                            worldBlock.restore(false);
                                            regeneration.remove();
                                            return;
                                        }
                                        task.stop();
                                        this.damagedBlocks.remove(entityExplodeEvent.getEntity());
                                        this.fallingBlocks.stream()
                                                .filter(Entity::isValid)
                                                .forEach(Entity::remove);
                                        this.fallingBlocks.clear();
                                        location.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, location.add(0, 3, 0), 3);
                                        location.getWorld().playSound(location, Sound.ENTITY_VILLAGER_YES, 3, 1);
                                    }, Ticks.from(8, TimeUnit.SECONDS), 3);
                        });

        Services.load(EventBus.class)
                .subscribe(EntityChangeBlockEvent.class,
                        entityChangeBlockEvent -> !entityChangeBlockEvent.isCancelled() && this.fallingBlocks.contains(entityChangeBlockEvent.getEntity()),
                        entityChangeBlockEvent -> {
                            entityChangeBlockEvent.setCancelled(true);
                            entityChangeBlockEvent.getBlock().getWorld().playEffect(entityChangeBlockEvent.getBlock().getLocation(), Effect.STEP_SOUND, ((FallingBlock)entityChangeBlockEvent.getEntity()).getBlockId());
                            this.fallingBlocks.remove(entityChangeBlockEvent.getEntity());
                        });
    }

    @Nonnull
    @Override
    public String getName() {
        return "Bomb";
    }

    @Nonnull
    @Override
    public ItemStack getIdentifier() {
        return ItemStacks.of(Material.TNT)
                .name("&cBomb")
                .lore("&8Gadget")
                .lore(" ")
                .lore("&fRight Click&e to throw a bomb!")
                .lore(" ")
                .lore("&f≻ &eAvailable every: &f3s&e.")
                .build();
    }

    @Override
    public int getSlot() {
        return 6;
    }

    @Override
    public boolean canUse(@Nonnull Player player) {
        MetadataMap metadata = Metadata.provideForPlayer(player);
        Optional<Player> cooldown = metadata.get(COOLDOWN);
        if (!cooldown.isPresent()) {
            return true;
        }
        player.sendMessage(Text.colorize("&4This gadget is only available every 3 seconds."));
        return false;
    }

    @Override
    public void onUse(@Nonnull Player player) {
        Metadata.provideForPlayer(player)
                .put(BombGadget.COOLDOWN, ExpiringValue.of(player, 3, TimeUnit.SECONDS));
        Location location = player.getLocation();
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SHOOT, 2, 15);
        TNTPrimed tntPrimed = (TNTPrimed) location.getWorld().spawnEntity(location, EntityType.PRIMED_TNT);
        tntPrimed.setYield(0);
        tntPrimed.setVelocity(location.getDirection().multiply(2.5));

        Schedulers.sync()
                .runRepeating(task -> {
                    if (tntPrimed.isValid()) {
                        Location entityLocation = tntPrimed.getLocation();
                        Players.playSound(entityLocation, Sound.ENTITY_TNT_PRIMED);
                        Players.spawnParticle(entityLocation, tntPrimed.isOnGround() ? Particle.VILLAGER_ANGRY : Particle.LAVA, 3);
                        return;
                    }
                    task.stop();
                }, 1, 1);
    }

    @Nonnull
    private Set<Block> getNearbyBlocks(@Nonnull Location initial, int radius) {
        Objects.requireNonNull(initial, "initial");
        if (radius <= 0) {
            return new HashSet<>();
        }
        Set<Block> blocks = new HashSet<>();
        IntStream.rangeClosed(-radius, radius)
                .forEach(x -> IntStream.rangeClosed(-radius, radius)
                        .forEach(y -> IntStream.rangeClosed(-radius, radius)
                                .filter(z -> {
                                    Block block = initial.getBlock().getRelative(x, y, z);
                                    return !BombGadget.DISALLOWED_MATERIALS.contains(block.getType()) && block.getRelative(BlockFace.UP).getType() != Material.FIRE;
                                })
                                .forEach(z -> blocks.add(initial.getBlock().getRelative(x, y, z)))));
        return blocks;
    }

    public interface WorldBlock {

        @Nonnull
        BlockPosition getBlockPosition();

        @Nonnull
        MaterialData getMaterialData();

        void restore(boolean fast);
    }

    @AllArgsConstructor @Getter
    public class RegularBlock implements WorldBlock {

        private BlockPosition blockPosition;
        private MaterialData materialData;

        @Override
        public void restore(boolean fast) {
            Block block = blockPosition.toBlock();
            block.setTypeIdAndData(this.materialData.getItemTypeId(), this.materialData.getData(), false);
            if (!fast) {
                block.getWorld().spawnParticle(Particle.BLOCK_CRACK, block.getLocation(), 10, block.getState().getData());
                block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getTypeId());
            }
        }
    }

    @Override
    public void close() throws Exception {
        this.damagedBlocks.values().stream()
                .flatMap(Collection::stream)
                .forEach(worldBlock -> worldBlock.restore(true));
        this.damagedBlocks.clear();
    }
}
