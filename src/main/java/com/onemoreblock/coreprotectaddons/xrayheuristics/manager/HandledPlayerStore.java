//--------------------------------------------------------------------
// Copyright © Dylan Calaf Latham 2019-2021 XRay Heuristics
//--------------------------------------------------------------------

package com.onemoreblock.coreprotectaddons.xrayheuristics.manager;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.onemoreblock.coreprotectaddons.xrayheuristics.XRayHeuristicsModule;
import com.onemoreblock.coreprotectaddons.xrayheuristics.callback.HandledPlayerListCallback;
import com.onemoreblock.coreprotectaddons.xrayheuristics.callback.HandledPlayerBelongingsCallback;
import com.onemoreblock.coreprotectaddons.xrayheuristics.callback.HandledPlayerLocationCallback;
import com.onemoreblock.coreprotectaddons.xrayheuristics.callback.HandledPlayerStoreCallback;
import com.onemoreblock.coreprotectaddons.xrayheuristics.support.DummyEquipment;
import com.onemoreblock.coreprotectaddons.xrayheuristics.support.DummyInventory;
import com.onemoreblock.coreprotectaddons.xrayheuristics.util.BukkitSerializer;
import com.onemoreblock.coreprotectaddons.xrayheuristics.handling.HandledPlayerRecord;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.apache.commons.dbcp2.BasicDataSource;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HandledPlayerStore {

    private static final Logger log = LoggerFactory.getLogger(HandledPlayerStore.class);
    private final XRayHeuristicsModule module;
    //SQL Data:
    private BasicDataSource dataSource; //Stores a pool of SQL connections
    //JSON Data:
    private List<HandledPlayerRecord> storedXrayersFromJSON = new ArrayList<>(); //Used for loading xrayer data from JSON

    public HandledPlayerStore(XRayHeuristicsModule main) {
        this.module = main;
    }

    //The following methods manage persistent memory resources (SQL or JSON is managed depending on plugin config.yml)
    //The methods are designed to be called asynchronously through Bukkit's scheduler, and return data through a callback function:

    //Stores player as xrayer with data
    public void StorePlayerData(Player player, final HandledPlayerStoreCallback callback) {
        switch (module.getStorageType()) {
            case "MYSQL":
                try (Connection cn = dataSource.getConnection()) {
                    try {
                        if (cn != null) {
                            SQLPlayerDataStore(cn, player, callback);
                        }
                    } catch (SQLException e) {
                        log.error("e: ", e);
                    }
                } catch (SQLException e) {
                    log.error("e: ", e);
                }
                break;
            case "JSON":
                JSONPlayerDataStore(player, callback);
                break;
            default:
                break;
        }
    }

    //Stores fake player as xrayer with fake data
    public void StoreDummyPlayerData(final HandledPlayerStoreCallback callback) {
        switch (module.getStorageType()) {
            case "MYSQL":
                try (Connection cn = dataSource.getConnection()) {
                    try {
                        if (cn != null) {
                            SQLPlayerDataStore(cn, null, callback);
                        }
                    } catch (SQLException e) {
                        log.error("e: ", e);
                    }
                } catch (SQLException e) {
                    log.error("e: ", e);
                }
                break;
            case "JSON":
                JSONPlayerDataStore(null, callback);
                break;
            default:
                break;
        }
    }

    //Returns various array lists through callback function containing all registered xrayer UUID's, handled times amount, and firstHandled time.
    public void GetAllBaseXrayerData(final HandledPlayerListCallback callback) {
        switch (module.getStorageType()) {
            case "MYSQL":
                try (Connection cn = dataSource.getConnection()) {
                    try {
                        if (cn != null) {
                            SQLGetAllBaseXrayerData(cn, callback);
                        }
                    } catch (SQLException e) {
                        log.error("e: ", e);
                    }
                } catch (SQLException e) {
                    log.error("e: ", e);
                }
                break;
            case "JSON":
                JSONGetAllBaseXrayerData(callback);
                break;
            default:
                break;
        }
    }

    //Returns ItemStack array through callback function containing all confiscated ItemStacks from the specified player by UUID
    public void GetXrayerBelongings(String xrayerUUID, final HandledPlayerBelongingsCallback callback) {
        switch (module.getStorageType()) {
            case "MYSQL":
                try (Connection cn = dataSource.getConnection()) {
                    try {
                        if (cn != null) {
                            SQLGetXrayerBelongings(cn, xrayerUUID, callback);
                        }
                    } catch (SQLException e) {
                        log.error("e: ", e);
                    }
                } catch (SQLException e) {
                    log.error("e: ", e);
                }
                break;
            case "JSON":
                JSONGetXrayerBelongings(xrayerUUID, callback);
            default:
                break;
        }
    }

    //Returns HandleLocation Location through callback function by UUID
    public void GetXrayerHandleLocation(String xrayerUUID, final HandledPlayerLocationCallback callback) {
        switch (module.getStorageType()) {
            case "MYSQL":
                try (Connection cn = dataSource.getConnection()) {
                    try {
                        if (cn != null) {
                            SQLGetXrayerHandleLocation(cn, xrayerUUID, callback);
                        }
                    } catch (SQLException e) {
                        log.error("e: ", e);
                    }
                } catch (SQLException e) {
                    log.error("e: ", e);
                }
                break;
            case "JSON":
                JSONGetXrayerHandleLocation(xrayerUUID, callback);
            default:
                break;
        }
    }

    //Deletes xrayer with specified UUID from memory
    public void DeleteXrayer(String xrayerUUID) {
        switch (module.getStorageType()) {
            case "MYSQL":
                try (Connection cn = dataSource.getConnection()) {
                    try {
                        if (cn != null) {
                            SQLDeleteXrayer(cn, xrayerUUID);
                        }
                    } catch (SQLException e) {
                        log.error("e: ", e);
                    }
                } catch (SQLException e) {
                    log.error("e: ", e);
                }
                break;
            case "JSON":
                JSONDeleteXrayer(xrayerUUID);
                break;
            default:
                break;
        }
    }

    //Deletes all registered xrayers (basically leaves memory empty)
    public void DeleteRegisteredXrayers() {
        switch (module.getStorageType()) {
            case "MYSQL":
                try (Connection cn = dataSource.getConnection()) {
                    try {
                        if (cn != null) {
                            SQLDeleteRegistry(cn);
                        }
                    } catch (SQLException e) {
                        log.error("e: ", e);
                    }
                } catch (SQLException e) {
                    log.error("e: ", e);
                }
                break;
            case "JSON":
                JSONStoreInFile("[]");
                break;
            default:
                break;
        }
    }

    //------------------ SQL RELATED OPERATIONS ------------------:

    public void InitializeDataSource() {
        BasicDataSource basicDataSource = new BasicDataSource();

        if (!Objects.equals(module.getConfig().getString("SQLDriverClassName"), "")) basicDataSource.setDriverClassName(module.getConfig().getString("SQLDriverClassName"));
        basicDataSource.setUsername(module.getConfig().getString("SQLUsername"));
        basicDataSource.setPassword(module.getConfig().getString("SQLPassword"));
        basicDataSource.setUrl("jdbc:mysql://" + module.getConfig().getString("SQLHost") + ":" + module.getConfig().getString("SQLPort") + "/" + module.getConfig().getString("SQLDatabaseName") + "?useSSL=false");
        basicDataSource.setMaxTotal(module.getConfig().getInt("SQLMaxActiveConnections"));
        basicDataSource.setTestOnBorrow(true);
        basicDataSource.setValidationQuery("SELECT 1");

        dataSource = basicDataSource;
    }

    public void CloseDataSource() {
        try {
            dataSource.close();
        } catch (SQLException e) {
            log.error("e: ", e);
        }
    }

    //Creates the Xrayers table
    public void SQLCreateTableIfNotExists() {
        try (Connection cn = dataSource.getConnection()) {
            try {
                if (cn != null) {
                    PreparedStatement create = cn.prepareStatement("CREATE TABLE IF NOT EXISTS Xrayers(UUID VARCHAR(36) NOT NULL, Handled INT NOT NULL, FirstHandleTime VARCHAR(32) NOT NULL, HandleLocation VARCHAR(128) NOT NULL, Belongings TEXT NULL, PRIMARY KEY(UUID))");

                    create.executeUpdate();
                }
            } catch (SQLException e) {
                log.error("e: ", e);
            }
        } catch (SQLException e) {
            log.error("e: ", e);
        }
    }

    //Returns true if UUID was found in the database
    private boolean SQLFindUUID(@NonNull Connection connection, String n) throws SQLException {
        PreparedStatement query = connection.prepareStatement("SELECT COUNT(1) FROM Xrayers WHERE UUID = ?");
        query.setString(1, n);

        ResultSet result = query.executeQuery();

        result.next();
        return result.getInt(1) == 1;
    }

    //Stores player name as xrayer and some other info + player belongings if configured (or dummy data if player is null), ONLY IF there isn't UUID related information already stored.
    private void SQLPlayerDataStore(Connection connection, Player player, final HandledPlayerStoreCallback callback) throws SQLException {
        String playerUUID;
        //Assign true UUID?
        if (player != null) playerUUID = player.getUniqueId().toString();
            //Assign a random "fake" UUID?
        else playerUUID = UUID.randomUUID().toString();

        if (!SQLFindUUID(connection, playerUUID)) //Primary key (player UUID) doesn't already exist
        {
            String serializedPlayerLocation;
            //Assign true "serialized" location
            if (player != null) serializedPlayerLocation = player.getLocation().getWorld().getName() + "," + player.getLocation().getX() + "," + player.getLocation().getY() + "," + player.getLocation().getZ() + "," + player.getLocation().getPitch() + "," + player.getLocation().getYaw();
                //Assign default "fake" "serialized" location
            else if (!module.getConfig().getStringList("TrackWorlds").isEmpty()) serializedPlayerLocation = module.getConfig().getStringList("TrackWorlds").getFirst() + ",0.0,0.0,0.0,0.0,0.0";
            else serializedPlayerLocation = "world,0.0,0.0,0.0,0.0,0.0";

            if (module.getConfig().getBoolean("StoreCopy")) //Full store
            {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                LocalDateTime now = LocalDateTime.now();

                PreparedStatement entry = connection.prepareStatement("INSERT INTO Xrayers(UUID, Handled, FirstHandleTime, HandleLocation, Belongings) VALUES(?,?,?,?,?)");
                entry.setString(1, playerUUID);
                entry.setInt(2, 1);
                entry.setString(3, dtf.format(now));
                entry.setString(4, serializedPlayerLocation);
                //Assign true player inventory and equipment
                if (player != null) entry.setString(5, BukkitSerializer.itemStackArrayToBase64(BukkitSerializer.InventoryAndEquipmentToSingleItemStackArray(player.getInventory(), player.getEquipment())));
                    //Assign "fake" player inventory and equipment
                else {
                    Inventory dummyInventory = new DummyInventory();
                    EntityEquipment dummyEquipment = new DummyEquipment();
                    entry.setString(5, BukkitSerializer.itemStackArrayToBase64(BukkitSerializer.InventoryAndEquipmentToSingleItemStackArray(dummyInventory, dummyEquipment)));
                }

                entry.executeUpdate();
            } else //Partial store
            {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                LocalDateTime now = LocalDateTime.now();

                PreparedStatement entry = connection.prepareStatement("INSERT INTO Xrayers(UUID, Handled, FirstHandleTime, HandleLocation) VALUES(?,?,?,?)");
                entry.setString(1, playerUUID);
                entry.setInt(2, 1);
                entry.setString(3, dtf.format(now));
                entry.setString(4, serializedPlayerLocation);

                entry.executeUpdate();
            }

            if (callback != null) {
                //Callback to main thread returns extracted data
                Bukkit.getScheduler().runTask(module.getHostPlugin(), () -> callback.onInsertDone(1));
            }
        } else //Primary key (player UUID) already exists
        {
            //Add +1 to Handled column
            PreparedStatement update = connection.prepareStatement("UPDATE Xrayers SET Handled = Handled + 1 WHERE UUID = ?");
            update.setString(1, playerUUID);

            update.executeUpdate();

            //Get Handled column value
            PreparedStatement query = connection.prepareStatement("SELECT Handled FROM Xrayers WHERE UUID = ?");
            query.setString(1, playerUUID);

            ResultSet result = query.executeQuery();

            result.next();

            if (callback != null) {
                final int timesHandledFinal = result.getInt("Handled");
                //Callback to main thread returns extracted data
                Bukkit.getScheduler().runTask(module.getHostPlugin(), () -> callback.onInsertDone(timesHandledFinal));
            }
        }
    }

    private void SQLGetAllBaseXrayerData(java.sql.@NonNull Connection connection, final HandledPlayerListCallback callback) throws SQLException //Returns all the basic xrayer information (pretty much everything except for the inventory and handlecoordinates)
    {
        PreparedStatement entry = connection.prepareStatement("SELECT UUID, Handled, FirstHandleTime FROM Xrayers");

        ResultSet result = entry.executeQuery();

        ArrayList<String> UUIDs = new ArrayList<>();
        ArrayList<Integer> handledAmounts = new ArrayList<>();
        ArrayList<String> firstHandledTimes = new ArrayList<>();

        while (result.next()) {
            UUIDs.add(result.getString("UUID"));
            handledAmounts.add(result.getInt("Handled"));
            firstHandledTimes.add(result.getString("FirstHandleTime"));
        }

        //We send the extracted data to the vault array from here:
        module.handledPlayerVault.SubstituteXrayerInfoLists(UUIDs, handledAmounts, firstHandledTimes);
        //Callback to main thread
        Bukkit.getScheduler().runTask(module.getHostPlugin(), callback::onQueryDone);
    }

    private void SQLGetXrayerBelongings(java.sql.@NonNull Connection connection, String xrayerUUID, HandledPlayerBelongingsCallback callback) throws SQLException //Gets an xrayer player's (by UUID) confiscated belongings
    {
        PreparedStatement query = connection.prepareStatement("SELECT Belongings FROM Xrayers WHERE UUID = ?");
        query.setString(1, xrayerUUID);

        ResultSet result = query.executeQuery();

        result.next();

        try {
            final ItemStack[] belongings = BukkitSerializer.itemStackArrayFromBase64(result.getString("Belongings"));
            //Callback to main thread returns extracted data
            Bukkit.getScheduler().runTask(module.getHostPlugin(), () -> callback.onQueryDone(belongings));
        } catch (IOException e) {
            log.error("e: ", e);
        }
    }

    private void SQLGetXrayerHandleLocation(java.sql.@NonNull Connection connection, String xrayerUUID, HandledPlayerLocationCallback callback) throws SQLException //Gets an xrayer player's (by UUID) handle location
    {
        PreparedStatement query = connection.prepareStatement("SELECT HandleLocation FROM Xrayers WHERE UUID = ?");
        query.setString(1, xrayerUUID);

        ResultSet result = query.executeQuery();

        result.next();

        //Deserialize obtained location string:
        String[] serializedPlayerLocation = result.getString("HandleLocation").split(",");
        final Location deserializedHandleLocation = new Location(Bukkit.getWorld(serializedPlayerLocation[0]), Double.parseDouble(serializedPlayerLocation[1]), Double.parseDouble(serializedPlayerLocation[2]), Double.parseDouble(serializedPlayerLocation[3]), Float.parseFloat(serializedPlayerLocation[4]), Float.parseFloat(serializedPlayerLocation[5]));
        //Callback to main thread returns extracted data
        Bukkit.getScheduler().runTask(module.getHostPlugin(), () -> callback.onQueryDone(deserializedHandleLocation));
    }

    private void SQLDeleteXrayer(java.sql.@NonNull Connection connection, String xrayerUUID) throws SQLException //Removes player (by UUID) from xrayers database
    {
        PreparedStatement purge = connection.prepareStatement("DELETE FROM Xrayers WHERE UUID = ?");
        purge.setString(1, xrayerUUID);

        purge.executeUpdate();
    }

    private void SQLDeleteRegistry(java.sql.@NonNull Connection connection) throws SQLException //Truncates the whole Xrayers table, basically emptying all registered xrayers
    {
        PreparedStatement purge = connection.prepareStatement("TRUNCATE TABLE Xrayers");

        purge.executeUpdate();
    }


    //------------------ JSON RELATED OPERATIONS ------------------

    //Java File I.O. functions

    private @NonNull File getJsonDataFile() {
        return module.getPluginDataFile("data.json");
    }

    public void JSONFileCreateIfNotExists() //Returns true if file was created
    {
        try {
            //Create file (will do nothing if it already exists):
            if (getJsonDataFile().createNewFile()) {
                JSONStoreInFile("[]");
            } //Store empty JSON array in file
        } catch (IOException e) {
            log.error("Error: ", e);
        }
    }

    private void JSONStoreInFile(String toStore) //Writes JSON content as string to file
    {
        try {
            FileWriter writer = new FileWriter(getJsonDataFile());
            writer.write(toStore);
            writer.close();
        } catch (IOException e) {
            log.error("Error: ", e);
        }
    }

    private @Nullable BufferedReader JSONGetFromFile() //Gets JSON content from file in BufferedReader format
    {
        try {
            return new BufferedReader(new FileReader(getJsonDataFile())); //Return buffered file contents
        } catch (IOException e) {
            log.error("Error: ", e);
            return null;
        }
    }

    //GSON Serialization functions

    private @NonNull String JSONSerializeXrayersData(List<HandledPlayerRecord> xrayers) //Serializes public class ArrayList to JSON String
    {
        return new Gson().toJson(xrayers);
    }

    private List<HandledPlayerRecord> JSONDeserializeXrayersData(BufferedReader xrayers) //Serializes from BufferedReader to ArrayList.
    {
        return new Gson().fromJson(xrayers, new TypeToken<ArrayList<HandledPlayerRecord>>() {
        }.getType()); //TypeToken gets the type of arraylist of xrayers
    }

    //File data manipulation and querying using GSON

    private void JSONRefreshLoadedXrayerData() //Loads JSON data from file and converts it to List, assigning it to storedXrayersFromJSON, consequently refreshing it in RAM Stack
    {
        storedXrayersFromJSON = JSONDeserializeXrayersData(JSONGetFromFile());
    }

    public void JSONFlushLoadedXrayerData() //Removes the loaded xrayer data from memory. Used for when nothing is actually using it (no one's reading it, definitely).
    {
        storedXrayersFromJSON.clear();
    }

    private void JSONPlayerDataStore(Player player, final HandledPlayerStoreCallback callback) //Stores player name as xrayer and some other info (+ player belongings if configured), ONLY IF there isn't information already stored. Also notifies through callback on finish
    {
        String playerUUID;
        //Assign true UUID?
        if (player != null) playerUUID = player.getUniqueId().toString();
            //Assign a random "fake" UUID?
        else playerUUID = UUID.randomUUID().toString();

        //Handled times for returning:
        int timesHandled = 0;

        //Refresh loaded xrayers in RAM Stack:
        JSONRefreshLoadedXrayerData();
        //Check if xrayer is already stored?:
        boolean exists = false;
        for (HandledPlayerRecord xrayer : storedXrayersFromJSON) {
            if (xrayer.UUID.equals(playerUUID)) {
                exists = true;
                //Also add +1 to handled:
                xrayer.Handled += 1;
                timesHandled = xrayer.Handled;
                //Store List back to file:
                String serial = JSONSerializeXrayersData(storedXrayersFromJSON);
                JSONStoreInFile(serial);
                break;
            }
        }
        if (!exists) { //player UUID doesn't already exist in file

            String serializedPlayerLocation;
            //Assign true "serialized" location
            if (player != null) serializedPlayerLocation = player.getLocation().getWorld().getName() + "," + player.getLocation().getX() + "," + player.getLocation().getY() + "," + player.getLocation().getZ() + "," + player.getLocation().getPitch() + "," + player.getLocation().getYaw();
                //Assign default "fake" "serialized" location
            else if (!module.getConfig().getStringList("TrackWorlds").isEmpty()) serializedPlayerLocation = module.getConfig().getStringList("TrackWorlds").getFirst() + ",0.0,0.0,0.0,0.0,0.0";
            else serializedPlayerLocation = "world,0.0,0.0,0.0,0.0,0.0";

            if (module.getConfig().getBoolean("StoreCopy")) //Full store
            {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                LocalDateTime now = LocalDateTime.now();

                //Add xrayer to List:
                //Existing inventory and equipment
                if (player != null) storedXrayersFromJSON.add(new HandledPlayerRecord(playerUUID, 1, dtf.format(now), serializedPlayerLocation, BukkitSerializer.itemStackArrayToBase64(BukkitSerializer.InventoryAndEquipmentToSingleItemStackArray(player.getInventory(), player.getEquipment()))));
                    //Made up inventory and equipment
                else {
                    Inventory dummyInventory = new DummyInventory();
                    EntityEquipment dummyEquipment = new DummyEquipment();
                    storedXrayersFromJSON.add(new HandledPlayerRecord(playerUUID, 1, dtf.format(now), serializedPlayerLocation, BukkitSerializer.itemStackArrayToBase64(BukkitSerializer.InventoryAndEquipmentToSingleItemStackArray(dummyInventory, dummyEquipment))));
                }
                //Store List back to file:
                String serial = JSONSerializeXrayersData(storedXrayersFromJSON);
                JSONStoreInFile(serial);
            } else //Partial store
            {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                LocalDateTime now = LocalDateTime.now();

                //Add xrayer to List:
                storedXrayersFromJSON.add(new HandledPlayerRecord(playerUUID, 1, dtf.format(now), serializedPlayerLocation, null));
                //Store List back to file:
                JSONStoreInFile(JSONSerializeXrayersData(storedXrayersFromJSON));
            }

            if (callback != null) {
                //Callback to main thread returns extracted data
                Bukkit.getScheduler().runTask(module.getHostPlugin(), () -> callback.onInsertDone(1));
            }
        } else if (callback != null) {
            final int timesHandledFinal = timesHandled;
            //Callback to main thread returns extracted data
            Bukkit.getScheduler().runTask(module.getHostPlugin(), () -> callback.onInsertDone(timesHandledFinal));
        }
    }

    private void JSONGetAllBaseXrayerData(HandledPlayerListCallback callback) //Returns all the basic xrayer information (pretty much everything except for the inventory and handlecoordinates)
    {
        //Refresh loaded xrayers in RAM Stack:
        JSONRefreshLoadedXrayerData();
        //Extract information from storedXrayersFromJSON:

        ArrayList<String> UUIDs = new ArrayList<>();
        ArrayList<Integer> handledAmounts = new ArrayList<>();
        ArrayList<String> firstHandledTimes = new ArrayList<>();

        for (HandledPlayerRecord xrayer : storedXrayersFromJSON) {
            UUIDs.add(xrayer.UUID);
            handledAmounts.add(xrayer.Handled);
            firstHandledTimes.add(xrayer.FirstHandleTime);
        }

        //We send the extracted data to the vault array from here:
        module.handledPlayerVault.SubstituteXrayerInfoLists(UUIDs, handledAmounts, firstHandledTimes);
        //Callback to main thread
        Bukkit.getScheduler().runTask(module.getHostPlugin(), callback::onQueryDone);
    }

    private void JSONGetXrayerBelongings(String xrayerUUID, HandledPlayerBelongingsCallback callback) {
        //Refresh loaded xrayers in RAM Stack:
        JSONRefreshLoadedXrayerData();
        //Find uuid, and return its belongings:
        for (HandledPlayerRecord xrayer : storedXrayersFromJSON) {
            if (xrayer.UUID.equals(xrayerUUID)) {
                try {
                    final ItemStack[] belongings = BukkitSerializer.itemStackArrayFromBase64(xrayer.Belongings);
                    //Callback to main thread returns extracted data
                    Bukkit.getScheduler().runTask(module.getHostPlugin(), () -> callback.onQueryDone(belongings));
                } catch (IOException e) {
                    log.error("e: ", e);
                }
            }
        }
    }

    private void JSONGetXrayerHandleLocation(String xrayerUUID, HandledPlayerLocationCallback callback) {
        //Refresh loaded xrayers in RAM Stack:
        JSONRefreshLoadedXrayerData();
        //Find uuid, and return its handle location:
        for (HandledPlayerRecord xrayer : storedXrayersFromJSON) {
            if (xrayer.UUID.equals(xrayerUUID)) {

                //Deserialize obtained location string:
                String[] serializedPlayerLocation = xrayer.HandleLocation.split(",");
                final Location deserializedHandleLocation = new Location(Bukkit.getWorld(serializedPlayerLocation[0]), Double.parseDouble(serializedPlayerLocation[1]), Double.parseDouble(serializedPlayerLocation[2]), Double.parseDouble(serializedPlayerLocation[3]), Float.parseFloat(serializedPlayerLocation[4]), Float.parseFloat(serializedPlayerLocation[5]));
                //Callback to main thread returns extracted data
                Bukkit.getScheduler().runTask(module.getHostPlugin(), () -> callback.onQueryDone(deserializedHandleLocation));
            }
        }
    }

    private void JSONDeleteXrayer(String xrayerUUID) {
        //Refresh loaded xrayers in RAM Stack:
        JSONRefreshLoadedXrayerData();
        for (HandledPlayerRecord xrayer : storedXrayersFromJSON) {
            if (xrayer.UUID.equals(xrayerUUID)) {
                storedXrayersFromJSON.remove(xrayer);
                //Store List back to file:
                JSONStoreInFile(JSONSerializeXrayersData(storedXrayersFromJSON));
                break;
            }
        }
    }
}
