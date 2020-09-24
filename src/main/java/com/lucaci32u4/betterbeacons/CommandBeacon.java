package com.lucaci32u4.betterbeacons;

import com.lucaci32u4.command.SimpleCommandExecutor;
import com.lucaci32u4.command.SubcommandHandler;
import com.lucaci32u4.command.parser.IntegerParser;
import com.lucaci32u4.command.reader.ParameterMap;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

import java.text.FieldPosition;
import java.text.Format;
import java.text.MessageFormat;
import java.text.ParsePosition;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class CommandBeacon {
    private final BetterBeacons plugin;

    public CommandBeacon(BetterBeacons plugin) {
        this.plugin = plugin;
        SimpleCommandExecutor.build().name("beacon")
                .subcommand()
                .name("info")
                .endSubcommand().subcommand()
                .name("add").explicitParameters(true)
                .parameter("-range", new IntegerParser(40, () -> IntStream.rangeClosed(1, 12).map(i ->i *10)))
                .parameter("-depth", new IntegerParser(20, () -> IntStream.rangeClosed(1, 6).map(i ->i *10)))
                .endSubcommand().subcommand()
                .name("remove")
                .endSubcommand().subcommand()
                .name("save")
                .endSubcommand().endCommand()
                .setHandler(this, CommandBeacon.class)
                .selfInstall(plugin, true);
    }

    private Beacon getTargetBeaconBlockLocation(CommandSender sender) throws CannotFindBeaconTargetException {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Block target = player.getTargetBlock(null, 15);
            if (target.getState() instanceof Beacon) {
                return (Beacon) (target.getState());
            } else throw new CannotFindBeaconTargetException("Not looking at a beacon, but at " + target.getType().name());
        } else throw new CannotFindBeaconTargetException("Must be a player to use this function");
    }

    String formatBeaconEffect(PotionEffect effect) {
        if (effect != null) {
            String str = effect.getType().getName().replace("_", " ").toLowerCase();
            str = str.substring(0, 1).toUpperCase() + str.substring(1);
            if (effect.getAmplifier() == 1) str = str + " II";
            return ChatColor.GREEN + str + ChatColor.RESET;
        } else return ChatColor.YELLOW + "None" + ChatColor.RESET;
    }

    @SubcommandHandler("add")
    public void onAdd(CommandSender sender, ParameterMap param) {
        Integer range = param.get("-range", Integer.class);
        Integer depth = param.get("-depth", Integer.class);
        try {
            Location beacon = getTargetBeaconBlockLocation(sender).getLocation();
            if (range < 1) throw new InvalidParameterException("Range must be greater than 1");
            if (depth < 0) throw new InvalidParameterException("Depth must be positive");
            plugin.registerBeacon(beacon, range, depth);
        } catch (RuntimeException cannotFindBeaconTargetException) {
            sender.sendMessage(cannotFindBeaconTargetException.getMessage());
        }
    }


    @SubcommandHandler("remove")
    public void onRemove(CommandSender sender, ParameterMap param) {
        try {
            Location beacon = getTargetBeaconBlockLocation(sender).getLocation();
            plugin.removeBeacon(beacon);
        } catch (RuntimeException cannotFindBeaconTargetException) {
            sender.sendMessage(cannotFindBeaconTargetException.getMessage());
        }
    }


    @SubcommandHandler("info")
    public void onInfo(CommandSender sender, ParameterMap param) {
        try {
            Beacon beacon = getTargetBeaconBlockLocation(sender);
            BeaconEntry props = plugin.queryBeacon(beacon.getLocation());
            sender.sendMessage(displayInfo(beacon, props));
        } catch (RuntimeException cannotFindBeaconTargetException) {
            sender.sendMessage(cannotFindBeaconTargetException.getMessage());
        }
    }

    public String displayInfo(Beacon beacon, BeaconEntry entry) {
        TextOutputFormatter formatter = new TextOutputFormatter();
        formatter.append("X: {0} Y: {1} Z:{2}\n", ChatColor.GREEN, beacon.getX(), beacon.getY(), beacon.getZ());
        formatter.append("Tier: {0}\n", ChatColor.GREEN, beacon.getTier());
        formatter.append("Primary Effect: {0}\n", ChatColor.GREEN, formatBeaconEffect(beacon.getPrimaryEffect()));
        if (beacon.getPrimaryEffect() != null && beacon.getPrimaryEffect().getAmplifier() == 1) {
            formatter.append("Secondary Effect: {0}\n", ChatColor.YELLOW, "Upgrade");
        } else {
            formatter.append("Secondary Effect: {0}\n", ChatColor.GREEN, formatBeaconEffect(beacon.getSecondaryEffect()));
        }
        if (entry != null) {
            formatter.append("Beacon registered with a range of {0} and depth of {1}\n", ChatColor.GREEN, entry.getRange(), entry.getDepth());
        } else {
            formatter.append("Beacon not registered\n", ChatColor.GREEN);
        }
        return formatter.toString();
    }

    @SubcommandHandler("save")
    public void onSave(CommandSender sender, ParameterMap param) {
        plugin.saveData();
        sender.sendMessage("Beacon data saved");
    }

    private static class CannotFindBeaconTargetException extends RuntimeException {
        public CannotFindBeaconTargetException(String message) {
            super(message);
        }
    }

    private static class InvalidParameterException extends RuntimeException {
        public InvalidParameterException(String message) {
            super(message);
        }
    }
}

class TextOutputFormatter {
    private StringBuffer text = new StringBuffer();

    public void append(String text, ChatColor color, Object ... objs) {
        MessageFormat mf = new MessageFormat(text);
        for (int i = 0; i < objs.length; i++) {
            objs[i] = color + objs[i].toString() + ChatColor.RESET;
        }
        mf.format(objs, this.text, null);
    }

    @Override
    public String toString() {
        return text.toString();
    }
}