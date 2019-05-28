package net.frankheijden.insights;

import org.bukkit.Material;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.HashMap;

public class Listeners implements Listener {
    private Insights plugin;

    Listeners(Insights plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        MemorySection materials = (MemorySection) plugin.config.get("general.materials");
        if (materials != null) {
            for (String materialString : materials.getKeys(false)) {
                Material material = Material.getMaterial(materialString);
                if (event.getBlock().getType() == material) {
                    int limit = plugin.config.getInt("general.materials." + materialString);
                    int current = plugin.utils.updateCachedAmountInChunk(event.getBlock().getChunk(), material, true);

                    if (player.hasPermission("insights.check.realtime") && plugin.sqLite.hasRealtimeCheckEnabled(player)) {
                        plugin.utils.sendActionBar(player, "messages.realtime_check_custom", "%count%", String.valueOf(current), "%material%", plugin.utils.capitalizeName(material.name().toLowerCase()), "%limit%", String.valueOf(limit));
                    }
                    return;
                }
            }
        }

        if (plugin.utils.isTile(event.getBlock())) {
            if (player.hasPermission("insights.check.realtime") && plugin.sqLite.hasRealtimeCheckEnabled(player)) {
                plugin.utils.sendActionBar(player, "messages.realtime_check", "%tile_count%", String.valueOf(event.getBlock().getLocation().getChunk().getTileEntities().length));
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        MemorySection materials = (MemorySection) plugin.config.get("general.materials");
        if (materials != null) {
            for (String materialString : materials.getKeys(false)) {
                Material material = Material.valueOf(materialString);
                if (event.getBlockPlaced().getType() == material) {
                    int limit = plugin.config.getInt("general.materials." + materialString);
                    int current = plugin.utils.updateCachedAmountInChunk(event.getBlockPlaced().getChunk(), material, false);
                    if (current > limit) {
                        if (!player.hasPermission("insights.bypass." + materialString)) {
                            String n = event.getBlockPlaced().getChunk().getX() + "_" + event.getBlockPlaced().getChunk().getZ();
                            HashMap<Material, Integer> ms = plugin.chunkSnapshotHashMap.get(n);
                            ms.put(material, limit);
                            plugin.chunkSnapshotHashMap.put(n, ms);
                            current = current - 1;

                            plugin.utils.sendMessage(event.getPlayer(), "messages.limit_reached_custom", "%limit%", String.valueOf(limit), "%material%", plugin.utils.capitalizeName(material.name().toLowerCase()));
                            event.setCancelled(true);
                        }
                    }

                    if (event.getPlayer().hasPermission("insights.check.realtime") && plugin.sqLite.hasRealtimeCheckEnabled(player)) {
                        plugin.utils.sendActionBar(event.getPlayer(), "messages.realtime_check_custom", "%count%", String.valueOf(current), "%material%", plugin.utils.capitalizeName(material.name().toLowerCase()), "%limit%", String.valueOf(limit));
                    }
                    return;
                }
            }
        }

        if (plugin.utils.isTile(event.getBlockPlaced())) {
            if (plugin.max > -1 && event.getBlockPlaced().getLocation().getChunk().getTileEntities().length >= plugin.max) {
                if (!player.hasPermission("insights.bypass")) {
                    event.setCancelled(true);
                    plugin.utils.sendMessage(player, "messages.limit_reached", "%limit%", String.valueOf(plugin.max));
                }
            }

            if (player.hasPermission("insights.check.realtime") && plugin.sqLite.hasRealtimeCheckEnabled(player)) {
                plugin.utils.sendActionBar(player, "messages.realtime_check", "%tile_count%", String.valueOf(event.getBlock().getLocation().getChunk().getTileEntities().length));
            }
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        plugin.chunkSnapshotHashMap.remove(event.getChunk().getX() + "_" + event.getChunk().getZ());
    }
}