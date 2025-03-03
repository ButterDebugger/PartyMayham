package com.butterycode.partymayhem.utils;

import com.butterycode.partymayhem.PartyMayhem;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.*;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class WorldEditor {

    public static void fillRegion(World world, Location loc1, Location loc2, Material material) {
        List<Block> blocks = getBlocksWithinRegion(world, loc1, loc2);

        for (Block block : blocks) {
            block.setType(material);
        }
    }

    public static List<Block> getBlocksWithinRegion(World world, Location loc1, Location loc2) {
        List<Block> blocks = new ArrayList<>();

        int x1 = loc1.getBlockX();
        int y1 = loc1.getBlockY();
        int z1 = loc1.getBlockZ();

        int x2 = loc2.getBlockX();
        int y2 = loc2.getBlockY();
        int z2 = loc2.getBlockZ();

        int lowestX = Math.min(x1, x2);
        int lowestY = Math.min(y1, y2);
        int lowestZ = Math.min(z1, z2);

        int highestX = Math.max(x1, x2);
        int highestY = Math.max(y1, y2);
        int highestZ = Math.max(z1, z2);

        for (int x = lowestX; x <= highestX; x++) {
            for (int y = lowestY; y <= highestY; y++) {
                for (int z = lowestZ; z <= highestZ; z++) {
                    Location loc = new Location(world, x, y, z);
                    blocks.add(loc.getBlock());
                }
            }
        }

        return blocks;
    }

    /*
     *  Schematic functions
     */

    public static Clipboard loadSchematic(String name) {
        File file = new File(PartyMayhem.getPlugin().getDataFolder() + File.separator + name.replaceAll("/", File.separator) + ".schematic");

        if (!file.exists()) { // Check if file doesn't exist
            return null;
        }

        try {
            ClipboardFormat format = ClipboardFormats.findByFile(file);
            ClipboardReader reader = format.getReader(Files.newInputStream(file.toPath()));
            return reader.read();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

		/* Pasting example:
		Clipboard clipboard = WorldEditor.loadSchematic("example");

		if (clipboard == null) {
			// Schematic had an issue while loading
		} else {
			try (EditSession editSession = WorldEdit.getInstance().newEditSession(new BukkitWorld(loc.getWorld()))) {
				Operation operation = new ClipboardHolder(clipboard)
					.createPaste(editSession)
					.to(BlockVector3.at(loc.getX(), loc.getY(), loc.getZ()))
					.ignoreAirBlocks(false)
					.build();

				Operations.complete(operation);
			} catch (WorldEditException e) {
				e.printStackTrace();
			}
		}
		*/
    }

    public static boolean saveSchematic(String name, Player player) {
        File file = new File(PartyMayhem.getPlugin().getDataFolder() + File.separator + name.replaceAll("/", File.separator) + ".schematic");

        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        WorldEditPlugin wep = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("FastAsyncWorldEdit");
        LocalSession session = wep.getSession(player);
        com.sk89q.worldedit.entity.Player p = wep.wrapPlayer(player);
        EditSession editSession = session.createEditSession(p);

        try (ClipboardWriter writer = BuiltInClipboardFormat.FAST.getWriter(Files.newOutputStream(file.toPath()))) {
            Region region = session.getSelection(p.getWorld());
            Clipboard clipboard = new BlockArrayClipboard(region);
            ForwardExtentCopy copy = new ForwardExtentCopy(editSession, region, clipboard, region.getMinimumPoint());
            Operations.complete(copy);

            writer.write(clipboard);
            return true;
        } catch (IOException | WorldEditException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean saveSchematic(String name, Location minPoint, Location maxPoint) {
        File file = new File(PartyMayhem.getPlugin().getDataFolder() + File.separator + name.replaceAll("/", File.separator) + ".schematic");

        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if (!minPoint.getWorld().equals(maxPoint.getWorld())) return false; // Cancel if locations are not in the same world
        World world = minPoint.getWorld();

        BukkitWorld w = new BukkitWorld(world);
        BlockVector3 pos1 = BlockVector3.at(minPoint.getX(), minPoint.getY(), minPoint.getZ());
        BlockVector3 pos2 = BlockVector3.at(maxPoint.getX(), maxPoint.getY(), maxPoint.getZ());
        CuboidRegion region = new CuboidRegion(w, pos1, pos2);

        try (ClipboardWriter writer = BuiltInClipboardFormat.FAST.getWriter(Files.newOutputStream(file.toPath()))) {
            BlockArrayClipboard clipboard = new BlockArrayClipboard(region);

            writer.write(clipboard);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
