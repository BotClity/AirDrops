package main.airdrop;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class Translation {
    private File cfg;
    private FileConfiguration cfg_conf;
    private final Main plugin = Main.getInstance();
    public Translation(){
        createConfig();
    }
    public FileConfiguration getConfig(){
        return this.cfg_conf;
    }
    public void createConfig(){
        cfg = new File(plugin.getDataFolder(), "translation.yml");
        if (!cfg.exists()){
            cfg.getParentFile().mkdirs();
            plugin.saveResource("translation.yml",false);
        }
        cfg_conf = new YamlConfiguration();
        try{
            cfg_conf.load(cfg);
        }catch (IOException | InvalidConfigurationException e){
            e.printStackTrace();
        }
    }
    public void save(){
        try{
            cfg_conf.save(cfg);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public void reload(){
        File temp = new File(plugin.getDataFolder(),"translation.yml");
        try{
            cfg_conf.load(temp);
        }catch (IOException | InvalidConfigurationException e){
            e.printStackTrace();
        }
    }
}
