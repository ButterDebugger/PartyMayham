package com.butterycode.partymayhem.utils;

import dev.debutter.cuberry.paper.utils.Caboodle;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GameMakerUtils {

    public static boolean isEntityInsideRegion(Entity entity, Location loc1, Location loc2) {
        BoundingBox bounds = entity.getBoundingBox();

        double minX = Math.min(loc1.getX(), loc2.getX());
        double maxX = Math.max(loc1.getX(), loc2.getX());
        double minY = Math.min(loc1.getY(), loc2.getY());
        double maxY = Math.max(loc1.getY(), loc2.getY());
        double minZ = Math.min(loc1.getZ(), loc2.getZ());
        double maxZ = Math.max(loc1.getZ(), loc2.getZ());

        return minX <= bounds.getMinX() && maxX >= bounds.getMaxX() &&
                minY <= bounds.getMinY() && maxY >= bounds.getMaxY() &&
                minZ <= bounds.getMinZ() && maxZ >= bounds.getMaxZ();
    }

    /** @return A list of the highest impassable blocks within the given region */
    public static ArrayList<Block> getHighestBlocksInRegion(World world, Vector vec1, Vector vec2) {
        Vector lowPoint = Caboodle.getLowestPoint(vec1, vec2);
        Vector highPoint = Caboodle.getHighestPoint(vec1, vec2);
        ArrayList<Block> blocks = new ArrayList<>();

        for (int x = lowPoint.getBlockX(); x < highPoint.getBlockX(); x++) {
            for (int z = lowPoint.getBlockZ(); z < highPoint.getBlockZ(); z++) {
                for (int y = highPoint.getBlockY(); y >= lowPoint.getBlockY(); y--) {
                    Block block = world.getBlockAt(x, y, z);

                    if (block.isPassable()) continue;

                    blocks.add(block);
                    break;
                }
            }
        }

        return blocks;
    }

    public static void lookAtLocation(Player player, Location location) {
        Location playerLocation = player.getLocation();
        double distanceX = location.getX() - playerLocation.getX();
        double distanceY = location.getY() - playerLocation.getY();
        double distanceZ = location.getZ() - playerLocation.getZ();
        double distanceXZ = Math.sqrt(distanceX * distanceX + distanceZ * distanceZ);
        double pitch = Math.atan(distanceY / distanceXZ);
        double yaw = Math.atan2(-distanceX, distanceZ);
        pitch = Math.toDegrees(pitch);
        yaw = Math.toDegrees(yaw);
        player.setRotation((float) yaw, (float) pitch);
    }

    public static void line(Player player, Location loc1, Location loc2, Particle particle, double space) {
        Iterator<Vector> points = Caboodle.line(loc1.toVector(), loc2.toVector(), space);

        while (points.hasNext()) {
            Vector point = points.next();
            player.spawnParticle(particle, point.getX(), point.getY(), point.getZ(), 0);
        }
    }

    public static void outline(Player player, Location loc1, Location loc2, Particle particle, double space) {
        List<Location> points = new ArrayList<>();

        for (int dumX = 0; dumX < 2; dumX++) {
            double x = dumX == 0 ? loc1.getX() : loc2.getX();

            for (int dumY = 0; dumY < 2; dumY++) {
                double y = dumY == 0 ? loc1.getY() : loc2.getY();

                for (int dumZ = 0; dumZ < 2; dumZ++) {
                    double z = dumZ == 0 ? loc1.getZ() : loc2.getZ();

                    points.add(new Location(loc1.getWorld(), x, y, z));
                }
            }
        }

        line(player, points.get(0), points.get(1), particle, space); // front bottom
        line(player, points.get(1), points.get(3), particle, space); // front right
        line(player, points.get(3), points.get(2), particle, space); // front top
        line(player, points.get(2), points.get(0), particle, space); // front left
        line(player, points.get(4), points.get(5), particle, space); // back bottom
        line(player, points.get(5), points.get(7), particle, space); // back right
        line(player, points.get(7), points.get(6), particle, space); // back top
        line(player, points.get(6), points.get(4), particle, space); // back left
        line(player, points.get(0), points.get(4), particle, space); // bottom left
        line(player, points.get(1), points.get(5), particle, space); // bottom right
        line(player, points.get(2), points.get(6), particle, space); // top left
        line(player, points.get(3), points.get(7), particle, space); // top right
    }

    public static void outline(Player player, Block block, Particle particle, double space) {
        BoundingBox bounds = block.getBoundingBox();
        Location loc1 = new Location(block.getWorld(), bounds.getMinX(), bounds.getMinY(), bounds.getMinZ());
        Location loc2 = new Location(block.getWorld(), bounds.getMaxX(), bounds.getMaxY(), bounds.getMaxZ());

        outline(player, loc1, loc2, particle, space);
    }

}
