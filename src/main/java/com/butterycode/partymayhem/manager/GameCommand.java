package com.butterycode.partymayhem.manager;

import com.butterycode.partymayhem.games.MinigameFactory;
import com.butterycode.partymayhem.manager.blueprint.Anchor;
import com.butterycode.partymayhem.manager.blueprint.Blueprint;
import com.butterycode.partymayhem.manager.blueprint.Region;
import com.butterycode.partymayhem.utils.GameMakerUtils;
import com.butterycode.partymayhem.utils.menus.GuiButtonUtils;
import com.butterycode.partymayhem.utils.menus.GuiMenu;
import dev.debutter.cuberry.paper.utils.AwesomeText;
import dev.debutter.cuberry.paper.utils.Caboodle;
import dev.debutter.cuberry.paper.utils.DogTags;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
            openGameMenu(player);

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
        } else if (args[0].equalsIgnoreCase("blueprint")) {
            if (!EditorManager.isEditing(player.getUniqueId())) {
                player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT_CLOSED, 2f, 1f);
                player.sendMessage(AwesomeText.colorizeHex("&cError: &7You must be in editor mode to use this."));
                return true;
            }
            if (args.length < 4) {
                player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT_CLOSED, 2f, 1f);
                player.sendMessage(AwesomeText.colorizeHex("&cError: &7Not enough arguments."));
                return true;
            }

            MinigameFactory minigame;
            if (args[1].equals("lobby")) {
                minigame = GameManager.getLobby();
            } else {
                minigame = GameManager.getMinigameById(args[1]);
            }

            if (minigame == null) {
                player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT_CLOSED, 2f, 1f);
                player.sendMessage(AwesomeText.colorizeHex("&cError: &7Invalid game id."));
                return true;
            }

            Blueprint blueprint = minigame.getBlueprintByName(args[2]);

            int index = 0;

            if (args.length > 4) {
                if (!DogTags.isNumeric(args[4])) {
                    player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT_CLOSED, 2f, 1f);
                    player.sendMessage(AwesomeText.colorizeHex("&cError: &7You must enter a valid number."));
                    return true;
                }

                index = Integer.parseInt(args[4]) - 1;

                if (index < 0 || index >= blueprint.getMaxAmount()) {
                    player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT_CLOSED, 2f, 1f);
                    player.sendMessage(AwesomeText.colorizeHex("&cError: &7Invalid blueprint index."));
                    return true;
                }
            }

            if (blueprint instanceof Region region) {
                if (args[3].equalsIgnoreCase("set")) {
                    if (!EditorManager.hasSelection(player.getUniqueId())) {
                        player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT_CLOSED, 2f, 1f);
                        player.sendMessage(AwesomeText.colorizeHex("&cError: &7You currently do not have anything selected."));
                        return true;
                    }

                    Location firstPos = EditorManager.getFirstPos(player.getUniqueId());
                    Location secondPos = EditorManager.getSecondPos(player.getUniqueId());

                    region.setWorld(index, firstPos.getWorld());
                    region.setFirstPoint(index, firstPos.toVector());
                    region.setSecondPoint(index, secondPos.toVector());

                    if (!region.save()) {
                        player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT_CLOSED, 2f, 1f);
                        player.sendMessage(AwesomeText.colorizeHex("&cError: &7Something went wrong while saving."));
                        return true;
                    }

                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2f, 1f);
                    player.sendMessage(AwesomeText.colorizeHex("&a&l» &7Region blueprint &f" + blueprint.getBlueprintName() + "&7 has been set."));
                    return true;
                } else if (args[3].equalsIgnoreCase("select")) {
                    if (!region.isIndexValid(index)) {
                        player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT_CLOSED, 2f, 1f);
                        player.sendMessage(AwesomeText.colorizeHex("&cError: &7Region blueprint &f" + blueprint.getBlueprintName() + "&7 has not been set."));
                        return true;
                    }

                    EditorManager.setFirstPos(player.getUniqueId(), region.getFirstLocation(index));
                    EditorManager.setSecondPos(player.getUniqueId(), region.getSecondLocation(index));

                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2f, 1f);
                    player.sendMessage(AwesomeText.colorizeHex("&a&l» &7Region blueprint &f" + blueprint.getBlueprintName() + "&7 has been selected."));
                    return true;
                } else if (args[3].equalsIgnoreCase("reset")) {
                    if (!region.delete(index)) {
                        player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT_CLOSED, 2f, 1f);
                        player.sendMessage(AwesomeText.colorizeHex("&cError: &7Something went wrong while deleting."));
                        return true;
                    }

                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2f, 1f);
                    player.sendMessage(AwesomeText.colorizeHex("&a&l» &7Region blueprint &f" + blueprint.getBlueprintName() + "&7 has been reset."));
                    return true;
                } else {
                    player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT_CLOSED, 2f, 1f);
                    player.sendMessage(AwesomeText.colorizeHex("&cError: &7Invalid arguments."));
                    return true;
                }
            } else if (blueprint instanceof Anchor anchor) {
                if (args[3].equalsIgnoreCase("set")) {
                    anchor.setLocation(index, player.getLocation());

                    if (!anchor.save()) {
                        player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT_CLOSED, 2f, 1f);
                        player.sendMessage(AwesomeText.colorizeHex("&cError: &7Something went wrong while saving."));
                        return true;
                    }

                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2f, 1f);
                    player.sendMessage(AwesomeText.colorizeHex("&a&l» &7Anchor blueprint &f" + blueprint.getBlueprintName() + "&7 has been set."));
                    return true;
                } else if (args[3].equalsIgnoreCase("teleport")) {
                    if (!anchor.status()) {
                        player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT_CLOSED, 2f, 1f);
                        player.sendMessage(AwesomeText.colorizeHex("&cError: &7Anchor blueprint &f" + blueprint.getBlueprintName() + "&7 has not been set."));
                        return true;
                    }

                    player.teleport(anchor.getLocation(index));

                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2f, 1f);
                    player.sendMessage(AwesomeText.colorizeHex("&a&l» &7You have been teleported to the anchor blueprint &f" + blueprint.getBlueprintName() + "&7."));
                    return true;
                } else if (args[3].equalsIgnoreCase("reset")) {
                    if (!anchor.delete(index)) {
                        player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT_CLOSED, 2f, 1f);
                        player.sendMessage(AwesomeText.colorizeHex("&cError: &7Something went wrong while deleting."));
                        return true;
                    }

                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2f, 1f);
                    player.sendMessage(AwesomeText.colorizeHex("&a&l» &7Anchor blueprint &f" + blueprint.getBlueprintName() + "&7 has been reset."));
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
        } else if (args[0].equalsIgnoreCase("transition")) {
            if (args.length < 2) {
                player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT_CLOSED, 2f, 1f);
                player.sendMessage(AwesomeText.colorizeHex("&cError: &7Not enough arguments."));
                return true;
            }

            Transition transition = Arrays.stream(Transition.values()).filter(trans -> trans.getLabel().equals(args[1])).findFirst().orElse(null);

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

    private void openGameMenu(Player player) {
        GuiMenu menu = new GuiMenu(InventoryType.CHEST, AwesomeText.beautifyMessage("<#312dff>Game Admin Panel"));

        menu.onClick((event) -> {
            event.setCancelled(true);
            return false;
        });

        // Create editor selection buttons
        ItemStack enabledEditorButton = new ItemStack(Material.GOLDEN_AXE);
        {
            ItemMeta itemMeta = enabledEditorButton.getItemMeta();
            itemMeta.customName(AwesomeText.beautifyMessage("<!i><green>Editor Enabled"));
            itemMeta.setEnchantmentGlintOverride(true);
            GameMakerUtils.appendLoreLine(itemMeta, AwesomeText.beautifyMessage("<!i><gray>Edit blueprints and manage games."));
            enabledEditorButton.setItemMeta(itemMeta);
        }
        ItemStack disabledEditorButton = new ItemStack(Material.WOODEN_AXE);
        {
            ItemMeta itemMeta = disabledEditorButton.getItemMeta();
            itemMeta.customName(AwesomeText.beautifyMessage("<!i><red>Editor Disabled"));
            GameMakerUtils.appendLoreLine(itemMeta, AwesomeText.beautifyMessage("<!i><gray>Be a regular player who can play games."));
            disabledEditorButton.setItemMeta(itemMeta);
        }

        GuiButtonUtils.createOptionSelect(menu, 12, EditorManager.isEditing(player.getUniqueId()) ? 0 : 1, new GuiButtonUtils.Option[]{
            new GuiButtonUtils.Option(enabledEditorButton, "Enabled", () -> {
                EditorManager.addEditor(player.getUniqueId());
                giveEditorWand(player);
            }),
            new GuiButtonUtils.Option(disabledEditorButton, "Disabled", () -> {
                EditorManager.revokeEditor(player.getUniqueId());
            }),
        });

        // Create transition selection buttons
        ItemStack continuousTransButton = new ItemStack(Material.REPEATER);
        {
            ItemMeta itemMeta = continuousTransButton.getItemMeta();
            itemMeta.customName(AwesomeText.beautifyMessage("<!i><gold>Continuous Gameplay"));
            GameMakerUtils.appendLoreLine(itemMeta, AwesomeText.beautifyMessage("<!i><gray>Immediately after the game ends,"));
            GameMakerUtils.appendLoreLine(itemMeta, AwesomeText.beautifyMessage("<!i><gray>a new game will start."));
            continuousTransButton.setItemMeta(itemMeta);
        }
        ItemStack shuffleTransButton = new ItemStack(Material.CHORUS_FRUIT);
        {
            ItemMeta itemMeta = shuffleTransButton.getItemMeta();
            itemMeta.customName(AwesomeText.beautifyMessage("<!i><light_purple>Randomly Select Games"));
            GameMakerUtils.appendLoreLine(itemMeta, AwesomeText.beautifyMessage("<!i><gray>Games will be randomly chosen"));
            GameMakerUtils.appendLoreLine(itemMeta, AwesomeText.beautifyMessage("<!i><gray>after every game."));
            shuffleTransButton.setItemMeta(itemMeta);
        }
        ItemStack voteTransButton = new ItemStack(Material.FILLED_MAP);
        {
            ItemMeta itemMeta = voteTransButton.getItemMeta();
            itemMeta.customName(AwesomeText.beautifyMessage("<!i><yellow>Player Vote"));
            itemMeta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            GameMakerUtils.appendLoreLine(itemMeta, AwesomeText.beautifyMessage("<!i><gray>During the intermission, players"));
            GameMakerUtils.appendLoreLine(itemMeta, AwesomeText.beautifyMessage("<!i><gray>will be able to vote for which"));
            GameMakerUtils.appendLoreLine(itemMeta, AwesomeText.beautifyMessage("<!i><gray>game they want to play next."));
            voteTransButton.setItemMeta(itemMeta);
        }

        GuiButtonUtils.createOptionSelect(menu, 14, switch (GameManager.getTransition()) {
            case CONTINUOUS -> 0;
            case SHUFFLE -> 1;
            case VOTE -> 2;
        }, new GuiButtonUtils.Option[]{
            new GuiButtonUtils.Option(continuousTransButton, "Continuous", () -> {
                GameManager.setTransition(Transition.CONTINUOUS);
            }),
            new GuiButtonUtils.Option(shuffleTransButton, "Shuffle", () -> {
                GameManager.setTransition(Transition.SHUFFLE);
            }),
            new GuiButtonUtils.Option(voteTransButton, "Vote", () -> {
                GameManager.setTransition(Transition.VOTE);
            }),
        });

        menu.open(player);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return null; // Cancel if sender is not a player

        if (!player.hasPermission("partymayhem.admin")) return null; // Cancel if player doesn't have permission

        if (args.length == 1) {
            return new ArrayList<>(Arrays.asList("menu", "editor", "blueprint", "transition", "enable", "disable", "info", "start", "stop"));
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("editor")) {
            return new ArrayList<>(Arrays.asList("enable", "disable"));
        }
        if (args[0].equalsIgnoreCase("blueprint") && EditorManager.isEditing(player.getUniqueId())) {
            if (args.length == 2) {
                ArrayList<String> editorList = new ArrayList<>();
                editorList.add("lobby");
                editorList.addAll(GameManager.getMinigameIds());
                return editorList;
            }

            MinigameFactory minigame;
            if (args[1].equals("lobby")) {
                minigame = GameManager.getLobby();
            } else {
                minigame = GameManager.getMinigameById(args[1]);
            }

            if (args.length == 3 && minigame != null) {
                return minigame.getBlueprintNames();
            }
            if (args.length == 4 && minigame != null) {
                Blueprint blueprint = minigame.getBlueprintByName(args[2]);

                if (blueprint instanceof Region) {
                    return new ArrayList<>(Arrays.asList("set", "select", "reset"));
                } else if (blueprint instanceof Anchor) {
                    return new ArrayList<>(Arrays.asList("set", "teleport", "reset"));
                }
            }
            if (args.length == 5 && minigame != null) {
                Blueprint blueprint = minigame.getBlueprintByName(args[2]);

                ArrayList<String> indexes = new ArrayList<>();
                for (int i = 0; i < blueprint.getMaxAmount(); i++) {
                    indexes.add(String.valueOf(i + 1));
                }
                return indexes;
            }
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
