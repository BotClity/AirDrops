package main.airdrop.SupplySignals;

import main.airdrop.Main;
import main.airdrop.Translation;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class SupplySignalLVL3 {
    private final HashMap<String,String> cmds = new HashMap<>();
    private final Main plugin = Main.getInstance();
    private final ItemStack item ;
    private final String cfg_name ;
    private final ArrayList<String> lore;
    private final int id ;
    public SupplySignalLVL3(){

        //Настройка названия и цвета предмета
        FileConfiguration cfg = new Translation().getConfig();
        item = new ItemStack(Material.valueOf(cfg.getString("Drop3.material").toUpperCase()));
        cfg_name = cfg.getString("Drop3.name");
        id = cfg.getInt("Drop3.id");
        //Настройка описания предмета
        lore = (ArrayList<String>) cfg.getStringList("Drop3.lore.text");

        //Установка названия и описания
        int count = cfg.getInt("Drop3.enchantments.count");
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(cfg_name);
        meta.setLore(lore);
        for (int i = 0;i<count;i++) {
            int index = i+1;
            String ench_name = cfg.getString("Drop3.enchantments."+index+".name").toUpperCase();
            int ench_lvl = cfg.getInt("Drop3.enchantments."+index+".level");
            meta.addEnchant(Enchantment.getByName(ench_name),ench_lvl,true);
        }
        item.setItemMeta(meta);
    }

    public ArrayList<String> getLore() {
        return lore;
    }

    public  ItemStack getItem() {
        return item;
    }

    public String getName() {
        return cfg_name;
    }

    public ItemStack getSupply(){
        return item;
    }
    public Inventory getRandomLoot(String name){
        Inventory inv = Bukkit.createInventory(null,27,name);
        int count = plugin.getConfig().getInt("Drops3.count");
        int slot = 0;
        for (int index = 0;index<count;index++){
            int percentage = plugin.getConfig().getInt("Drops3.items."+index+".percentage");
            if (random(1,100) > percentage)continue;
            Material material = Material.matchMaterial(plugin.getConfig().getString("Drops3.items."+index+".material").toUpperCase());
            ItemStack item = new ItemStack(material);
            String temp = plugin.getConfig().getString("Drops3.items."+index+".name");
            List<String> lore = plugin.getConfig().getStringList("Drops1.items."+index+".lore");
            ItemMeta meta = item.getItemMeta();
            meta.setLore(lore);
            meta.setDisplayName(temp);
            item.setItemMeta(meta);
            int from = plugin.getConfig().getInt("Drops3.items."+index+".fromAmount");
            int to = plugin.getConfig().getInt("Drops3.items."+index+".toAmount");
            item.setAmount(random(from,to));
            inv.setItem(slot,item);
            slot++;
        }
        return inv;
    }
    private int random(int min,int max){
        Random random = new Random();
        int result = random.nextInt(max-min+1);
        return result+min;
    }

    public HashMap<String, String> getCmds() {
        int count = plugin.getConfig().getInt("Drops3.count");
        for (int index = 0;index<count;index++){
            String temp = plugin.getConfig().getString("Drops3.items."+index+".name");
            String cmd = plugin.getConfig().getString("Drops3.items."+index+".command");
            if (cmds.containsKey(temp))continue;
            cmds.put(temp,cmd);
        }
        return cmds;
    }
    public boolean isDrop(List<String> lore2){
        return lore.equals(lore2);
    }

    public int getId() {
        return id;
    }
}
