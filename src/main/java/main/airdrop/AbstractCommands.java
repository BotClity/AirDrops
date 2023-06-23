package main.airdrop;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;

public abstract class AbstractCommands implements CommandExecutor {
    public AbstractCommands(String command){
        PluginCommand pluginCommand =Main.getInstance().getCommand(command);
        if (pluginCommand != null){
            pluginCommand.setExecutor(this);
        }
    }
    public abstract void execute(CommandSender sender, Command command, String label, String[] args);
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        execute(sender,command,label,args);
        return true;
    }
}
