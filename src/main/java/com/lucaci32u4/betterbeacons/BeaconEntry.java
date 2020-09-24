package com.lucaci32u4.betterbeacons;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class BeaconEntry {
    private final int x, y, z;
    private int depth, range;
    private final UUID world;

    public BeaconEntry(int x, int y, int z, int depth, int range, UUID world) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.depth = depth;
        this.range = range;
        this.world = world;
    }

    public BeaconEntry(JSONObject json) {
        try {
            x = json.getInt("x");
            y = json.getInt("y");
            z = json.getInt("z");
            range = json.getInt("range");
            depth = json.getInt("depth");
            world = UUID.fromString(json.getString("world"));
        } catch (JSONException exception) {
            throw new DeserializationException("Error parsing json saved data");
        }
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public int getDepth() {
        return depth;
    }

    public int getRange() {
        return range;
    }

    public UUID getWorld() {
        return world;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public void setRange(int range) {
        this.range = range;
    }

    boolean checkPosition(int x, int y, int z, UUID world) {
        return this.world.equals(world) && this.x == x && this.y == y && this.z == z;
    }

    boolean checkInside(int x, int y, int z, UUID world) {
        return this.world.equals(world)
                && this.y - depth <= y
                && this.x - range <= x && x <= this.x + range
                && this.z - range <= z && z <= this.z + range;
    }

    public JSONObject serialize() {
        JSONObject serial = new JSONObject();
        serial.put("x", x);
        serial.put("y", y);
        serial.put("z", z);
        serial.put("range", range);
        serial.put("depth", depth);
        serial.put("world", world.toString());
        return serial;
    }

    public static class DeserializationException extends RuntimeException {
        public DeserializationException(String message) {
            super(message);
        }
    }
}
