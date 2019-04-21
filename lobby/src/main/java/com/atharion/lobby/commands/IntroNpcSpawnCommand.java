package com.atharion.lobby.commands;

import com.atharion.commons.lang.Lang;
import com.atharion.lobby.AtharionLobby;
import com.sllibrary.bukkit.Commands;

import javax.annotation.Nonnull;

public class IntroNpcSpawnCommand {

    public IntroNpcSpawnCommand(@Nonnull AtharionLobby atharionLobby) {
        Commands.create()
                .assertPlayer(Lang.COMMAND_INVALID_STATE.toString())
                .assertOp(Lang.COMMAND_NO_PERMISSION.toString())
                .handler(context -> {
                    atharionLobby.setNpcSpawn(context.sender().getLocation());
                    context.reply("You have set this as the introduction2 npc's location.");
                }).register("intronpc");
    }
}
