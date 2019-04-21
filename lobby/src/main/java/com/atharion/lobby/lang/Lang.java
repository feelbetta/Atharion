package com.atharion.lobby.lang;

import com.sllibrary.bukkit.text.Text;

import javax.annotation.Nonnull;

public enum Lang {

    GADGET_BOMB_COOLDOWN("&4You can only use this every %s!");

    private String text;
    private boolean replaceUTF8 = false;


    Lang(String text, boolean replaceUTF8) {
        this.text = text;
        this.replaceUTF8 = replaceUTF8;
    }

    Lang(String text) {
        this.text = text;
    }

    public String format(Object... format) {
        return String.format(this.text, format);
    }

    private String format(@Nonnull String string) {
        return string.replace("<3", "❤");
    }

    @Override
    public String toString() {
        String formatted = Text.colorize(this.text);
        return this.replaceUTF8 ? this.format(formatted) : formatted;
    }
}
