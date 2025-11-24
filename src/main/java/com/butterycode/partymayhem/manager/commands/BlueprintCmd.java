package com.butterycode.partymayhem.manager.commands;

import com.butterycode.partymayhem.games.MinigameFactory;
import com.butterycode.partymayhem.manager.EditorManager;
import com.butterycode.partymayhem.manager.GameManager;
import com.butterycode.partymayhem.settings.blueprint.Anchor;
import com.butterycode.partymayhem.settings.blueprint.Blueprint;
import com.butterycode.partymayhem.settings.blueprint.Region;
import dev.debutter.cuberry.paper.utils.AwesomeText;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BlueprintCmd implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendRichMessage("<red>Error:</red> <gray>You must be a player to use this command.</gray>");
            return true;
        }

        if (!player.hasPermission("partymayhem.admin")) {
            player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT_CLOSED, 2f, 1f);
            player.sendRichMessage("<red>Error:</red> <gray>You are lacking the permission node <white>partymayhem.admin</white>.</gray>");
            return true;
        }

        if (args.length == 0) {
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2f, 1f);
            sender.sendRichMessage("<dark_aqua>Usage:</dark_aqua> <gray>/game <minigame_id> <blueprint_id> <action></gray>");
            return true;
        }

        if (args.length < 3) {
            player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT_CLOSED, 2f, 1f);
            player.sendRichMessage("<red>Error:</red> <gray>Not enough arguments.</gray>");
            return true;
        }

        MinigameFactory minigame;
        if (args[0].equals("lobby")) {
            minigame = GameManager.getLobby();
        } else {
            minigame = GameManager.getMinigameById(args[0]);
        }

        if (minigame == null) {
            player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT_CLOSED, 2f, 1f);
            player.sendMessage(AwesomeText.colorizeHex("&cError: &7Invalid game id."));
            return true;
        }

        Blueprint blueprint = minigame.getBlueprintById(args[1]);

        if (blueprint instanceof Region region) {
            if (args[2].equalsIgnoreCase("set")) {
                // Cancel if not in editor mode
                if (!EditorManager.isEditing(player.getUniqueId())) {
                    player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT_CLOSED, 2f, 1f);
                    player.sendRichMessage("<red>Error:</red> <gray>You must be in editor mode to use this.</gray>");
                    return true;
                }

                // Cancel if the player does not have a selection
                if (!EditorManager.hasSelection(player.getUniqueId())) {
                    player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT_CLOSED, 2f, 1f);
                    player.sendMessage(AwesomeText.colorizeHex("&cError: &7You currently do not have anything selected."));
                    return true;
                }

                // Update the blueprint
                Location firstPos = EditorManager.getFirstPos(player.getUniqueId());
                Location secondPos = EditorManager.getSecondPos(player.getUniqueId());

                region.setWorld(firstPos.getWorld());
                region.setFirstPoint(firstPos.toVector());
                region.setSecondPoint(secondPos.toVector());

                // Save the blueprint
                if (!region.save()) {
                    player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT_CLOSED, 2f, 1f);
                    player.sendMessage(AwesomeText.colorizeHex("&cError: &7Something went wrong while saving."));
                    return true;
                }

                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2f, 1f);
                player.sendMessage(AwesomeText.colorizeHex("&a&l» &7Region blueprint &f" + blueprint.getId() + "&7 has been set."));
                return true;
            } else if (args[2].equalsIgnoreCase("select")) {
                if (!region.status()) {
                    player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT_CLOSED, 2f, 1f);
                    player.sendMessage(AwesomeText.colorizeHex("&cError: &7Region blueprint &f" + blueprint.getId() + "&7 has not been set."));
                    return true;
                }

                // Cancel if not in editor mode
                if (!EditorManager.isEditing(player.getUniqueId())) {
                    player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT_CLOSED, 2f, 1f);
                    player.sendRichMessage("<red>Error:</red> <gray>You must be in editor mode to use this.</gray>");
                    return true;
                }

                EditorManager.setFirstPos(player.getUniqueId(), region.getFirstLocation());
                EditorManager.setSecondPos(player.getUniqueId(), region.getSecondLocation());

                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2f, 1f);
                player.sendMessage(AwesomeText.colorizeHex("&a&l» &7Region blueprint &f" + blueprint.getId() + "&7 has been selected."));
                return true;
            } else if (args[2].equalsIgnoreCase("reset")) {
                if (!region.delete()) {
                    player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT_CLOSED, 2f, 1f);
                    player.sendMessage(AwesomeText.colorizeHex("&cError: &7Something went wrong while deleting."));
                    return true;
                }

                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2f, 1f);
                player.sendMessage(AwesomeText.colorizeHex("&a&l» &7Region blueprint &f" + blueprint.getId() + "&7 has been reset."));
                return true;
            } else {
                player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT_CLOSED, 2f, 1f);
                player.sendMessage(AwesomeText.colorizeHex("&cError: &7Invalid arguments."));
                return true;
            }
        } else if (blueprint instanceof Anchor anchor) {
            if (args[2].equalsIgnoreCase("set")) {
                anchor.setLocation(player.getLocation());

                if (!anchor.save()) {
                    player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT_CLOSED, 2f, 1f);
                    player.sendMessage(AwesomeText.colorizeHex("&cError: &7Something went wrong while saving."));
                    return true;
                }

                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2f, 1f);
                player.sendMessage(AwesomeText.colorizeHex("&a&l» &7Anchor blueprint &f" + blueprint.getId() + "&7 has been set."));
                return true;
            } else if (args[2].equalsIgnoreCase("teleport")) {
                if (!anchor.status()) {
                    player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT_CLOSED, 2f, 1f);
                    player.sendMessage(AwesomeText.colorizeHex("&cError: &7Anchor blueprint &f" + blueprint.getId() + "&7 has not been set."));
                    return true;
                }
                assert anchor.getLocation() != null;

                player.teleport(anchor.getLocation());

                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2f, 1f);
                player.sendMessage(AwesomeText.colorizeHex("&a&l» &7You have been teleported to the anchor blueprint &f" + blueprint.getId() + "&7."));
                return true;
            } else if (args[2].equalsIgnoreCase("reset")) {
                if (!anchor.delete()) {
                    player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT_CLOSED, 2f, 1f);
                    player.sendMessage(AwesomeText.colorizeHex("&cError: &7Something went wrong while deleting."));
                    return true;
                }

                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2f, 1f);
                player.sendMessage(AwesomeText.colorizeHex("&a&l» &7Anchor blueprint &f" + blueprint.getId() + "&7 has been reset."));
                return true;
            } else {
                player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT_CLOSED, 2f, 1f);
                player.sendMessage(AwesomeText.colorizeHex("&cError: &7Invalid arguments."));
                return true;
            }
        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT_CLOSED, 2f, 1f);
            player.sendMessage(AwesomeText.colorizeHex("&cError: &7Invalid arguments."));
            return true;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) return Collections.emptyList(); // Cancel if sender is not a player

        if (!player.hasPermission("partymayhem.admin")) return Collections.emptyList(); // Cancel if player doesn't have permission

        if (args.length == 1) {
            ArrayList<String> editorList = new ArrayList<>();
            editorList.add("lobby");
            editorList.addAll(GameManager.getMinigameIds());
            return editorList;
        }

        if (args.length >= 2) {
            MinigameFactory minigame;
            if (args[0].equals("lobby")) {
                minigame = GameManager.getLobby();
            } else {
                minigame = GameManager.getMinigameById(args[0]);
            }

            if (minigame == null) return Collections.emptyList();

            if (args.length == 2) {
                return minigame.getBlueprintIds();
            }
            if (args.length == 3) {
                Blueprint blueprint = minigame.getBlueprintById(args[1]);

                return switch (blueprint) {
                    case Region region -> List.of("set", "select", "reset");
                    case Anchor anchor -> List.of("set", "teleport", "reset");
                    case null -> Collections.emptyList();
                };
            }
        }

        return Collections.emptyList();
    }

}
