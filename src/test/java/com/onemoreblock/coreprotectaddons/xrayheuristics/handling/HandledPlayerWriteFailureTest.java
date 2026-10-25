package com.onemoreblock.coreprotectaddons.xrayheuristics.handling;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.onemoreblock.coreprotectaddons.xrayheuristics.XRayHeuristicsModule;
import com.onemoreblock.coreprotectaddons.xrayheuristics.manager.LocaleManager;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedStatic;

/** Exercises the real handling/store flow; only Bukkit's external runtime is doubled. */
class HandledPlayerWriteFailureTest {
    private static final UUID PLAYER_ID = UUID.fromString("2fd6d450-7530-4a65-9f1b-d3b5c02a96da");
    private static final String PLAYER_NAME = ".bedrock";

    @TempDir
    Path dataDirectory;

    private final Queue<Runnable> storageTasks = new ArrayDeque<>();
    private final Queue<Runnable> mainTasks = new ArrayDeque<>();
    private final List<String> dispatchedCommands = new ArrayList<>();
    private final ItemStack[] inventoryContents = new ItemStack[36];
    private final ItemStack[] equipmentContents = new ItemStack[5];
    private XRayHeuristicsModule module;
    private Path dataFile;
    private MockedStatic<Bukkit> bukkit;

    @BeforeEach
    void setUpHandling() throws IOException {
        bukkit = mockStatic(Bukkit.class);
        var server = mock(Server.class);
        bukkit.when(Bukkit::getServer).thenReturn(server);
        var scheduler = mock(BukkitScheduler.class);
        var task = mock(BukkitTask.class);
        bukkit.when(Bukkit::getScheduler).thenReturn(scheduler);
        when(server.getPluginManager()).thenReturn(mock(PluginManager.class));
        when(scheduler.runTaskAsynchronously(any(), any(Runnable.class))).thenAnswer(call -> {
            storageTasks.add(call.getArgument(1, Runnable.class));
            return task;
        });
        when(scheduler.runTask(any(), any(Runnable.class))).thenAnswer(call -> {
            mainTasks.add(call.getArgument(1, Runnable.class));
            return task;
        });

        var console = mock(ConsoleCommandSender.class);
        bukkit.when(Bukkit::getConsoleSender).thenReturn(console);
        bukkit.when(() -> Bukkit.dispatchCommand(eq(console), anyString())).thenAnswer(call -> {
            dispatchedCommands.add(call.getArgument(1, String.class));
            return true;
        });

        var inventory = mock(PlayerInventory.class);
        var equipment = mock(EntityEquipment.class);
        inventoryContents[0] = item(new byte[] {1, 2, 3});
        equipmentContents[0] = item(new byte[] {4, 5, 6});
        when(inventory.getItem(anyInt())).thenAnswer(call -> inventoryContents[call.getArgument(0, Integer.class)]);
        when(equipment.getItemInOffHand()).thenAnswer(_ -> equipmentContents[0]);
        when(equipment.getHelmet()).thenAnswer(_ -> equipmentContents[1]);
        when(equipment.getChestplate()).thenAnswer(_ -> equipmentContents[2]);
        when(equipment.getLeggings()).thenAnswer(_ -> equipmentContents[3]);
        when(equipment.getBoots()).thenAnswer(_ -> equipmentContents[4]);
        doAnswer(_ -> {
            Arrays.fill(inventoryContents, null);
            return null;
        }).when(inventory).clear();
        doAnswer(_ -> {
            Arrays.fill(equipmentContents, null);
            return null;
        }).when(equipment).clear();

        var world = mock(World.class);
        when(world.getName()).thenReturn("world");
        var player = mock(Player.class);
        when(player.getName()).thenReturn(PLAYER_NAME);
        when(player.getUniqueId()).thenReturn(PLAYER_ID);
        when(player.getLocation()).thenReturn(new Location(world, 1, 2, 3));
        when(player.getInventory()).thenReturn(inventory);
        when(player.getEquipment()).thenReturn(equipment);
        bukkit.when(() -> Bukkit.getPlayer(PLAYER_NAME)).thenReturn(player);

        Files.writeString(dataDirectory.resolve("config.yml"), """
                StorageType: JSON
                StoreCopy: true
                CleansePlayerItems: true
                UseHeadsInGUI: false
                SendMessageToPlayer: false
                TellPlayersWithPermission: false
                NullifySuspicionAfterPunish: false
                CommandsExecutedOnXrayerDetected:
                  1:
                    1: 'test-handle {PlayerName} {TimesDetected}'
                  3:
                    1: 'test-handle {PlayerName} {TimesDetected}'
                """);
        var plugin = mock(JavaPlugin.class);
        when(plugin.getLogger()).thenReturn(Logger.getLogger("HandledPlayerWriteFailureTest"));
        module = new XRayHeuristicsModule(plugin, dataDirectory.toFile());
        LocaleManager.setup(dataDirectory.toFile());
        dataFile = dataDirectory.resolve("data.json");
    }

    @AfterEach
    void restoreFilePermissions() throws IOException {
        if (bukkit != null) {
            bukkit.close();
        }
        if (dataFile != null && Files.exists(dataFile)) {
            Files.setPosixFilePermissions(dataFile, PosixFilePermissions.fromString("rw-------"));
        }
    }

    @ParameterizedTest
    @CsvSource({"false,true", "true,true", "false,false", "true,false"})
    void failedJsonWritePreservesInventoryAndEquipmentAndSkipsCommands(boolean existingRecord, boolean storeCopy)
            throws IOException {
        prepareRecord(existingRecord, storeCopy);
        Files.setPosixFilePermissions(dataFile, PosixFilePermissions.fromString("r--------"));
        assertFalse(Files.isWritable(dataFile), "The regression fixture must reject writes while remaining readable");
        var originalInventory = inventoryContents.clone();
        var originalEquipment = equipmentContents.clone();

        module.getHandledPlayerService().handlePlayer(PLAYER_NAME);
        drainTasks();

        assertAll(
                () -> assertArrayEquals(originalInventory, inventoryContents, "Failed storage must preserve inventory"),
                () -> assertArrayEquals(originalEquipment, equipmentContents, "Failed storage must preserve equipment"),
                () -> assertTrue(dispatchedCommands.isEmpty(), "Failed storage must not run handling commands")
        );
    }

    @ParameterizedTest
    @CsvSource({"false,true", "true,true", "false,false", "true,false"})
    void successfulJsonWriteStillAllowsConfiguredHandling(boolean existingRecord, boolean storeCopy) throws IOException {
        prepareRecord(existingRecord, storeCopy);

        module.getHandledPlayerService().handlePlayer(PLAYER_NAME);
        drainTasks();

        int expectedHandleCount = existingRecord ? 3 : 1;
        assertAll(
                () -> assertArrayEquals(new ItemStack[36], inventoryContents, "Successful handling clears inventory"),
                () -> assertArrayEquals(new ItemStack[5], equipmentContents, "Successful handling clears equipment"),
                () -> assertEquals(List.of("test-handle .bedrock " + expectedHandleCount), dispatchedCommands)
        );
    }

    private void prepareRecord(boolean existingRecord, boolean storeCopy) throws IOException {
        module.getConfig().set("StoreCopy", storeCopy);
        Files.writeString(dataFile, existingRecord ? """
                [{"UUID":"2fd6d450-7530-4a65-9f1b-d3b5c02a96da","Handled":2,
                  "FirstHandleTime":"2026/09/25 12:00:00","HandleLocation":"world,1,2,3,0,0","Belongings":null}]
                """ : "[]");
    }

    private void drainTasks() {
        while (!storageTasks.isEmpty() || !mainTasks.isEmpty()) {
            while (!storageTasks.isEmpty()) {
                storageTasks.remove().run();
            }
            while (!mainTasks.isEmpty()) {
                mainTasks.remove().run();
            }
        }
    }

    private static ItemStack item(byte[] serialized) {
        var item = mock(ItemStack.class);
        when(item.isEmpty()).thenReturn(false);
        when(item.serializeAsBytes()).thenReturn(serialized);
        return item;
    }
}
