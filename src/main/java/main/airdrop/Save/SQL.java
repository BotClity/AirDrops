package main.airdrop.Save;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

public class SQL {
    private static Connection connection;

    private static Statement statement () throws SQLException{return connection.createStatement();}
    private static ResultSet resultSet;
    public static void Connect(String path) throws SQLException,ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:"+path+"DataBase.s3db");
        Bukkit.getLogger().info(ChatColor.GREEN + "DataBase connected!");
        connection.setAutoCommit(true);
    }
    public static void createTable() throws SQLException{
        statement().execute("CREATE TABLE IF NOT EXISTS airdrops (id INT,opened BOOLEAN,x INT,y INT,z INT, timer INT,lvl INT,inv TEXT,opening BOOLEAN, name TEXT)");
    }
    public static int addAirDrop(int x, int y, int z, int lvl) throws SQLException{
        resultSet = statement().executeQuery("SELECT count(*) FROM airdrops");
        resultSet.next();
        int lastId = resultSet.getInt(1);
        if (lastId == 0) {
            int id = lastId + 1;
            statement().execute("INSERT INTO airdrops (id,opened,x,y,z,timer,lvl,inv,opening, name) VALUES (" + id + ", 0 ," + x + "," + y + "," + z + ",15,"+lvl+" , null , 1 , null)");
            return id;
        }
        int MaxID = getMaxID();
        int id = MaxID+1;
        statement().execute("INSERT INTO airdrops (id,opened,x,y,z,timer,lvl,inv,opening, name) VALUES (" + id + ", 0 ," + x + "," + y + "," + z + ",15,"+lvl+" , null , 1 , null)");
        return id;
    }
    public static Location getAirdropLocation(int id)throws SQLException{
        resultSet = statement().executeQuery("SELECT x,y,z FROM airdrops WHERE id="+id+"");
        if (!resultSet.next()){
            return null;
        }
        return new Location(Bukkit.getWorlds().get(0),resultSet.getInt(1),resultSet.getInt(2),resultSet.getInt(3));
    }
    public static int getAirDropID(Location location)throws SQLException{
        resultSet = statement().executeQuery("SELECT id FROM airdrops WHERE x="+location.getX()+" and y="+location.getY()+" and z="+location.getZ()+"");
        if (!resultSet.next()){
            return 0;
        }
        return resultSet.getInt(1);
    }
    public static int getAirDropID(Inventory inventory)throws SQLException{
        resultSet = statement().executeQuery("SELECT id FROM airdrops WHERE inv="+BukkitSerialization.toBase64(inventory));
        if (!resultSet.next()){
            return 0;
        }
        return resultSet.getInt(1);
    }
    public static int getAirDropID(int x, int y, int z)throws SQLException{
        resultSet = statement().executeQuery("SELECT id FROM airdrops WHERE x="+x+" and y="+y+" and z="+z+"");
        if (!resultSet.next()){
            return 0;
        }
        return resultSet.getInt(1);
    }
    public static void removeAirDrop(int id) throws SQLException{
        statement().execute("DELETE FROM airdrops WHERE id="+id+"");
    }
    public static void removeAirDrop(Block block) throws SQLException{
        statement().execute("DELETE FROM airdrops WHERE x="+block.getX()+" and y="+block.getY()+" and z="+block.getZ()+"");
    }
    public static void closeDataBase() throws SQLException {
        statement().executeUpdate("Update airdrops SET opened=0");
        connection.close();
    }
    public static int getMaxID()throws SQLException{
        resultSet = statement().executeQuery("SELECT MAX(id) FROM airdrops");
        if (!resultSet.next()){
            return 0;
        }
        return resultSet.getInt(1);
    }
    public static void updateOpened(int id,int opened)throws SQLException{
        String query = "UPDATE airdrops SET opened= ? WHERE id= ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, opened);
        statement.setInt(2, id);
        statement.executeUpdate();
    }
    public static int getLevel(Block block)throws SQLException{
        resultSet = statement().executeQuery("SELECT lvl FROM airdrops WHERE x="+block.getX()+" and y="+block.getY()+" and z="+block.getZ()+"");
        if (!resultSet.next()){
            return 0;
        }
        return resultSet.getInt(1);
    }
    public static int getLevel(Location location)throws SQLException{
        resultSet = statement().executeQuery("SELECT lvl FROM airdrops WHERE x="+location.getBlockX()+" and y="+location.getBlockY()+" and z="+location.getBlockZ()+"");
        if (!resultSet.next()){
            return 0;
        }
        return resultSet.getInt(1);
    }
    public static int getLevel(int id)throws SQLException{
        resultSet = statement().executeQuery("SELECT lvl FROM airdrops WHERE id="+id);
        if (!resultSet.next()){
            return 0;
        }
        return resultSet.getInt(1);
    }
    public static boolean checkAirDrop(Block block)throws SQLException{
        resultSet = statement().executeQuery("SELECT * FROM airdrops WHERE x="+block.getX()+" and y="+block.getY()+" and z="+block.getZ()+"");
        return resultSet.next();
    }
    public static ResultSet allAirdrops() throws SQLException{
        return statement().executeQuery("SELECT * FROM airdrops");
    }
    public static void addInventory(int id, Inventory inventory) throws SQLException{
        String query = "UPDATE airdrops SET inv= ? WHERE id="+id;
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1,BukkitSerialization.toBase64(inventory));
        statement.executeUpdate();
        updateName(inventory.getName(), id);
    }

    public static Boolean getOpened(int id) throws SQLException{
        resultSet = statement().executeQuery("SELECT opened FROM airdrops WHERE id="+id);
        if (!resultSet.next()){
            return null;
        }
        return resultSet.getBoolean(1);
    }
    public static void updateOpening(int id,int opening)throws SQLException{
        String query = "UPDATE airdrops SET opening= ? WHERE id= ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, opening);
        statement.setInt(2, id);
        statement.executeUpdate();
    }
    public static Boolean getOpening(int id) throws SQLException{
        resultSet = statement().executeQuery("SELECT opening FROM airdrops WHERE id="+id);
        if (!resultSet.next()){
            return null;
        }
        return resultSet.getBoolean(1);
    }
    public static ArrayList<Inventory> getAllInventories() throws SQLException, IOException {
        resultSet = statement().executeQuery("SELECT inv,name FROM airdrops");
        if (!resultSet.next()){
            return null;
        }
        ArrayList<Inventory> temps = new ArrayList<>();
        while (resultSet.next()){
            Inventory temp = BukkitSerialization.fromBase64(resultSet.getString(1),resultSet.getString(2));
            temps.add(temp);
        }
        return temps;
    }
    public static int getIdByInventoryName(String name) throws SQLException{
        resultSet = statement().executeQuery("SELECT id FROM airdrops WHERE name= '"+name+"'");
        if (!resultSet.next()){
            return 0;
        }
        return resultSet.getInt(1);
    }
    public static void updateName(String name,int id) throws SQLException{
        statement().execute("UPDATE airdrops SET name='"+name+"' WHERE id="+id);
    }
    public static void updateInventory(Inventory inventory,int id) throws SQLException{
        String query = "UPDATE airdrops SET inv=? WHERE id=?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1,BukkitSerialization.toBase64(inventory));
        statement.setInt(2,id);
        statement.executeUpdate();
    }
    public static Inventory getInventory(int id) throws SQLException, IOException {
        resultSet = statement().executeQuery("SELECT inv,name FROM airdrops WHERE id=" + id);
        if (!resultSet.next()) {
            return null;
        }
        if (resultSet.getString(1) == null)return null;
        if (resultSet.getString(2) == null)return null;
        return BukkitSerialization.fromBase64(resultSet.getString(1), resultSet.getString(2));

    }
    public static void updateTimer(Inventory inventory) throws SQLException{
        int id = getIdByInventoryName(inventory.getName());
        if (id==0)return;
        resultSet = statement().executeQuery("SELECT timer FROM airdrops WHERE id="+id);
        if (!resultSet.next())return;
        Boolean bool = getOpening(id);
        if (bool == null)return;
        if (bool)return;
        int time = resultSet.getInt(1);
        int newTime = time-1;
        if (newTime == 0){
            Location location = getAirdropLocation(id);
            if (location == null)return;
            location.getBlock().setType(Material.AIR);
            removeAirDrop(id);
            return;
        }
        statement().executeUpdate("UPDATE airdrops SET timer="+newTime+" WHERE id="+id);

    }
    public static ResultSet executeQuery(String command) throws SQLException{
        return statement().executeQuery(command);
    }
    public static void execute(String command) throws SQLException{
        statement().execute(command);
    }
    public static void updateTimer(int id, int time) throws SQLException{
        String Query = "UPDATE airdrops SET timer=? WHERE id=?";
        PreparedStatement preparedStatement = connection.prepareStatement(Query);
        preparedStatement.setInt(2, id);
        preparedStatement.setInt(1, time);
        preparedStatement.executeUpdate();
    }
}
