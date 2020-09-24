package com.lucaci32u4.betterbeacons;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Beacon;
import org.bukkit.entity.*;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.StreamSupport;

public class BetterBeacons extends JavaPlugin {
    private final static String SAVE_FILE = "plugins/BetterBeacons/beacons.json";

    private final ArrayList<BeaconEntry> beacons = new ArrayList<>();
    private final GameListener listener = new GameListener(this);


    @Override
    public void onEnable() {
        new CommandBeacon(this);
        this.getServer().getPluginManager().registerEvents(listener, this);
        loadData();
    }

    protected void onSpawnEvent(CreatureSpawnEvent spawnEvent) {
        Entity ent = spawnEvent.getEntity();
        if (ent instanceof Monster) {
            Monster e = (Monster) ent;
            if (e instanceof Zombie
                    || e instanceof Skeleton
                    || e instanceof Creeper
                    || e instanceof Spider
                    || e instanceof Witch
                    || e instanceof Enderman) {
                if (hasBeaconInRange(e.getLocation())) {
                    e.remove();
                }
            }
        }

    }

    protected void onBreakEvent(BlockBreakEvent breakEvent) {
        if (breakEvent.getBlock().getState() instanceof Beacon) {
            Location loc = breakEvent.getBlock().getLocation();
            removeBeacon(loc);
        }
    }

    protected void removeBeacon(Location l) {
        int x = l.getBlockX();
        int y = l.getBlockY();
        int z = l.getBlockZ();
        if (l.getWorld() != null) {
            UUID worldId = l.getWorld().getUID();
            beacons.removeIf(b -> b.checkPosition(x, y, z, worldId));
        }
    }

    private boolean hasBeaconInRange(Location l) {
        int x = l.getBlockX();
        int y = l.getBlockY();
        int z = l.getBlockZ();
        if (l.getWorld() != null) {
            UUID worldId = l.getWorld().getUID();
            return beacons.stream().anyMatch(b -> b.checkInside(x, y, z, worldId));
        }
        return false;
    }

    protected void registerBeacon(Location l, int range, int depth) {
        if (l.getWorld() != null) {
            BeaconEntry existing = queryBeacon(l);
            if (existing != null) {
                existing.setRange(range);
                existing.setDepth(depth);
            } else {
                beacons.add(new BeaconEntry(l.getBlockX(), l.getBlockY(), l.getBlockZ(), depth, range, l.getWorld().getUID()));
            }
        }
    }

    protected BeaconEntry queryBeacon(Location l) {
        int x = l.getBlockX();
        int y = l.getBlockY();
        int z = l.getBlockZ();
        if (l.getWorld() != null) {
            UUID worldId = l.getWorld().getUID();
            return beacons.stream().filter(b -> b.checkPosition(x, y, z, worldId)).findAny().orElse(null);
        }
        return null;
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(listener);
        saveData();
    }


    protected void saveData() {
        JSONArray array = new JSONArray();
        beacons.stream().map(BeaconEntry::serialize).forEachOrdered(array::put);
        try {
            new File(SAVE_FILE).getParentFile().mkdirs();
            new File(SAVE_FILE).createNewFile();
            FileOutputStream out = new FileOutputStream(SAVE_FILE);
            out.write(array.toString().getBytes());
            out.close();
        } catch (IOException ioe) {
            Bukkit.broadcastMessage("Error: Cannot write beacon data into file");
            ioe.printStackTrace();
        }
    }

    protected void loadData() {
        try {
            String content = new String(Files.readAllBytes(Paths.get(SAVE_FILE)));
            JSONArray arr = new JSONArray(content);
            beacons.clear();
            StreamSupport.stream(arr.spliterator(), false)
                    .map(o -> (JSONObject)o)
                    .map(BeaconEntry::new)
                    .filter(e -> this.getServer().getWorld(e.getWorld()) != null)
                    .forEachOrdered(beacons::add);
        } catch (Exception unused) {
            Bukkit.broadcastMessage("Warning: Cannot read beacon data from file");
        }
    }

}
