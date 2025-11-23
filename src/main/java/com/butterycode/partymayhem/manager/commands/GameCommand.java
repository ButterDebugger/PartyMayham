package com.butterycode.partymayhem.manager.commands;

import com.butterycode.partymayhem.games.MinigameFactory;
import com.butterycode.partymayhem.manager.EditorManager;
import com.butterycode.partymayhem.manager.GameManager;
import com.butterycode.partymayhem.manager.Transition;
import dev.debutter.cuberry.paper.utils.AwesomeText;
import dev.debutter.cuberry.paper.utils.Caboodle;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.butterycode.partymayhem.manager.menu.AdminMenu.beginningDialog;

public class GameCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(AwesomeText.colorizeHex("&cError: &7You must be a player to use this command."));
            return true;
        }

        if (!player.hasPermission("partymayhem.admin")) {
            player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT_CLOSED, 2f, 1f);
            player.sendMessage(AwesomeText.colorizeHex("&cError: &7You are lacking the permission node &fpartymayhem.admin&7."));
            return true;
        }

        if (args.length == 0) {
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2f, 1f);
            sender.sendMessage(AwesomeText.colorizeHex("&3Usage: &7/game <arguments>"));
            return true;
        }

        if (args[0].equalsIgnoreCase("menu")) {
            player.showDialog(beginningDialog());

            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1f);
            player.sendMessage(AwesomeText.colorizeHex("&a&l» &7Game menu has been opened."));
            return true;
        }

        if (args[0].equalsIgnoreCase("editor")) {
            boolean isEditor = EditorManager.isEditing(player.getUniqueId());

            if (args.length < 2) {
                if (isEditor) {
                    EditorManager.revokeEditor(player.getUniqueId());

                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2f, 1f);
                    player.sendMessage(AwesomeText.colorizeHex("&a&l» &7Editor mode has been disabled."));
                    return true;
                } else {
                    EditorManager.addEditor(player.getUniqueId());

                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2f, 1f);
                    player.sendMessage(AwesomeText.colorizeHex("&a&l» &7Editor mode has been enabled."));

                    giveEditorWand(player);
                    return true;
                }
            }

            if (args[1].equalsIgnoreCase("enable") || args[1].equalsIgnoreCase("on")) {
                if (isEditor) {
                    player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT_CLOSED, 2f, 1f);
                    player.sendMessage(AwesomeText.colorizeHex("&cError: &7Editor mode is already enabled."));

                    giveEditorWand(player);
                    return true;
                }

                EditorManager.addEditor(player.getUniqueId());

                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2f, 1f);
                player.sendMessage(AwesomeText.colorizeHex("&a&l» &7Editor mode has been enabled."));

                giveEditorWand(player);
                return true;
            } else if (args[1].equalsIgnoreCase("disable") || args[1].equalsIgnoreCase("off")) {
                if (!isEditor) {
                    player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT_CLOSED, 2f, 1f);
                    player.sendMessage(AwesomeText.colorizeHex("&cError: &7Editor mode is already disabled."));
                    return true;
                }

                EditorManager.revokeEditor(player.getUniqueId());

                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2f, 1f);
                player.sendMessage(AwesomeText.colorizeHex("&a&l» &7Editor mode has been disabled."));
                return true;
            } else {
                player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT_CLOSED, 2f, 1f);
                player.sendMessage(AwesomeText.colorizeHex("&cError: &7Invalid arguments."));
                return true;
            }
        } else if (args[0].equalsIgnoreCase("transition")) {
            if (args.length < 2) {
                player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT_CLOSED, 2f, 1f);
                player.sendMessage(AwesomeText.colorizeHex("&cError: &7Not enough arguments."));
                return true;
            }

            Transition transition = Transition.getByLabel(args[1]);

            if (transition == null) {
                player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT_CLOSED, 2f, 1f);
                player.sendMessage(AwesomeText.colorizeHex("&cError: &7Invalid transition mode."));
                return true;
            }

            GameManager.setTransition(transition);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2f, 1f);
            player.sendMessage(AwesomeText.colorizeHex("&a&l» &7Transition mode has been set to &f" + args[1] + "&7."));
            return true;
        } else if (args[0].equalsIgnoreCase("enable")) {
            if (args.length < 2) {
                player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT_CLOSED, 2f, 1f);
                player.sendMessage(AwesomeText.colorizeHex("&cError: &7Not enough arguments."));
                return true;
            }

            MinigameFactory minigame = GameManager.getMinigameById(args[1]);

            if (minigame == null) {
                player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT_CLOSED, 2f, 1f);
                player.sendMessage(AwesomeText.colorizeHex("&cError: &7Invalid game id."));
                return true;
            }

            if (minigame.isEnabled()) {
                player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT_CLOSED, 2f, 1f);
                player.sendMessage(AwesomeText.colorizeHex("&cError: &f" + args[1] + "&7 is already enabled."));
                return true;
            }

            boolean result = GameManager.enableMinigame(minigame);

            if (result) {
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2f, 1f);
                player.sendMessage(AwesomeText.colorizeHex("&a&l» &f" + args[1] + "&7 has been enabled."));
            } else {
                player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT_CLOSED, 2f, 1f);
                player.sendMessage(AwesomeText.colorizeHex("&cError: &7Could not enable &f" + args[1] + "&7."));
            }
            return true;
        } else if (args[0].equalsIgnoreCase("disable")) {
            if (args.length < 2) {
                player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT_CLOSED, 2f, 1f);
                player.sendMessage(AwesomeText.colorizeHex("&cError: &7Not enough arguments."));
                return true;
            }

            MinigameFactory minigame = GameManager.getMinigameById(args[1]);

            if (minigame == null) {
                player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT_CLOSED, 2f, 1f);
                player.sendMessage(AwesomeText.colorizeHex("&cError: &7Invalid game id."));
                return true;
            }

            if (!minigame.isEnabled()) {
                player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT_CLOSED, 2f, 1f);
                player.sendMessage(AwesomeText.colorizeHex("&cError: &f" + args[1] + "&7 is already disabled."));
                return true;
            }

            boolean result = GameManager.disableMinigame(minigame);

            if (result) {
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2f, 1f);
                player.sendMessage(AwesomeText.colorizeHex("&a&l» &f" + args[1] + "&7 has been disabled."));
            } else {
                player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT_CLOSED, 2f, 1f);
                player.sendMessage(AwesomeText.colorizeHex("&cError: &7Could not disable &f" + args[1] + "&7."));
            }
            return true;
        } else if (args[0].equalsIgnoreCase("info")) {
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2f, 1f);
            player.sendMessage(AwesomeText.colorizeHex("&a&l» &7Information:"));

            // Send games list
            List<String> enabledGames = GameManager.getEnabledMinigames().stream().map(MinigameFactory::getId).toList();
            List<String> disabledGames = GameManager.getDisabledMinigames().stream().map(MinigameFactory::getId).toList();

            if (enabledGames.isEmpty() && disabledGames.isEmpty()) {
                player.sendMessage(AwesomeText.colorizeHex("&7There are not any games available."));
            } else {
                int gamesAmount = enabledGames.size() + disabledGames.size();

                if (gamesAmount == 1) {
                    player.sendMessage(AwesomeText.colorizeHex("&7There is &f" + gamesAmount + " &7game available:"));
                } else {
                    player.sendMessage(AwesomeText.colorizeHex("&7There are &f" + gamesAmount + " &7games available:"));
                }

                for (String name : enabledGames) {
                    player.sendMessage(AwesomeText.colorizeHex("&a☑ &f" + name));
                }
                for (String name : disabledGames) {
                    player.sendMessage(AwesomeText.colorizeHex("&c☒ &f" + name));
                }
            }

            // Send game state
            String coloredState = switch (GameManager.getGameState()) {
                case STARTED -> "&a" + GameManager.getGameState();
                case STOPPED -> "&c" + GameManager.getGameState();
                case WAITING -> "&6" + GameManager.getGameState();
                case INTERMISSION -> "&e" + GameManager.getGameState();
            };

            player.sendMessage(AwesomeText.colorizeHex("&7Game State: " + coloredState));

            // Send lobby status
            if (GameManager.isLobbyActive()) {
                player.sendMessage(AwesomeText.colorizeHex("&7Lobby Status: &aACTIVE"));
            } else if (GameManager.getLobby().isSetup()) {
                player.sendMessage(AwesomeText.colorizeHex("&7Lobby Status: &6SETUP"));
            } else {
                player.sendMessage(AwesomeText.colorizeHex("&7Lobby Status: &cNOT SETUP"));
            }
            return true;
        } else if (args[0].equalsIgnoreCase("start")) {
            boolean result = GameManager.startMinigames();

            if (result) {
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2f, 1f);
                player.sendMessage(AwesomeText.colorizeHex("&a&l» &7Minigames have been started."));
            } else {
                player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT_CLOSED, 2f, 1f);
                player.sendMessage(AwesomeText.colorizeHex("&cError: &7Could not start minigames."));
            }
            return true;
        } else if (args[0].equalsIgnoreCase("stop")) {
            boolean result = GameManager.stopMinigames();

            if (result) {
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2f, 1f);
                player.sendMessage(AwesomeText.colorizeHex("&a&l» &7Minigames have been stopped."));
            } else {
                player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT_CLOSED, 2f, 1f);
                player.sendMessage(AwesomeText.colorizeHex("&cError: &7Could not stop minigames."));
            }
            return true;
        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT_CLOSED, 2f, 1f);
            player.sendMessage(AwesomeText.colorizeHex("&cError: &7Invalid arguments."));
            return true;
        }
    }

    private void giveEditorWand(Player player) {
        player.getInventory().remove(Material.GOLDEN_AXE);
        ItemStack oldItem = player.getInventory().getItemInMainHand();
        player.getInventory().setItemInMainHand(new ItemStack(Material.GOLDEN_AXE));
        Caboodle.giveItem(player, oldItem);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return null; // Cancel if sender is not a player

        if (!player.hasPermission("partymayhem.admin")) return null; // Cancel if player doesn't have permission

        if (args.length == 1) {
            return new ArrayList<>(Arrays.asList("menu", "editor", "transition", "enable", "disable", "info", "start", "stop"));
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("editor")) {
            return new ArrayList<>(Arrays.asList("enable", "disable"));
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("transition")) {
            return Arrays.stream(Transition.values()).map(Transition::getLabel).collect(Collectors.toList());
        }
        if (args[0].equalsIgnoreCase("enable")) {
            return GameManager.getDisabledMinigames().stream().map(MinigameFactory::getId).collect(Collectors.toList());
        }
        if (args[0].equalsIgnoreCase("disable")) {
            return GameManager.getEnabledMinigames().stream().map(MinigameFactory::getId).collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
