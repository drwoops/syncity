package me.drwoops.syncity.commands;

import me.drwoops.syncity.Syncity;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DebugCommand implements TabExecutor {

    Syncity plugin;

    public DebugCommand(Syncity plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 2) {
            if (args[1].equals("on")) plugin.setDebug(true);
            else if (args[1].equals("off")) plugin.setDebug(false);
            else return false;
            return true;
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 2) {
            ArrayList<String> options = new ArrayList<String>();
            options.add("on");
            options.add("off");
            return options;
        }
        return null;
    }
}
