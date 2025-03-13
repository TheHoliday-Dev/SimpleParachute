package com.holiday.simpleparachute.commands;

import com.holiday.simpleparachute.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MainCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender instanceof Player player) {
            player.getInventory().addItem(Main.getInstance().getParachuteItem());
        }
        return false;
    }
}
