package com.onemoreblock.coreprotectaddons.xrayheuristics.smoke;

import com.google.gson.Gson;
import com.onemoreblock.coreprotectaddons.xrayheuristics.XRayHeuristicsModule;
import com.onemoreblock.coreprotectaddons.xrayheuristics.XRayHeuristicsPlugin;
import com.onemoreblock.coreprotectaddons.xrayheuristics.handling.HandledPlayerRecord;
import com.onemoreblock.coreprotectaddons.xrayheuristics.util.BukkitSerializer;
import io.papermc.paper.datacomponent.DataComponentTypes;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.java.JavaPlugin;

/** Runs only in the fresh loopback server created by scripts/test-paper-stable.py. */
public final class CompatibilityProbe extends JavaPlugin {
    private static final String FIXTURE_UUID = "2fd6d450-7530-4a65-9f1b-d3b5c02a96da";
    private XRayHeuristicsPlugin target;
    private XRayHeuristicsModule module;
    private int assertions;

    @Override
    public void onEnable() {
        // An explicit launch flag prevents accidental fixture writes on another server.
        if (!Boolean.getBoolean("axh.smoke.disposable")) {
            getLogger().severe("Refusing to run outside the disposable compatibility server.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        Bukkit.getScheduler().runTaskLater(this, () -> guarded(this::verify), 40);
    }

    private void verify() throws Exception {
        require(Bukkit.isPrimaryThread(), "Main-thread probe");
        target = (XRayHeuristicsPlugin) Bukkit.getPluginManager().getPlugin("xrayheuristics");
        require(target != null && target.isEnabled(), "Plugin enabled");
        module = target.getModule();
        require(module.getBuildMetadata().paperApiChannel().equals("stable"), "Stable API metadata");
        require(module.getCoreProtectHook().isHooked(), "CoreProtect hook");
        require(module.getCoreProtectHook().getCoreProtectVersion().equals("25.0"), "CoreProtect 25.0");
        require(module.getCoreProtectHook().getCoreProtectApiVersion().equals("13"), "CoreProtect API 13");
        require(target.getApi() != null && target.GetAPI() != null, "Canonical and legacy APIs");
        verifyMiningAndPermissions();
        ItemStack[] expected = fixtureItems();
        String encoded = BukkitSerializer.itemStackArrayToBase64(expected);
        verifyItems(expected, BukkitSerializer.itemStackArrayFromBase64(encoded));
        require(BukkitSerializer.itemStackArrayFromBase64(null) == null, "Absent stored belongings");
        try {
            BukkitSerializer.itemStackArrayFromBase64("%%%invalid-base64%%%");
            throw new AssertionError("Corrupt belongings accepted");
        } catch (IOException expectedFailure) {
            assertions++;
        }
        // Detached fixture construction stays on the server thread; disk access runs asynchronously.
        String json = new Gson().toJson(List.of(new HandledPlayerRecord(FIXTURE_UUID, 2,
                "2026/10/11 12:00:00", "world,0.0,-50.0,0.0,0.0,0.0", encoded)));
        async(() -> {
            if (!Boolean.getBoolean("axh.smoke.restart")) {
                Files.writeString(module.getPluginDataFile("data.json").toPath(), json);
            }
            module.handledPlayerStore.GetXrayerBelongings(FIXTURE_UUID, items -> guarded(() -> {
                require(Bukkit.isPrimaryThread(), "Storage callback returns to main thread");
                verifyItems(expected, items);
                verifyStoredRecords();
            }));
        });
    }

    private void verifyMiningAndPermissions() throws Exception {
        World world = Bukkit.getWorld("world");
        require(world != null, "Disposable world");
        module.getConfig().set("TrackWorlds", List.of(world.getName()));
        Block block = world.getBlockAt(0, -50, 0);
        BlockData original = block.getBlockData();
        List<String> messages = new ArrayList<>();
        Player miner = player("CompatibilityMiner", world, false, messages);
        Player ignored = player(".CompatibilityBedrock", world, true, messages);
        try {
            block.setType(Material.STONE, false);
            fire(new BlockBreakEvent(block, ignored));
            require(!module.sessions.containsKey(ignored.getName()), "Ignore permission bypasses mining");
            BlockBreakEvent cancelled = new BlockBreakEvent(block, miner);
            cancelled.setCancelled(true);
            fire(cancelled);
            require(!module.sessions.containsKey(miner.getName()), "Cancelled mining creates no session");
            for (int i = 0; i <= module.getMinimumBlocksMinedToNextVein(); i++) {
                fire(new BlockBreakEvent(block, miner));
            }
            require(module.sessions.containsKey(miner.getName()), "Mining creates a Suspicion Session");
            float before = module.sessions.get(miner.getName()).GetSuspicionLevel();
            block.setType(Material.DIAMOND_ORE, false);
            fire(new BlockBreakEvent(block, miner));
            require(module.sessions.get(miner.getName()).GetSuspicionLevel() > before, "Ore raises suspicion");
            for (String command : List.of("vault", "reload", "resetsuspicion", "suspicion", "debug")) {
                messages.clear();
                target.getCommand("xrayer").execute(miner, "xrayer", new String[]{command});
                require(messages.stream().anyMatch(message -> message.contains("do not have permission")),
                        "Denied command reports missing permission: " + command);
            }
            require(module.sessions.containsKey(miner.getName()), "Denied reset preserves session");
            module.reloadPluginState();
            require(module.getCoreProtectHook().isHooked(), "CoreProtect hook survives reload");
            require(module.sessions.containsKey(miner.getName()), "Reload preserves session");
        } finally {
            block.setBlockData(original, false);
            module.sessions.remove(miner.getName());
            module.sessions.remove(ignored.getName());
            module.reloadPluginState();
        }
        getLogger().info("PROBE mining, cancellation, ignore permission, denied commands and reload passed");
    }

    private Player player(String name, World world, boolean ignored, List<String> messages) {
        UUID uuid = UUID.randomUUID(); // Synthetic test entity only; never an identity lookup.
        return (Player) Proxy.newProxyInstance(Player.class.getClassLoader(), new Class<?>[]{Player.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getName", "toString" -> name;
                    case "getUniqueId" -> uuid;
                    case "getWorld" -> world;
                    case "getLocation" -> world.getBlockAt(0, -50, 0).getLocation();
                    case "hasPermission" -> ignored && "xrayheuristics.ignore".equals(args[0]);
                    case "isOp" -> false;
                    case "sendMessage" -> {
                        for (Object argument : args) {
                            if (argument instanceof Component component) {
                                messages.add(PlainTextComponentSerializer.plainText().serialize(component));
                            } else if (argument instanceof String message) {
                                messages.add(message);
                            }
                        }
                        yield null;
                    }
                    case "hashCode" -> uuid.hashCode();
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException("Unexpected player call: " + method.getName());
                });
    }

    private void fire(BlockBreakEvent event) throws Exception {
        int called = 0;
        for (RegisteredListener listener : BlockBreakEvent.getHandlerList().getRegisteredListeners()) {
            if (listener.getPlugin() == target) {
                listener.callEvent(event);
                called++;
            }
        }
        require(called > 0, "Paper mining listener registered");
    }

    private ItemStack[] fixtureItems() {
        ItemStack item = ItemStack.of(Material.DIAMOND, 3);
        item.editMeta(meta -> {
            meta.displayName(Component.text("Foreign plugin diamond"));
            meta.lore(List.of(Component.text("Metadata must survive storage")));
            meta.getPersistentDataContainer().set(new NamespacedKey("foreignplugin", "identity"),
                    PersistentDataType.STRING, "fixture-033");
        });
        item.setData(DataComponentTypes.MAX_STACK_SIZE, 16);
        ItemStack container = ItemStack.of(Material.SHULKER_BOX);
        BlockStateMeta meta = (BlockStateMeta) container.getItemMeta();
        ShulkerBox state = (ShulkerBox) meta.getBlockState();
        state.getInventory().setItem(4, item.clone());
        meta.setBlockState(state);
        container.setItemMeta(meta);
        ItemStack[] items = new ItemStack[41];
        items[0] = item;
        items[5] = container;
        items[36] = ItemStack.of(Material.SHIELD);
        items[37] = ItemStack.of(Material.DIAMOND_HELMET);
        return items;
    }

    private void verifyItems(ItemStack[] expected, ItemStack[] actual) {
        require(actual != null && actual.length == 41, "All inventory/equipment slots retained");
        for (int i = 0; i < expected.length; i++) {
            require(expected[i] == null ? actual[i].isEmpty() : expected[i].equals(actual[i]),
                    "Slot " + i + " preserves amount, components, PDC and container contents");
        }
    }

    private void verifyStoredRecords() {
        async(() -> module.handledPlayerStore.GetAllBaseXrayerData(() -> guarded(() -> {
            require(Bukkit.isPrimaryThread(), "Record callback on main thread");
            require(module.handledPlayerVault.GetUUIDs().equals(List.of(FIXTURE_UUID)), "Persisted fixture identity");
            module.getConfig().set("StoreCopy", false);
            async(() -> module.handledPlayerStore.StoreDummyPlayerData(count -> guarded(() -> {
                require(Bukkit.isPrimaryThread() && count == 1, "Asynchronous insert callback");
                async(() -> module.handledPlayerStore.GetAllBaseXrayerData(() -> guarded(() -> {
                    var ids = new ArrayList<>(module.handledPlayerVault.GetUUIDs());
                    require(ids.size() == 2, "Stored dummy record");
                    ids.remove(FIXTURE_UUID);
                    async(() -> {
                        module.handledPlayerStore.DeleteXrayer(ids.getFirst());
                        module.handledPlayerStore.GetAllBaseXrayerData(() -> guarded(() -> {
                            require(module.handledPlayerVault.GetUUIDs().equals(List.of(FIXTURE_UUID)), "Purge preserves fixture");
                            module.reloadPluginState();
                            getLogger().info("COMPATIBILITY_PROBE_PASS assertions=" + assertions
                                    + " runtime=" + System.getProperty("java.runtime.version"));
                        }));
                    });
                })));
            })));
        })));
    }

    private void async(CheckedAction action) {
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> guarded(action));
    }

    private void guarded(CheckedAction action) {
        try {
            action.run();
        } catch (Throwable failure) {
            getLogger().log(Level.SEVERE, "COMPATIBILITY_PROBE_FAIL", failure);
        }
    }

    private void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
        assertions++;
    }

    @FunctionalInterface
    private interface CheckedAction { void run() throws Exception; }
}
