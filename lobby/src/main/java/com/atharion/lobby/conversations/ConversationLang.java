package com.atharion.lobby.conversations;

import com.atharion.commons.lang.Lang;
import com.atharion.lobby.npcs.LadyAliceNpc;
import com.sllibrary.bukkit.text.Text;
import lombok.Getter;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.stream.Collectors;

@Getter
public enum ConversationLang {

    CONVERSATION_GREETER("&6Lady Alice" + com.atharion.commons.lang.Lang.COLON + " &f%s"),

    /**
     * Introduction
     */
    INTRODUCTION_IN_VILLAGE_RANGE(com.atharion.commons.lang.Lang.CONVERSATION_PLAYER_SELF.format("Here it is! I think I see someone..")),
    INTRODUCTION_GREETER_WELCOME(new String[]{
            String.format(CONVERSATION_GREETER.toString(), "Hey there! I didn't expect to see a new face!"),
            String.format(CONVERSATION_GREETER.toString(), "After the incident, I thought everyone had vanished and deserted the town.."),
            String.format(Lang.CONVERSATION_PLAYER_SELF.toString(), "Incident? What incident?"),
            String.format(CONVERSATION_GREETER.toString(), "Just the other night, a massive portal was summoned north of the town."),
            String.format(CONVERSATION_GREETER.toString(), "Crops have been dying, and villagers have gone missing.. I've been hiding out since then."),
            String.format(Lang.CONVERSATION_PLAYER_SELF.toString(), "I'll go take a look. You said it was north from here?"),
            String.format(CONVERSATION_GREETER.toString(), "Yes! Just north of the town, it isn't too far. Please do be careful!"),
            String.format(CONVERSATION_GREETER.toString(), "Here are some materials for your journey.."),
    });

    private final String[] text;

    ConversationLang(String text) {
        this.text = new String[]{text};
    }

    ConversationLang(String[] text) {
        this.text = text;
    }

    @Nullable
    public String get(int index) {
        if (this.text.length == 0) {
            throw new IllegalStateException("array has no elements");
        }
        if (this.text.length == 1) {
            return Text.colorize(this.text[0]);
        }
        if (index >= this.text.length) {
            throw new IndexOutOfBoundsException("index exceeds array length");
        }
        return Text.colorize(this.text[index]);
    }

    @Override
    public String toString() {
        return this.text.length == 1 ? Text.colorize(this.text[0]) : Arrays.stream(this.text)
                .map(Text::colorize)
                .collect(Collectors.joining("\n"));
    }
}
