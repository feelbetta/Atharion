package com.atharion.lobby.gadgets.types;

import com.atharion.lobby.gadgets.Gadget;
import com.google.common.base.Strings;
import com.sllibrary.bukkit.item.ItemStacks;
import com.sllibrary.bukkit.text.Text;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

public class DonationGadget implements Gadget {

    private final TextComponent preText, url;

    public DonationGadget() {
        this.preText = new TextComponent(Text.center("&eour forums at, "));
        this.url = new TextComponent(Text.center("&6atharion.com/platinum&e."));
        this.url.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent(Text.colorize("&fClick to visit our forums!"))}));
        this.url.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.atharion.com/platinum"));
    }

    @Nonnull
    @Override
    public String getName() {
        return "Purchase Platinum";
    }

    @Nonnull
    @Override
    public ItemStack getIdentifier() {
        return ItemStacks.of(Material.TOTEM)
                .name("&fPlatinum Membership")
                .lore("&8Gadget")
                .lore(" ")
                .lore("&fRight Click &eto view information")
                .lore("&eon &fPlatinum Memberships!")
                .build();
    }

    @Override
    public int getSlot() {
        return 2;
    }

    @Override
    public boolean canUse(@Nonnull Player player) {
        return true;
    }

    @Override
    public void onUse(@Nonnull Player player) {
        player.sendMessage(Strings.repeat(" \n", 80));
        player.sendMessage(Text.colorize("&7&m↦-----------------------------------------------↤"));
        player.sendMessage(Text.center("&fPlatinum Membership"));
        player.sendMessage(" ");
        player.sendMessage(Text.center("&eBigger parties, exclusive skins,"));
        player.sendMessage(Text.center("&eguild addons, daily rewards, and more!"));
        player.sendMessage(" ");
        player.sendMessage(this.url);
        player.sendMessage(Text.colorize("&7&m↦-----------------------------------------------↤"));
        player.playSound(player.getLocation(), Sound.BLOCK_IRON_TRAPDOOR_OPEN, 2, 15);
    }
}
