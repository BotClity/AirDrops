package main.airdrop.SupplySignals;

import main.airdrop.Main;
import main.airdrop.Translation;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;

public class SupplyDrops {
    private final Plugin main = Main.getInstance();
    private final FileConfiguration cfg = main.getConfig();
    private final FileConfiguration translation = new Translation().getConfig();
    private final int amount = cfg.getInt("count");
    private final ArrayList<ItemStack> items = new ArrayList<>();
    private final HashMap<Integer, ItemStack> ids = new HashMap<>();
    public SupplyDrops(){
        init_items();
    }
    private void init_items(){
        for(int index=0;index<amount;index++){
            // id предмета
            int id = translation.getInt("Drops"+index+".id");
            // Настроййка предмета
            String name = translation.getString("Drops"+index+".name");
            ChatColor color = ChatColor.valueOf(translation.getString("Drops"+index+".color").toUpperCase());
            Material material = Material.getMaterial(translation.getString("Drops"+index+".material").toUpperCase());

            ArrayList<String> lore = new ArrayList<>();
            ChatColor lore_color = ChatColor.valueOf(translation.getString("Drops"+index+".lore.color").toUpperCase());
            for(String line:translation.getStringList("Drops"+index+".lore.text")){
                lore.add(lore_color+line);
            }
            int count = translation.getInt("Drops"+index+".enchantments.count");
            // Предмет
            ItemStack temp = new ItemStack(material);
            ItemMeta meta = temp.getItemMeta();
            meta.setDisplayName(color+name);
            meta.setLore(lore);
            for (int i = 0;i<count;i++) {
                String ench_name = translation.getString("Drops"+index+".enchantments."+i+".name").toUpperCase();
                int ench_lvl = translation.getInt("Drops"+index+".enchantments."+i+".level");
                meta.addEnchant(Enchantment.getByName(ench_name),ench_lvl,true);
            }
            temp.setItemMeta(meta);
            items.add(temp);
            ids.put(id, temp);
        }
    }

    public HashMap<Integer, ItemStack> getIds() {
        return ids;
    }

    public int getAmount() {
        return amount;
    }

    public ArrayList<ItemStack> getItems() {
        return items;
    }
}
