package main.airdrop;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import main.airdrop.Commands.Airdrop;
import main.airdrop.Save.SQL;
import main.airdrop.SupplySignals.SupplyDrop;
import main.airdrop.SupplySignals.SupplySignalLVL1;
import org.bukkit.*;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class Main extends JavaPlugin {
    private static Main instance;
    private static WorldGuardPlugin worldGuardPlugin;
    private static WorldEdit worldEdit;
    public static WorldGuardPlugin getWorldGuardPlugin(){
        return worldGuardPlugin;
    }
    public static Main getInstance(){
        return instance;
    }
    public static WorldEdit getWorldEdit(){return worldEdit;}
    @Override
    public void onEnable(){
        File config = new File(getDataFolder() + File.separator + "config.yml");
        if (!config.exists()) {
            getLogger().info("Creating config file...");
            getLogger().info("Please write config");
            getConfig().options().copyDefaults(true);
            saveDefaultConfig();
        }
        try {
            SQL.Connect(getDataFolder()+File.separator);
            SQL.createTable();
        } catch (SQLException | ClassNotFoundException exception) {
            exception.printStackTrace();
        }
        worldGuardPlugin = getPlugin(WorldGuardPlugin.class);
        worldEdit = WorldEdit.getInstance();
        instance = this;
        // Plugin startup logic
        new SupplyDrop().startTimer();
        getServer().getPluginManager().registerEvents(new SupplyDrop(), this);
        new TimerPlaceholder().register();
        new Translation();
        new Airdrop();
        new SupplySignalLVL1();
        new Tasks(this,0, 1200){
            @Override
            public void run() {
                try {
                    ResultSet resultSet = SQL.executeQuery("SELECT id,timer FROM 'airdrops' WHERE opening=0 ");
                    while (resultSet.next()){
                        int id = resultSet.getInt(1);
                        int timer = resultSet.getInt(2);

                        int new_timer = timer-1;
                        if (new_timer==0){
                            Location location = SQL.getAirdropLocation(id);
                            if (location == null)continue;
                            Chunk chunk = location.getChunk();
                            if (!chunk.isLoaded()){
                                chunk.load();
                            }
                            location.getWorld().spawnParticle(Particle.EXPLOSION_LARGE,location,3);
                            location.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXPLODE,1,1);
                            Bukkit.getServer().broadcastMessage(getMessage("Status.removed",location));
                            location.getBlock().setType(Material.AIR);
                            SQL.removeAirDrop(id);
                            continue;
                        }
                        SQL.updateTimer(id,new_timer);
                        Location location = SQL.getAirdropLocation(id);
                        Bukkit.getServer().broadcastMessage(getMessage("Status.deleted",location,new_timer));
                    }
                } catch (SQLException exception) {
                    exception.printStackTrace();
                }
            }
        };
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        try {
            ResultSet resultSet = SQL.executeQuery("SELECT id FROM airdrops WHERE opening=1");
            while (resultSet.next()){
                SQL.updateOpening(resultSet.getInt(1),0);
            }
            SQL.closeDataBase();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }
    private String getMessage(String path,Location loc){
        String str = new Translation().getConfig().getString(path+".text");
        str=str.replaceAll("\\{y\\}", String.valueOf(loc.getBlockY()));
        str=str.replaceAll("\\{x\\}", String.valueOf(loc.getBlockX()));
        str=str.replaceAll("\\{z\\}", String.valueOf(loc.getBlockZ()));
        return str;
    }
    private String getMessage(String path, Location loc, @Nullable Integer time){
        String str = new Translation().getConfig().getString(path+".text");
        if (time != null){
            str=str.replaceAll("\\{time\\}", String.valueOf(time));
        }
        str=str.replaceAll("\\{y\\}", String.valueOf(loc.getBlockY()));
        str=str.replaceAll("\\{x\\}", String.valueOf(loc.getBlockX()));
        str=str.replaceAll("\\{z\\}", String.valueOf(loc.getBlockZ()));
        return str;
    }


}
