package main.airdrop.SupplySignals;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import main.airdrop.Main;
import main.airdrop.Save.SQL;
import main.airdrop.Tasks;
import main.airdrop.Translation;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class SupplyDrop implements Listener {
    private final ArrayList<World> worlds = new ArrayList<>();
    private final Plugin plugin = Main.getInstance();
    private final WorldGuardPlugin wg;
    private final FileConfiguration cfg = new Translation().getConfig();
    RegionManager regionManager;
    public SupplyDrop(){
        times = cfg_main.getInt("RandomDrop.times");
        times2 =times;
        wg= Main.getWorldGuardPlugin();
        for (String str :cfg_main.getStringList("Worlds")){
            if (Bukkit.getWorld(str) != null){
                worlds.add(Bukkit.getWorld(str));
            }
        }
    }
    @EventHandler
    public void onInteract(PlayerInteractEvent event) throws SQLException, IOException {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK & event.getAction() != Action.RIGHT_CLICK_AIR){
            return;
        }
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (block != null) {
            if (block.getType() == Material.CHEST) {
                Location loc1 = new Location(block.getWorld(), block.getX(), block.getY(), block.getZ());
                int id = SQL.getAirDropID(loc1);
                Boolean bool = SQL.getOpening(id);
                if (bool == null)return;
                event.setCancelled(true);
                if (bool){
                    event.getPlayer().sendMessage(getMessage("Status.closed", loc1, player, null));
                    return;
                }
                bool = SQL.getOpened(id);
                if (bool == null)return;
                if (bool) {
                    event.getPlayer().sendMessage(getMessage("Status.occupied", loc1, player, null));
                    return;
                }
                Inventory inv = SQL.getInventory(id);

                event.getPlayer().closeInventory();
                event.getPlayer().openInventory(inv);
                SQL.updateOpened(id,1);
                return;
            }
        }
        ItemStack item = event.getPlayer().getItemInHand();
        Location location = player.getLocation();
        if (item == null)return;
        if (item.getItemMeta() == null)return;
        if (item.getItemMeta().getLore() == null)return;
        List<String> lore = item.getItemMeta().getLore();
        if (item.getItemMeta().getLore().isEmpty())return;
        SupplySignalLVL1 drop1 = new SupplySignalLVL1();
        SupplySignalLVL2 drop2 = new SupplySignalLVL2();
        SupplySignalLVL3 drop3 = new SupplySignalLVL3();
        if (drop1.isDrop(lore) || drop2.isDrop(lore) || drop3.isDrop(lore)){
            if (worlds.isEmpty())return;
            if (!worlds.contains(location.getWorld()))return;
            if (cfg_main.getInt("Limit") > Bukkit.getOnlinePlayers().size()){
                player.sendMessage(getMessage("Status.canceled",null,null,null));
                return;
            }
            List<String> lore_item = item.getItemMeta().getLore();
            int id;
            if (drop1.isDrop(lore))id=1;
            else if (drop2.isDrop(lore))id=2;
            else if (drop3.isDrop(lore))id=3;
            else {player.sendMessage("Error");return;}
            event.setCancelled(true);
            for (int playerY = location.getBlockY();playerY<256;playerY++) {
                Location rg = new Location(location.getWorld(),location.getBlockX(),playerY,location.getBlockZ());
                RegionManager manager = wg.getRegionManager(event.getPlayer().getWorld());
                for (ProtectedRegion region : manager.getApplicableRegions(rg)) {
                    if (!region.getId().equalsIgnoreCase("global")) {
                        player.sendMessage(getMessage("Status.rg_problem",rg, player, null));
                        return;
                    }
                }
            }
            int cfg_x = plugin.getConfig().getInt("Drops"+id+".x") / 2;
            int cfg_z = plugin.getConfig().getInt("Drops"+id+".z") / 2;
            boolean check = true;
            Location spawn = null;
            while (check) {
                int x;
                if (random(1, 2) == 1) {
                    if (location.getBlockX() >= 0)x =random(0, cfg_x) + location.getBlockX();
                    else if (location.getBlockX() < 0)x = -random(0, cfg_x) + location.getBlockX();
                    else return;
                } else {
                    if (location.getBlockX() >= 0)x =-random(0, cfg_x) + location.getBlockX();
                    else if (location.getBlockX() < 0)x = random(0, cfg_x) + location.getBlockX();
                    else return;
                }
                int z;
                if (random(1, 2) == 1) {
                    if (location.getBlockZ() >= 0)z = random(0, cfg_z) + location.getBlockZ();
                    else if (location.getBlockZ() < 0)z = -random(0, cfg_z) + location.getBlockZ();
                    else return;
                } else {
                    if (location.getBlockZ() >= 0)z = -random(0, cfg_z) + location.getBlockZ();
                    else if (location.getBlockZ() < 0)z = random(0, cfg_z) + location.getBlockZ();
                    else return;
                }
                boolean setting_y = true;
                int test =0;
                Integer spawn_y = null;
                for (int playerY = 256; playerY > 0; playerY--) {
                    Location rg = new Location(location.getWorld(), x, playerY, z);
                    RegionManager manager = wg.getRegionManager(event.getPlayer().getWorld());
                    if (rg.getBlock().getType() != Material.AIR && setting_y){
                        spawn_y = playerY+50;
                        setting_y=false;
                    }
                    for (ProtectedRegion region : manager.getApplicableRegions(rg)) {
                        if (!region.getId().equalsIgnoreCase("global")) {
                            test=1;
                        }
                    }
                }
                if (spawn_y == null)return;
                spawn = new Location(location.getWorld(), x, spawn_y, z);
                if (test==1)continue;
                check=false;
            }
            Chunk chunk = event.getPlayer().getWorld().getChunkAt(spawn);
            chunk.load();
            int amount = item.getAmount()-1;
            if (amount == 0){
                item.setAmount(0);
                item.setType(Material.AIR);
            }else {
                item.setAmount(amount);
            }
            FallingBlock fallingBlock = location.getWorld().spawnFallingBlock(spawn,Material.JUKEBOX,(byte) 0);
            fallingBlock.setDropItem(false);
            fallingBlock.setFallDistance(50);
            Bukkit.broadcastMessage(getMessage("Status.called",spawn, null, null));
            regionManager = wg.getRegionManager(fallingBlock.getWorld());
            new Tasks(plugin,0,0) {
                @Override
                public void run(){
                    Location location2 = fallingBlock.getLocation();
                    Location location3 = new Location(location.getWorld(),location2.getBlockX(),location2.getBlockY()-1,location2.getBlockZ());
                    chunk.load();
                    ArrayList<Location> locs = new ArrayList<>();
                    locs.add(new Location(location.getWorld(),location2.getBlockX()-1,location2.getBlockY()-1,location2.getBlockZ()-1));
                    locs.add(new Location(location.getWorld(),location2.getBlockX()-1,location2.getBlockY()-1,location2.getBlockZ()));
                    locs.add(new Location(location.getWorld(),location2.getBlockX()-1,location2.getBlockY()-1,location2.getBlockZ()+1));
                    locs.add(new Location(location.getWorld(),location2.getBlockX(),location2.getBlockY()-1,location2.getBlockZ()-1));
                    locs.add(new Location(location.getWorld(),location2.getBlockX(),location2.getBlockY()-1,location2.getBlockZ()+1));
                    locs.add(new Location(location.getWorld(),location2.getBlockX()+1,location2.getBlockY()-1,location2.getBlockZ()-1));
                    locs.add(new Location(location.getWorld(),location2.getBlockX()+1,location2.getBlockY()-1,location2.getBlockZ()));
                    locs.add(new Location(location.getWorld(),location2.getBlockX()+1,location2.getBlockY()-1,location2.getBlockZ()+1));
                    boolean test = false;
                    for (Location locat:locs){
                        if (locat.getBlock().getType() != Material.AIR){
                            test = true;
                        }
                    }
                    if (location3.getBlock().getType() != Material.AIR || test){

                        chunk.load();
                        fallingBlock.remove();
                        fallingBlock.getLocation().getBlock().setType(Material.CHEST);
                        Location loc = fallingBlock.getLocation();
                        loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE,loc,3);
                        loc.getWorld().playSound(loc,Sound.ENTITY_GENERIC_EXPLODE,1,1);
                        Location MainLocation = new Location(loc.getWorld(),loc.getBlockX(),loc.getBlockY(),loc.getBlockZ());
                        Location First_sel = new Location(MainLocation.getWorld(),MainLocation.getBlockX()-5,MainLocation.getBlockY()-5,MainLocation.getBlockZ()-5);
                        Location Second_sel = new Location(MainLocation.getWorld(),MainLocation.getBlockX()+5,MainLocation.getBlockY()+5,MainLocation.getBlockZ()+5);
                        String name = "x" + MainLocation.getBlockX() + "y" + MainLocation.getBlockY() + "z" + MainLocation.getBlockZ();
                        BlockVector blockVector1 = BlockVector.toBlockPoint(First_sel.getBlockX(),First_sel.getBlockY(),First_sel.getBlockZ());
                        BlockVector blockVector2 = BlockVector.toBlockPoint(Second_sel.getBlockX(),Second_sel.getBlockY(),Second_sel.getBlockZ());
                        ProtectedCuboidRegion protectedCuboidRegion = new ProtectedCuboidRegion(name,blockVector1,blockVector2);
                        protectedCuboidRegion.setMinimumPoint(blockVector1);
                        protectedCuboidRegion.setMaximumPoint(blockVector2);
                        protectedCuboidRegion.setFlag(DefaultFlag.PVP, StateFlag.State.ALLOW);
                        protectedCuboidRegion.setFlag(DefaultFlag.BUILD, StateFlag.State.DENY);
                        protectedCuboidRegion.setFlag(DefaultFlag.BLOCK_BREAK, StateFlag.State.DENY);
                        protectedCuboidRegion.setFlag(DefaultFlag.BLOCK_PLACE, StateFlag.State.DENY);
                        wg.getRegionManager(loc.getWorld()).addRegion(protectedCuboidRegion);

                        int lvl;
                        Inventory loot;
                        if (lore_item.equals(drop1.getLore())){
                            lvl=1;
                            loot= drop1.getRandomLoot(name);
                        }
                        else if (lore_item.equals(drop2.getLore())){
                            lvl=2;
                            loot= drop2.getRandomLoot(name);
                        }
                        else if (lore_item.equals(drop3.getLore())){
                            lvl=3;
                            loot= drop3.getRandomLoot(name);
                        }else {return;}
                        int finalId = 0;
                        try {
                            finalId = SQL.addAirDrop(loc.getBlockX(),loc.getBlockY(),loc.getBlockZ(),lvl);
                            SQL.addInventory(finalId,loot);
                        } catch (SQLException exception) {
                            exception.printStackTrace();
                        }
                        Bukkit.broadcastMessage(getMessage("Timer.5min", loc, null, null));
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                Bukkit.broadcastMessage(getMessage("Timer.4min", loc, null, null));
                            }
                        }.runTaskLater(plugin,1200);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                Bukkit.broadcastMessage(getMessage("Timer.3min", loc, null, null));
                            }
                        }.runTaskLater(plugin,2400);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                Bukkit.broadcastMessage(getMessage("Timer.2min", loc, null, null));
                            }
                        }.runTaskLater(plugin,3600);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                Bukkit.broadcastMessage(getMessage("Timer.1min", loc, null, null));
                            }
                        }.runTaskLater(plugin,4800);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                Bukkit.broadcastMessage(getMessage("Timer.5sec", loc, null, null));
                            }
                        }.runTaskLater(plugin,5900);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                Bukkit.broadcastMessage(getMessage("Timer.4sec", loc, null, null));
                            }
                        }.runTaskLater(plugin,5920);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                Bukkit.broadcastMessage(getMessage("Timer.3sec", loc, null, null));
                            }
                        }.runTaskLater(plugin,5940);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                Bukkit.broadcastMessage(getMessage("Timer.2sec", loc, null, null));
                            }
                        }.runTaskLater(plugin,5960);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                Bukkit.broadcastMessage(getMessage("Timer.1sec", loc, null, null));
                            }
                        }.runTaskLater(plugin,5980);
                        int finalId1 = finalId;
                        new BukkitRunnable(){
                            @Override
                            public void run() {
                                Location location1 = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
                                try {
                                    SQL.updateOpening(finalId1, 0);
                                } catch (SQLException exception) {
                                    exception.printStackTrace();
                                }
                                Bukkit.broadcastMessage(getMessage("Status.opened", location1, player, null));
                                Bukkit.broadcastMessage(getMessage("Status.deleted", location1, player, 15));
                            }
                        }.runTaskLater(plugin,6000);
                        cancel();
                        return;
                    }
                    fallingBlock.getWorld().spawnParticle(Particle.SMOKE_LARGE,new Location(location2.getWorld(),location2.getX(),location2.getY()+0.5,location2.getZ()),1);
                    fallingBlock.setVelocity(new org.bukkit.util.Vector(0, -0.05, 0));

                }
            };
        }
    }
    @EventHandler
    public void InventoryClose(InventoryCloseEvent event) throws SQLException {
        if (event.getView().getTopInventory().getName()==null)return;
        int id = SQL.getIdByInventoryName(event.getView().getTopInventory().getName());
        if (id==0)return;
        SQL.updateOpened(id,0);
    }
    @EventHandler
    public void InventoryInteraction(InventoryClickEvent event) throws SQLException {
        if (event.getClickedInventory() == null)return;
        if (event.getClickedInventory().getName() == null)return;
        if (event.isShiftClick()){event.setCancelled(true);return;}
        String name = event.getClickedInventory().getName();
        int id = SQL.getIdByInventoryName(name);
        if (id==0)return;
        int lvl = SQL.getLevel(id);
        event.setCancelled(true);
        Player player = Bukkit.getPlayer(event.getWhoClicked().getUniqueId());
        ItemStack item =  event.getCurrentItem();
        Inventory inventory = event.getClickedInventory();
        if (item == null)return;
        if (item.getItemMeta() == null)return;
        if (item.getType() == Material.AIR)return;
        if (item.getItemMeta().getDisplayName() == null)return;
        HashMap<String,String> cmds = null;
        if (lvl == 1){
            cmds = new SupplySignalLVL1().getCmds();
        }
        if (lvl == 2){
            cmds = new SupplySignalLVL2().getCmds();
        }
        if (lvl == 3){
            cmds = new SupplySignalLVL3().getCmds();
        }if (lvl == 4){
            cmds = getCmds();
        }
        if (cmds == null)return;
        if (cmds.containsKey(item.getItemMeta().getDisplayName())){
            inventory.remove(item);

            player.getInventory().remove(item);
            String cmd = cmds.get(item.getItemMeta().getDisplayName());
            cmd = cmd.replaceAll("\\bplayer\\b",player.getName());
            cmd = cmd.replaceAll("\\bamount\\b", String.valueOf(item.getAmount()));
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),cmd);
            boolean test = false;
            for(ItemStack i : inventory) {
                if(i != null) {
                    test = true;
                    break;
                }
            }
            if (!test){
                Location location = SQL.getAirdropLocation(id);
                if (location == null)return;
                location.getBlock().setType(Material.AIR);
                SQL.removeAirDrop(id);
                player.closeInventory();
                for (ProtectedRegion rg : regionManager.getApplicableRegions(location)){
                    if (rg.getId().equalsIgnoreCase("global"))
                        return;
                    regionManager.removeRegion(rg.getId());
                }
                Bukkit.getServer().broadcastMessage(getMessage("Status.rg_claimed",location,player,null));
                return;
            }
            SQL.updateInventory(inventory,id);
        }else{
            item.setType(Material.AIR);
        }
    }
    @EventHandler
    public void BlockBreak(BlockBreakEvent event) throws SQLException{
        Location loc = new Location(event.getBlock().getWorld(), event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ());
        int id = SQL.getAirDropID(loc);
        if (id!=0){
            event.setCancelled(true);
        }
    }
    private String getMessage(String path,@Nullable Location loc, @Nullable Player player, @Nullable Integer time){
        String str = cfg.getString(path+".text");
        if (player != null){
            str=str.replaceAll("\\{player\\}", player.getDisplayName());
        }
        if (time != null){
            str=str.replaceAll("\\{time\\}", String.valueOf(time));
        }
        if (loc != null) {
            str = str.replaceAll("\\{y\\}", String.valueOf(loc.getBlockY()));
            str = str.replaceAll("\\{x\\}", String.valueOf(loc.getBlockX()));
            str = str.replaceAll("\\{z\\}", String.valueOf(loc.getBlockZ()));
        }
        return str;
    }
    private int random(int min,int max){
        Random random = new Random();
        int result = random.nextInt(max-min+1);
        return result+min;
    }
    private final HashMap<String,String> cmds = new HashMap<>();
    private final int times;
    private static int times2;
    private final FileConfiguration cfg_main = plugin.getConfig();
    public void timerAir(){
        if (cfg_main.getInt("Limit") > Bukkit.getOnlinePlayers().size()){
            Bukkit.broadcastMessage(getMessage("Status.canceled",null,null,null));
            return;
        }
        if (worlds.isEmpty())return;
        World world = worlds.get(random(1,worlds.size()));
        List<Integer> list_x = cfg_main.getIntegerList("RandomDrop.x");
        List<Integer> list_z = cfg_main.getIntegerList("RandomDrop.z");

        int y = cfg_main.getInt("RandomDrop.y");

        boolean check = true;
        Location spawn;
        int x=0;
        int z=0;
        while (check) {
            x = random(list_x.get(0),list_x.get(1));
            z = random(list_z.get(0),list_z.get(1));
            spawn = new Location(world, x, y, z);
            int test =0;
            for (int playerY = spawn.getBlockY(); playerY > 0; playerY--) {
                Location rg = new Location(spawn.getWorld(), spawn.getBlockX(), playerY, spawn.getBlockZ());
                RegionManager manager = wg.getRegionManager(Bukkit.getWorlds().get(0));
                for (ProtectedRegion region : manager.getApplicableRegions(rg)) {
                    if (!region.getId().equalsIgnoreCase("global")) {
                        test=1;
                    }
                }
            }
            if (test==1)continue;
            check=false;
        }
        Location location = new Location(world,x,y,z);
        Chunk chunk = world.getChunkAt(location);
        chunk.load();
        FallingBlock fallingBlock = location.getWorld().spawnFallingBlock(location, Material.JUKEBOX,(byte) 0);
        fallingBlock.setDropItem(false);
        fallingBlock.setFallDistance(50);
        Bukkit.broadcastMessage(getMessage("Status.called",location));
        new Tasks(plugin,0,0) {
            @Override
            public void run() {
                Location location2 = fallingBlock.getLocation();
                chunk.load();
                Location location3 = new Location(location.getWorld(),location2.getBlockX(),location2.getBlockY()-1,location2.getBlockZ());
                ArrayList<Location> locs = new ArrayList<>();
                locs.add(new Location(location.getWorld(),location2.getBlockX()-1,location2.getBlockY()-1,location2.getBlockZ()-1));
                locs.add(new Location(location.getWorld(),location2.getBlockX()-1,location2.getBlockY()-1,location2.getBlockZ()));
                locs.add(new Location(location.getWorld(),location2.getBlockX()-1,location2.getBlockY()-1,location2.getBlockZ()+1));
                locs.add(new Location(location.getWorld(),location2.getBlockX(),location2.getBlockY()-1,location2.getBlockZ()-1));
                locs.add(new Location(location.getWorld(),location2.getBlockX(),location2.getBlockY()-1,location2.getBlockZ()+1));
                locs.add(new Location(location.getWorld(),location2.getBlockX()+1,location2.getBlockY()-1,location2.getBlockZ()-1));
                locs.add(new Location(location.getWorld(),location2.getBlockX()+1,location2.getBlockY()-1,location2.getBlockZ()));
                locs.add(new Location(location.getWorld(),location2.getBlockX()+1,location2.getBlockY()-1,location2.getBlockZ()+1));
                boolean test = false;
                for (Location locat:locs){
                    if (locat.getBlock().getType() != Material.AIR || locat.getBlock().getType() == Material.WATER || locat.getBlock().getType() == Material.LAVA){
                        test = true;
                    }
                }
                if ( location3.getBlock().getType() != Material.AIR || test){
                    chunk.load();
                    fallingBlock.remove();
                    fallingBlock.getLocation().getBlock().setType(Material.CHEST);
                    Location loc = fallingBlock.getLocation();
                    Bukkit.broadcastMessage(getMessage("Timer.5min", loc));
                    loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE,loc,3);
                    loc.getWorld().playSound(loc,Sound.ENTITY_GENERIC_EXPLODE,1,1);
                    chunk.load();
                    int id = 0;
                    String name = "x" + loc.getBlockX() + "y" + loc.getBlockY() + "z" + loc.getBlockZ();
                    Location First_sel = new Location(loc.getWorld(),loc.getBlockX()-5,location.getBlockY()-5,loc.getBlockZ()-5);
                    Location Second_sel = new Location(loc.getWorld(),loc.getBlockX()+5,location.getBlockY()+5,loc.getBlockZ()+5);
                    BlockVector blockVector1 = BlockVector.toBlockPoint(First_sel.getBlockX(),First_sel.getBlockY(),First_sel.getBlockZ());
                    BlockVector blockVector2 = BlockVector.toBlockPoint(Second_sel.getBlockX(),Second_sel.getBlockY(),Second_sel.getBlockZ());
                    ProtectedCuboidRegion protectedCuboidRegion = new ProtectedCuboidRegion(name,blockVector1,blockVector2);
                    protectedCuboidRegion.setMinimumPoint(blockVector1);
                    protectedCuboidRegion.setMaximumPoint(blockVector2);
                    protectedCuboidRegion.setFlag(DefaultFlag.PVP, StateFlag.State.ALLOW);
                    protectedCuboidRegion.setFlag(DefaultFlag.BUILD, StateFlag.State.DENY);
                    protectedCuboidRegion.setFlag(DefaultFlag.BLOCK_BREAK, StateFlag.State.DENY);
                    protectedCuboidRegion.setFlag(DefaultFlag.BLOCK_PLACE, StateFlag.State.DENY);
                    wg.getRegionManager(loc.getWorld()).addRegion(protectedCuboidRegion);
                    try {
                        id = SQL.addAirDrop(loc.getBlockX(),loc.getBlockY(),loc.getBlockZ(),4);

                        SQL.addInventory(id,getRandomLoot(name));
                    } catch (SQLException exception) {
                        exception.printStackTrace();
                    }

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Bukkit.broadcastMessage(getMessage("Timer.4min", loc));
                        }
                    }.runTaskLater(plugin,1200);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Bukkit.broadcastMessage(getMessage("Timer.3min", loc));
                        }
                    }.runTaskLater(plugin,2400);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Bukkit.broadcastMessage(getMessage("Timer.2min", loc));
                        }
                    }.runTaskLater(plugin,3600);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Bukkit.broadcastMessage(getMessage("Timer.1min", loc));
                        }
                    }.runTaskLater(plugin,4800);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Bukkit.broadcastMessage(getMessage("Timer.5sec", loc));
                        }
                    }.runTaskLater(plugin,5900);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Bukkit.broadcastMessage(getMessage("Timer.4sec", loc));
                        }
                    }.runTaskLater(plugin,5920);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Bukkit.broadcastMessage(getMessage("Timer.3sec", loc));
                        }
                    }.runTaskLater(plugin,5940);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Bukkit.broadcastMessage(getMessage("Timer.2sec", loc));
                        }
                    }.runTaskLater(plugin,5960);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Bukkit.broadcastMessage(getMessage("Timer.1sec", loc));
                        }
                    }.runTaskLater(plugin,5980);
                    int finalId = id;
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Location location1 = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
                            Bukkit.broadcastMessage(getMessage("Status.opened", location1, null, null));
                            Bukkit.broadcastMessage(getMessage("Status.deleted", location1, null, 15));
                            try {
                                SQL.updateOpening(finalId, 0);
                            } catch (SQLException exception) {
                                exception.printStackTrace();
                            }
                        }
                    }.runTaskLater(plugin,6000);
                    cancel();
                    return;
                }
                fallingBlock.getWorld().spawnParticle(Particle.SMOKE_LARGE,new Location(location2.getWorld(),location2.getX(),location2.getY()+0.5,location2.getZ()),1);
                fallingBlock.setVelocity(new org.bukkit.util.Vector(0, -0.05, 0));
            }
        };
    }
    private String getMessage(String path,Location loc){
        String str = cfg.getString(path+".text");
        str=str.replaceAll("\\{y\\}", String.valueOf(loc.getBlockY()));
        str=str.replaceAll("\\{x\\}", String.valueOf(loc.getBlockX()));
        str=str.replaceAll("\\{z\\}", String.valueOf(loc.getBlockZ()));
        return str;
    }
    public Inventory getRandomLoot(String name){
        Inventory inv = Bukkit.createInventory(null,27,name);
        int count = plugin.getConfig().getInt("RandomDrop.count");
        int slot = 0;
        for (int index = 0;index<count;index++){
            int percentage = plugin.getConfig().getInt("RandomDrop.items."+index+".percentage");
            if (random(1,100) > percentage)continue;
            Material material = Material.matchMaterial(plugin.getConfig().getString("RandomDrop.items."+index+".material").toUpperCase());
            ItemStack item = new ItemStack(material);
            String temp = plugin.getConfig().getString("RandomDrop.items."+index+".name");
            List<String> lore = plugin.getConfig().getStringList("Drops1.items."+index+".lore");
            ItemMeta meta = item.getItemMeta();
            meta.setLore(lore);
            meta.setDisplayName(temp);
            item.setItemMeta(meta);
            int from = plugin.getConfig().getInt("RandomDrop.items."+index+".fromAmount");
            int to = plugin.getConfig().getInt("RandomDrop.items."+index+".toAmount");
            item.setAmount(random(from,to));
            inv.setItem(slot,item);
            slot++;
        }
        return inv;
    }
    public HashMap<String, String> getCmds() {
        int count = plugin.getConfig().getInt("RandomDrop.count");
        for (int index = 0;index<count;index++){
            String temp = plugin.getConfig().getString("RandomDrop.items."+index+".name");
            String cmd = plugin.getConfig().getString("RandomDrop.items."+index+".command");
            cmds.put(temp,cmd);
        }
        return cmds;
    }
    public void startTimer(){
        new Tasks(plugin,0,20){
            @Override
            public void run() {
                removeTime();
                if(times2==0){
                    timerAir();
                    resetTime();
                }

            }
        };
    }
    private void removeTime(){
        times2--;
    }
    public static int getTimes2() {
        return times2;
    }
    private void resetTime(){
        times2=times;
    }
}
