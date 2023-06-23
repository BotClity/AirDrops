package main.airdrop;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public abstract class Tasks implements Runnable{
    private final int id;
    public Tasks(Plugin plugin,int arg1, int arg2){
        id = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin,this,arg1,arg2);
    }
    public void cancel(){
        Bukkit.getScheduler().cancelTask(id);
    }
}
