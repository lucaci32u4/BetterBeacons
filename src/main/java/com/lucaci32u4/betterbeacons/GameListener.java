package com.lucaci32u4.betterbeacons;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;


public class GameListener implements Listener {
    private final BetterBeacons master;

    public GameListener(BetterBeacons master) {
        this.master = master;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSpawnEvent(CreatureSpawnEvent spawnEvent) {
        master.onSpawnEvent(spawnEvent);
    }


    @EventHandler(priority = EventPriority.HIGH)
    public void onBreakEvent(BlockBreakEvent breakEvent) {
        master.onBreakEvent(breakEvent);
    }
}
