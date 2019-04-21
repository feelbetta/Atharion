package com.atharion.lobby.commands;

import com.atharion.commons.lang.Lang;
import com.atharion.lobby.AtharionLobby;
import com.sllibrary.bukkit.Commands;
import com.sllibrary.bukkit.serialize.Point;

import javax.annotation.Nonnull;

public class IntroBoxSpawnCommand {

    public IntroBoxSpawnCommand(@Nonnull AtharionLobby atharionLobby) {
        Commands.create()
                .assertPlayer(Lang.COMMAND_INVALID_STATE.toString())
                .assertOp(Lang.COMMAND_NO_PERMISSION.toString())
                .handler(context -> {
                    atharionLobby.setBox(Point.of(context.sender().getLocation()));
                    context.reply("You have set this as the introduction's box location.");
                }).register("introbox");
    }
}
