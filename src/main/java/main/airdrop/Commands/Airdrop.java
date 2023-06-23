package main.airdrop.Commands;

import main.airdrop.AbstractCommands;
import main.airdrop.Main;
import main.airdrop.SupplySignals.SupplySignalLVL1;
import main.airdrop.SupplySignals.SupplySignalLVL2;
import main.airdrop.SupplySignals.SupplySignalLVL3;
import main.airdrop.Translation;
import org.bukkit.Bukkit;
import org.bukkit.command.BufferedCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;

public class Airdrop extends AbstractCommands {
    private final Plugin plugin = Main.getInstance();
    public Airdrop() {
        super("airdrop");
    }

    @Override
    public void execute(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender)return;
        if (!sender.hasPermission("airdrop.give") || !sender.isOp())return;
        Player player = (Player) sender;
        int id1 =new SupplySignalLVL1().getId();
        int id2 =new SupplySignalLVL2().getId();
        int id3 =new SupplySignalLVL3().getId();
        if (args.length == 0){
            return;
        }
        if (args[0].equalsIgnoreCase("reload")){
            new Translation().reload();
            plugin.reloadConfig();
            player.sendMessage("Configs reloaded");
            return;
        }
        int get_id = Integer.parseInt(args[0]);
        if (id1 == get_id){
            ((Player) sender).getInventory().addItem(new SupplySignalLVL1().getSupply());
        }
        if (id2 == get_id){
            ((Player) sender).getInventory().addItem(new SupplySignalLVL2().getSupply());
        }
        if (id3 == get_id){
            ((Player) sender).getInventory().addItem(new SupplySignalLVL3().getSupply());
        }
    }
}
