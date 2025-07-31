//--------------------------------------------------------------------
// Copyright © Dylan Calaf Latham 2019-2021 AntiXrayHeuristics
//--------------------------------------------------------------------

package com.greymagic27;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

public class XrayerVault {
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();
    public final ItemStack separator;
    public final ItemStack nextButton;
    public final ItemStack prevButton;
    public final ItemStack purgeButton;
    public final ItemStack refreshButton;
    public final ItemStack backButton;
    public final ItemStack purgePlayerButton;
    public final ItemStack absolvePlayerButton;
    private final AntiXrayHeuristics mainClassAccess;
    //The following 4 list's values are parallel, and represent xrayer information. The first 3 are usually filled from
    //persistent memory, while the last one is filled by querying heads
    private final ArrayList<String> UUIDs = new ArrayList<>();
    private final ArrayList<Integer> handledAmounts = new ArrayList<>();
    private final ArrayList<String> firstHandledTimes = new ArrayList<>();
    private final List<ItemStack> xrayerSkulls = new ArrayList<>();
    private final String GUITitle;
    private final HashMap<String, PlayerViewInfo> viewers = new HashMap<>(); //Stores who's viewing the GUI and info on what's being looked at.
    private int pages;

    public XrayerVault(AntiXrayHeuristics main) {
        this.mainClassAccess = main;

        GUITitle = PlaceholderManager.SubstituteColorCodePlaceholders(LocaleManager.get().getString("GUITitle"));

        separator = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        nextButton = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        prevButton = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        purgeButton = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        refreshButton = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
        backButton = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        purgePlayerButton = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        absolvePlayerButton = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);

        ItemMeta separator_meta = separator.getItemMeta();
        separator_meta.displayName(Component.text(" "));
        separator.setItemMeta(separator_meta);
        Function<String, Component> fromLocale = key -> {
            String raw = Objects.requireNonNull(LocaleManager.get().getString(key));
            return LEGACY_SERIALIZER.deserialize(raw);
        };

        ItemMeta next_meta = nextButton.getItemMeta();
        next_meta.displayName(fromLocale.apply("NextButtonTitle"));
        nextButton.setItemMeta(next_meta);

        ItemMeta prev_meta = prevButton.getItemMeta();
        prev_meta.displayName(fromLocale.apply("BackButtonTitle"));
        prevButton.setItemMeta(prev_meta);

        ItemMeta purge_meta = purgeButton.getItemMeta();
        purge_meta.displayName(fromLocale.apply("PurgeButtonTitle"));
        List<Component> purgeLore = LocaleManager.get().getStringList("PurgeButtonDesc").stream().map(LEGACY_SERIALIZER::deserialize).collect(Collectors.toList());
        purge_meta.lore(purgeLore);
        purgeButton.setItemMeta(purge_meta);

        ItemMeta refresh_meta = refreshButton.getItemMeta();
        refresh_meta.displayName(fromLocale.apply("RefreshButtonTitle"));
        List<Component> refreshLore = LocaleManager.get().getStringList("RefreshButtonDesc").stream().map(LEGACY_SERIALIZER::deserialize).collect(Collectors.toList());
        refresh_meta.lore(refreshLore);
        refreshButton.setItemMeta(refresh_meta);

        ItemMeta back_meta = backButton.getItemMeta();
        back_meta.displayName(fromLocale.apply("GoBackButtonTitle"));
        backButton.setItemMeta(back_meta);

        ItemMeta purgeplayer_meta = purgePlayerButton.getItemMeta();
        purgeplayer_meta.displayName(fromLocale.apply("PurgePlayerButtonTitle"));
        List<Component> purgePlayerLore = LocaleManager.get().getStringList("PurgePlayerButtonDesc").stream().map(LEGACY_SERIALIZER::deserialize).collect(Collectors.toList());
        purgeplayer_meta.lore(purgePlayerLore);
        purgePlayerButton.setItemMeta(purgeplayer_meta);

        ItemMeta absolveplayer_meta = absolvePlayerButton.getItemMeta();
        absolveplayer_meta.displayName(fromLocale.apply("AbsolvePlayerButtonTitle"));
        List<Component> absolvePlayerLore = LocaleManager.get().getStringList("AbsolvePlayerButtonDesc").stream().map(LEGACY_SERIALIZER::deserialize).collect(Collectors.toList());
        absolveplayer_meta.lore(absolvePlayerLore);
        absolvePlayerButton.setItemMeta(absolveplayer_meta);

    }

    public String GetGUITitle() {
        return GUITitle;
    }

    public List<String> GetUUIDs() {
        return UUIDs;
    }

    /**
     * Removes all xrayer data from both memory and xrayer vault, and refreshes vault after which sending everyone back to page 0 for safety
     */
    public void PurgeAllXrayersAndRefreshVault() {
        //Dump registered xrayers:
        Bukkit.getScheduler().runTaskAsynchronously(mainClassAccess, mainClassAccess.mm::DeleteRegisteredXrayers);
        //Refresh, just clear the vault lists since we did a global purge, and set pages to 1:
        ClearXrayerInfoLists(true);
        SetPages();
        //Send viewers back to page 0:
        SendAllToPageZero();
    }

    /**
     * Can clear the inspected xrayer that solicitor name is watching (or explicitly defined if nameIsSolicitor = false) from both
     * persistent data storage and data loaded in RAM for vault, and refresh vault, sending all viewers back to page 0 for safety
     *
     * @param name            The name of the player to be cleared from xrayer data
     * @param nameIsSolicitor The method needs to know if the player requesting the data removal is also the one who's data should be removed
     */
    public void XrayerDataRemover(String name, @NotNull Boolean nameIsSolicitor) {
        final String xrayerUUID;
        if (nameIsSolicitor) //Inputted name is the solicitor viewing xrayer through gui. We can get the xrayer's name since it's stored in this same vault
        {
            //Purge player from memory:
            xrayerUUID = GetInspectedXrayer(name);
            Bukkit.getScheduler().runTaskAsynchronously(mainClassAccess, () -> mainClassAccess.mm.DeleteXrayer(xrayerUUID));
            //Remove absolved uuid from being listed in vault:
            RemoveXrayerDataByUUIDFromList(xrayerUUID);
        } else //Should be the actual specific xrayer's name
        {
            //Purge player from memory:
            xrayerUUID = Objects.requireNonNull(Bukkit.getServer().getPlayer(name)).getUniqueId().toString();
            Bukkit.getScheduler().runTaskAsynchronously(mainClassAccess, () -> mainClassAccess.mm.DeleteXrayer(xrayerUUID));
            //Remove absolved uuid from being listed in vault:
            RemoveXrayerDataByUUIDFromList(xrayerUUID);
        }
        //Recalculate pages length:
        CalculatePages();
        //Send viewers back to page 0:
        SendAllToPageZero();
    }

    /**
     * Clears 3 of the data arrays and fills them with input data
     */
    public void SubstituteXrayerInfoLists(List<String> uuids, List<Integer> handledamounts, List<String> firsthandledtimes) {
        //Clear previous data:
        ClearXrayerInfoLists(false);

        //Update with new data:
        UUIDs.addAll(uuids);
        handledAmounts.addAll(handledamounts);
        firstHandledTimes.addAll(firsthandledtimes);
    }

    /**
     * Updates Xrayer information arrays through "SubstituteXayerInfoLists" and also forces page open for player if not null
     */
    public void UpdateXrayerInfoLists(Player player, int page) {
        Bukkit.getScheduler().runTaskAsynchronously(mainClassAccess, () -> mainClassAccess.mm.GetAllBaseXrayerData(() -> {
            OpenVault(player, page);
            Component prefix = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(LocaleManager.get().getString("MessagesPrefix")));
            Component message = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(LocaleManager.get().getString("VaultRefreshed")));
            Component finalMessage = Component.text().append(prefix).append(Component.space()).append(message).build();
            player.sendMessage(finalMessage);
        })); //This single async function fills up 3 xrayer info lists from persistent memory.
    }

    /**
     * Clears persistent memory related xrayer information arraylists
     *
     * @param clearHeadCache true = also clear the vault's xrayer head caché
     */
    public void ClearXrayerInfoLists(boolean clearHeadCache) {
        UUIDs.clear();
        handledAmounts.clear();
        firstHandledTimes.clear();
        if (mainClassAccess.getConfig().getBoolean("UseHeadsInGUI") && clearHeadCache) xrayerSkulls.clear();
    }

    /**
     * Calculates pages considering the amount of registered xrayer uuid's, and that there can only be 45 results per page
     */
    private void CalculatePages() {
        pages = MathFunctions.Cut(45, UUIDs.size());
    }

    /**
     * Sends all vault viewers back to page 0
     */
    private void SendAllToPageZero() {
        for (Map.Entry<String, PlayerViewInfo> entry : viewers.entrySet()) {
            OpenVault(Objects.requireNonNull(Bukkit.getServer().getPlayer(entry.getKey())), 0);
            Player targetPlayer = Bukkit.getServer().getPlayer(entry.getKey());
            if (targetPlayer != null) {
                Component prefix = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(LocaleManager.get().getString("MessagesPrefix")));
                Component forcedPageZero = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(LocaleManager.get().getString("ForcedPageZero")));
                Component message = Component.text().append(prefix).append(Component.space()).append(forcedPageZero).build();
                targetPlayer.sendMessage(message);
            }
        }
    }

    /**
     * Opens xrayer vault for player in a specified vault display page (player head index with info about the xrayers)
     * is often also used for switching pages and forcing viewers towards viewing a certain page
     *
     * @param player The player the vault will open for
     * @param page   What page the player will view
     */
    public void OpenVault(@NotNull Player player, int page) {
        //Recalculate pages length:
        CalculatePages();
        player.playSound(player.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 100, 1);

        Inventory gui = Bukkit.createInventory(null, 54, Component.text(GUITitle + (page + 1) + "/" + pages));
        viewers.put(player.getName(), new PlayerViewInfo(page)); //Register player as gui viewer on a certain page (used as player-page reference)

        if (mainClassAccess.getConfig().getBoolean("UseHeadsInGUI")) {
            if (!xrayerSkulls.isEmpty()) //Head caché has entries: Only construct the vault and then display it to the player
            {
                ConstructVault(gui, page);
                player.openInventory(gui);
            } else //Head caché doesn't have entries: Cache the xrayer heads + construct the vault and display it to the player
            {
                Bukkit.getScheduler().runTaskAsynchronously(mainClassAccess, () -> UpdateXrayerHeadCache(() -> {
                    ConstructVault(gui, page);
                    player.openInventory(gui);
                }));
            }
        } else {
            ConstructVault(gui, page);
            player.openInventory(gui);
        }
    }

    /**
     * Fills up vault gui with xrayer heads from cache
     */
    private void ConstructVault(Inventory gui, int page) {
        UUID currentUUID;
        int iteration = 0;
        ListIterator<String> iter = UUIDs.listIterator(page * 45);

        if (mainClassAccess.getConfig().getBoolean("UseHeadsInGUI")) //Fills up the vault page with skull entries containing xrayer data
        {
            ItemStack head;
            while (iter.hasNext() && !(iteration >= 45)) {
                currentUUID = UUID.fromString(iter.next());
                head = xrayerSkulls.get(iteration);
                SkullMeta meta = (SkullMeta) head.getItemMeta();
                meta.displayName(PlainTextComponentSerializer.plainText().deserialize(Objects.requireNonNull(Bukkit.getServer().getOfflinePlayer(currentUUID).getName())));//Head name editing
                Date lastSeenDate = new Date(Bukkit.getServer().getOfflinePlayer(currentUUID).getLastSeen()); //Getting the last played date as Date object, and then formatting it...
                DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                List<String> loreStrings = PlaceholderManager.SubstituteXrayerDataAndColorCodePlaceholders(LocaleManager.get().getStringList("EntryDesc"), String.valueOf(handledAmounts.get(iteration)), firstHandledTimes.get(iteration), df.format(lastSeenDate));
                List<Component> loreComponents = loreStrings.stream().map(LEGACY_SERIALIZER::deserialize) // assuming LEGACY_SERIALIZER is a LegacyComponentSerializer instance
                        .collect(Collectors.toList());
                meta.lore(loreComponents);
                head.setItemMeta(meta);
                gui.setItem(iteration, head);
                iteration++;
            }
        } else {
            ItemStack stone = new ItemStack(Material.STONE);
            while (iter.hasNext() && !(iteration >= 45)) //Fills up the vault page with stone entries containing xrayer data
            {
                currentUUID = UUID.fromString(iter.next());
                ItemMeta meta = stone.getItemMeta();
                meta.displayName(PlainTextComponentSerializer.plainText().deserialize(Objects.requireNonNull(Bukkit.getServer().getOfflinePlayer(currentUUID).getName()))); //Stone name editing
                Date lastSeenDate = new Date(Bukkit.getServer().getOfflinePlayer(currentUUID).getLastSeen()); //Getting the last played date as Date object, and then formatting it...
                DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                List<String> loreStrings1 = PlaceholderManager.SubstituteXrayerDataAndColorCodePlaceholders(LocaleManager.get().getStringList("EntryDesc"), String.valueOf(handledAmounts.get(iteration)), firstHandledTimes.get(iteration), df.format(lastSeenDate));
                List<Component> loreComponents1 = loreStrings1.stream().map(LEGACY_SERIALIZER::deserialize).collect(Collectors.toList());
                meta.lore(loreComponents1);
                stone.setItemMeta(meta);
                gui.setItem(iteration, stone);
                iteration++;
            }
        }

        //Lower section separators:
        gui.setItem(46, separator);
        gui.setItem(47, separator);
        gui.setItem(49, separator);
        gui.setItem(51, separator);
        gui.setItem(52, separator);

        //Lower section vault/gui stuff:
        if (page + 1 < pages) gui.setItem(53, nextButton);
        else gui.setItem(53, separator);
        if (page - 1 > -1) gui.setItem(45, prevButton);
        else gui.setItem(45, separator);
        gui.setItem(48, purgeButton);
        gui.setItem(50, refreshButton);
    }

    /**
     * Updates the xrayer head cache
     * This method is meant to be executed asynchronously
     * In principle, this method will work better if executed only once on first vault open, due to it being prone to errors
     * if heads are fetched for the same player in low periods of time
     */
    private void UpdateXrayerHeadCache(final CallbackUpdateXrayerHeadCache callback) {
        UUID currentUUID;
        //Fills up the xrayerSkulls list with xrayer skulls
        for (String uuid : UUIDs) {
            currentUUID = UUID.fromString(uuid);
            xrayerSkulls.add(HeadManager.GetPlayerHead(currentUUID, null));
        }

        //Callback to main thread runs synchronous instructions at the end of the asynchronous instructions
        Bukkit.getScheduler().runTask(mainClassAccess, callback::onFetchUpdateDone);
    }

    /**
     * Adds a single corresponding xrayer head to the xrayerSkulls cache list by xrayerName if not already listed
     */
    public void AddXrayerHeadToCache(UUID xrayerUUID) {
        Bukkit.getScheduler().runTaskAsynchronously(mainClassAccess, () -> HeadManager.GetPlayerHead(xrayerUUID, xrayerSkull -> {
            if (!xrayerSkulls.contains(xrayerSkull)) {
                xrayerSkulls.add(xrayerSkull);
            }
        }));
    }

    /**
     * Adds a dummy xrayer head to the xrayerSkulls cache. The dummy head will be Notch's head
     */
    public void AddDummyXrayerHeadToCache() {
        Bukkit.getScheduler().runTaskAsynchronously(mainClassAccess, () -> //No repeated head check since all dummy heads are generated with Notch's UUID, hence the head will repeat if this method is called more than once
                HeadManager.GetPlayerHead(UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5"), xrayerSkulls::add));
    }

    /**
     * Opens an xrayer's confiscated inventory information and actions by xrayer UUID ArrayList index
     *
     * @param player          player for which the xrayer's vault (xrayer's confiscated inventory, information and actions) GUI window will open
     * @param xrayerUUIDIndex the xrayer's index in the UUID ArrayList. This is actually the same value as the xrayer head position in the main GUI with the xrayer heads
     */
    public void OpenXrayerConfiscatedInventory(@NotNull Player player, int xrayerUUIDIndex) {
        viewers.get(player.getName()).xrayerInvUUID = UUIDs.get(xrayerUUIDIndex); //Update uuid of the xrayer we're watching

        Bukkit.getScheduler().runTaskAsynchronously(mainClassAccess, () -> mainClassAccess.mm.GetXrayerBelongings(UUIDs.get(xrayerUUIDIndex), belongings -> {
            Inventory inv = Bukkit.createInventory(null, 54, PlainTextComponentSerializer.plainText().deserialize(GUITitle)); //Vault contents to display

            if (belongings != null) //Belongings could be null when extracted from database, since StoreCopy option exists and can be false
            {
                //Fill up vault:
                for (int i = 0; i < 41; i++) //Fills up the vault page with confiscated xrayer's inventory
                {
                    inv.setItem(i, belongings[i]);
                }
            }

            //Separation bar for mere decoration:
            for (int i = 45; i < 54; i++) {
                inv.setItem(i, separator);
            }
            inv.setItem(46, separator);
            inv.setItem(47, separator);
            inv.setItem(48, separator);
            inv.setItem(50, separator);
            inv.setItem(52, separator);

            //Lower section vault/gui stuff:
            inv.setItem(45, backButton);
            inv.setItem(51, purgePlayerButton);
            inv.setItem(53, absolvePlayerButton);

            if (mainClassAccess.getConfig().getBoolean("UseHeadsInGUI")) {
                ItemStack head = xrayerSkulls.get(xrayerUUIDIndex);
                SkullMeta meta = (SkullMeta) head.getItemMeta();
                meta.displayName(PlainTextComponentSerializer.plainText().deserialize(Objects.requireNonNull(Bukkit.getServer().getOfflinePlayer(UUID.fromString(UUIDs.get(xrayerUUIDIndex))).getName()))); //Head name editing
                Date lastSeenDate = new Date(Bukkit.getServer().getOfflinePlayer(UUID.fromString(UUIDs.get(xrayerUUIDIndex))).getLastSeen()); //Getting the last played date as Date object, and then formatting it...
                DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                List<String> loreStrings2 = PlaceholderManager.SubstituteXrayerDataAndColorCodePlaceholders(LocaleManager.get().getStringList("EntryDescInspector"), String.valueOf(handledAmounts.get(xrayerUUIDIndex)), firstHandledTimes.get(xrayerUUIDIndex), df.format(lastSeenDate));
                List<Component> loreComponents2 = loreStrings2.stream().map(LEGACY_SERIALIZER::deserialize).collect(Collectors.toList());
                meta.lore(loreComponents2);
                head.setItemMeta(meta);
                inv.setItem(49, head);
            } else {
                ItemStack stone = new ItemStack(Material.STONE);
                ItemMeta meta = stone.getItemMeta();
                meta.displayName(PlainTextComponentSerializer.plainText().deserialize(Objects.requireNonNull(Bukkit.getServer().getOfflinePlayer(UUID.fromString(UUIDs.get(xrayerUUIDIndex))).getName()))); //Head name editing
                Date lastSeenDate = new Date(Bukkit.getServer().getOfflinePlayer(UUID.fromString(UUIDs.get(xrayerUUIDIndex))).getLastSeen()); //Getting the last played date as Date object, and then formatting it...
                DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                List<String> loreStrings2 = PlaceholderManager.SubstituteXrayerDataAndColorCodePlaceholders(LocaleManager.get().getStringList("EntryDescInspector"), String.valueOf(handledAmounts.get(xrayerUUIDIndex)), firstHandledTimes.get(xrayerUUIDIndex), df.format(lastSeenDate));
                List<Component> loreComponents2 = loreStrings2.stream().map(LEGACY_SERIALIZER::deserialize).collect(Collectors.toList());
                meta.lore(loreComponents2);
                stone.setItemMeta(meta);
                inv.setItem(49, stone);
            }

            player.openInventory(inv);
        }));
    }

    /**
     * Teleports the player to the coordinates where an xrayer was detected for xraying
     */
    public void TeleportToDetectionCoordinates(Player player, String xrayerUUID) {
        Bukkit.getScheduler().runTaskAsynchronously(mainClassAccess, () -> mainClassAccess.mm.GetXrayerHandleLocation(xrayerUUID, handlelocation -> {
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 100, 1);
            player.teleport(handlelocation);
            Component prefix = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(LocaleManager.get().getString("MessagesPrefix")));
            Component teleportMsg = LEGACY_SERIALIZER.deserialize(Objects.requireNonNull(LocaleManager.get().getString("TeleportToHandleLocation")));
            Component message = Component.text().append(prefix).append(Component.space()).append(teleportMsg).build();
            player.sendMessage(message);
        }));
    }

    /**
     * Returns page the viewing player is on
     */
    public int GetPage(String player) {
        return viewers.get(player).page;
    }

    /**
     * Returns the uuid of the original owner of the inventory player is inspecting (if any)
     */
    public String GetInspectedXrayer(String playerName) {
        return viewers.get(playerName).xrayerInvUUID;
    }

    /**
     * Removes all xrayer data in xrayer vault (the 4 lists) by uuid
     */
    private void RemoveXrayerDataByUUIDFromList(String xrayerUUID) {
        //Check if uuid exists:
        for (int i = 0; i < UUIDs.size(); i++)
            if (UUIDs.get(i).equals(xrayerUUID)) {
                //All xrayer data is in the same index, so we can use this index to delete all xrayer data in all parallel array lists
                UUIDs.remove(i);
                handledAmounts.remove(i);
                firstHandledTimes.remove(i);

                if (mainClassAccess.getConfig().getBoolean("UseHeadsInGUI")) xrayerSkulls.remove(i);
                break;
            }
    }

    /**
     * Removes a player and it's data from the viewers hashmap
     */
    public void RemovePlayerAsViewer(String playerName) {
        viewers.remove(playerName);
    }

    /**
     * Returns true if the viewers HashMap is empty, else false
     */
    public boolean CheckIfNoViewers() {
        return viewers.isEmpty();
    }

    /**
     * Forcefully sets how many pages the vault has
     */
    private void SetPages() {
        pages = 1;
    }
}