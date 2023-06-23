package main.airdrop.SupplySignals;

import main.airdrop.Main;
import main.airdrop.Translation;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class SupplySignalLVL1 {
    private final HashMap<String,String> cmds = new HashMap<>();
    private final Main plugin = Main.getInstance();
    private final ItemStack item;
    private final String cfg_name ;
    private final ArrayList<String> lore;
    private final int id ;
    public SupplySignalLVL1(){
        //Настройка названия и цвета предмета
        FileConfiguration cfg = new Translation().getConfig();
        item = new ItemStack(Material.valueOf(cfg.getString("Drop1.material").toUpperCase()));
        cfg_name = cfg.getString("Drop1.name");
        id = cfg.getInt("Drop1.id");
        //Настройка описания предмета
        lore = (ArrayList<String>) cfg.getStringList("Drop1.lore.text");
        //Установка названия и описания
        int count = cfg.getInt("Drop1.enchantments.count");
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(cfg_name);
        meta.setLore(lore);
        for (int i = 0;i<count;i++) {
            int index = i+1;
            String ench_name = cfg.getString("Drop1.enchantments."+index+".name").toUpperCase();
            int ench_lvl = cfg.getInt("Drop1.enchantments."+index+".level");
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
        int count = plugin.getConfig().getInt("Drops1.count");
        int slot = 0;
        for (int index = 0;index<count;index++){
            int percentage = plugin.getConfig().getInt("Drops1.items."+index+".percentage");
            if (random(1,100) > percentage)continue;
            Material material = Material.matchMaterial(plugin.getConfig().getString("Drops1.items."+index+".material").toUpperCase());
            ItemStack item = new ItemStack(material);
            String temp = plugin.getConfig().getString("Drops1.items."+index+".name");
            List<String> lore = plugin.getConfig().getStringList("Drops1.items."+index+".lore");
            boolean enchant = plugin.getConfig().getBoolean("Drops1.items."+index+".enchantment");
            ItemMeta meta = item.getItemMeta();
            if (enchant) {
                meta.addEnchant(Enchantment.WATER_WORKER,1,true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            meta.setLore(lore);
            meta.setDisplayName(temp);
            item.setItemMeta(meta);
            int from = plugin.getConfig().getInt("Drops1.items."+index+".fromAmount");
            int to = plugin.getConfig().getInt("Drops1.items."+index+".toAmount");
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
        int count = plugin.getConfig().getInt("Drops1.count");
        for (int index = 0;index<count;index++){
            String temp = plugin.getConfig().getString("Drops1.items."+index+".name");
            String cmd = plugin.getConfig().getString("Drops1.items."+index+".command");
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
