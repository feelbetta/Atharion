package com.atharion.lobby.guis;

import com.atharion.commons.util.Inventories;
import com.sllibrary.bukkit.Schedulers;
import com.sllibrary.bukkit.item.ItemStacks;
import com.sllibrary.bukkit.menu.Gui;
import com.sllibrary.bukkit.menu.Item;
import com.sllibrary.bukkit.menu.Slot;
import com.sllibrary.bukkit.menu.scheme.MenuScheme;
import com.sllibrary.bukkit.menu.scheme.StandardSchemeMappings;
import net.minecraft.server.v1_12_R1.ChatMessage;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.PacketPlayOutOpenWindow;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class RealmSelectorGui extends Gui {

    private static final MenuScheme BORDER = new MenuScheme(StandardSchemeMappings.STAINED_GLASS)
            .mask("111111111")
            .mask("000000000")
            .mask("000000000")
            .mask("111101111")

            .scheme(4, 4, 4, 4, 4, 4, 4, 4, 4)
            .scheme(4)
            .scheme(4)
            .scheme(4, 4, 4, 4, 4, 4, 4, 4);

    private static final Item BORDER_HIGHLIGHTED_PLACE_HOLDER = ItemStacks.of(Material.STAINED_GLASS_PANE)
            .data(0)
            .name("&eChoose a realm to &fplay&e!")
            .buildItem()
            .build();

    private static final Item REALM_INFORMATION = ItemStacks.of(Material.EMPTY_MAP)
            .name("&6&nAtharion Realms")
            .lore(" ")
            .lore("&eClick on the realm you would like to connect to.")
            .lore(" ")
            .lore("&f≻ &eRealms &fdo not&e have any &fadvantages&e or &ffeatured")
            .lore("&fcontent&e from one another.")
            .lore(" ")
            .lore("&f≻ &fALL &edata & gameplay is synchronized between realms.")
            .buildItem()
            .build();

    private static final Item REALM = ItemStacks.of(Material.BOOK)
            .name("&6Realm: &f1")
            .lore(" ")
            .lore("&f≻ &ePlayers: &f" + 30)
            .buildItem()
            .build();


    private int currentHighlight;

    public RealmSelectorGui(Player player) {
        super(player, 4, "Choose a realm!");
    }

    @Override
    public void redraw() {
        RealmSelectorGui.BORDER.apply(this);
        this.setItem(11, RealmSelectorGui.REALM);
        this.setItem(31, RealmSelectorGui.REALM_INFORMATION);
        Schedulers.sync()
                .runRepeating(task -> {
                        RealmSelectorGui.BORDER.apply(this);

                        this.setItem(this.currentHighlight, RealmSelectorGui.BORDER_HIGHLIGHTED_PLACE_HOLDER);
                        if (this.currentHighlight + 27 != 31) {
                            this.setItem(this.currentHighlight + 27, RealmSelectorGui.BORDER_HIGHLIGHTED_PLACE_HOLDER);
                        }
                        this.currentHighlight++;
                        if (this.currentHighlight == 9) {
                            this.currentHighlight = 0;
                        }
                        this.update(this.currentHighlight % 2 == 0 ? this.getInitialTitle() : "Choose a realm");
                }, 7, 7)
                .bindWith(this);
    }

    private void update(String title) {
        EntityPlayer entityPlayer = ((CraftPlayer) this.getPlayer()).getHandle();
        PacketPlayOutOpenWindow packet = new PacketPlayOutOpenWindow(entityPlayer.activeContainer.windowId, "minecraft:chest", new ChatMessage(title), this.getPlayer().getOpenInventory().getTopInventory().getSize());
        entityPlayer.playerConnection.sendPacket(packet);
        entityPlayer.updateInventory(entityPlayer.activeContainer);
    }
}
