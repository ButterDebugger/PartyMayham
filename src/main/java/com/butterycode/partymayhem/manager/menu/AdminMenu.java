package com.butterycode.partymayhem.manager.menu;

import com.butterycode.partymayhem.games.MinigameFactory;
import com.butterycode.partymayhem.manager.GameManager;
import com.butterycode.partymayhem.manager.Transition;
import com.butterycode.partymayhem.settings.blueprint.Anchor;
import com.butterycode.partymayhem.settings.blueprint.Blueprint;
import com.butterycode.partymayhem.settings.blueprint.Region;
import com.butterycode.partymayhem.settings.options.GameOption;
import com.butterycode.partymayhem.settings.options.NumberRange;
import com.butterycode.partymayhem.settings.options.Selection;
import com.butterycode.partymayhem.settings.options.Text;
import com.butterycode.partymayhem.settings.options.Toggle;
import dev.debutter.cuberry.paper.utils.AwesomeText;
import io.papermc.paper.connection.PlayerGameConnection;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.event.player.PlayerCustomClickEvent;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.body.PlainMessageDialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

public class AdminMenu implements Listener {

    @SuppressWarnings("UnstableApiUsage")
    public static Dialog beginningDialog() {
        return Dialog.create(builder -> builder.empty()
            .base(DialogBase.builder(Component.text("Navigate to where you want to manage")).build())
            .type(DialogType.multiAction(List.of(
                ActionButton.create(
                    Component.text("Main Config"),
                    null,
                    100,
                    DialogAction.staticAction(ClickEvent.showDialog(mainConfigDialog()))
                ),
                ActionButton.create(
                    Component.text("Minigames"),
                    null,
                    100,
                    DialogAction.staticAction(ClickEvent.showDialog(minigameListDialog()))
                )
            )).exitAction(
                ActionButton.builder(Component.text("Exit", TextColor.color(0xFFA0AB)))
                    .width(200)
                    .build()
            ).build())
        );
    }

    @SuppressWarnings("UnstableApiUsage")
    public static Dialog mainConfigDialog() {
        Transition transition = GameManager.getTransition();

        return Dialog.create(builder -> builder.empty()
            .base(DialogBase.builder(Component.text("Configure the global plugin config"))
                .inputs(List.of(
                    DialogInput.singleOption(
                        "transition",
                        200,
                        List.of(
                            SingleOptionDialogInput.OptionEntry.create(
                                "continuous",
                                AwesomeText.beautifyMessage("Continuous"),
                                transition.equals(Transition.CONTINUOUS)
                            ),
                            SingleOptionDialogInput.OptionEntry.create(
                                "shuffle",
                                AwesomeText.beautifyMessage("Shuffle"),
                                transition.equals(Transition.SHUFFLE)
                            ),
                            SingleOptionDialogInput.OptionEntry.create(
                                "vote",
                                AwesomeText.beautifyMessage("Vote"),
                                transition.equals(Transition.VOTE)
                            )
                        ),
                        AwesomeText.beautifyMessage("Transition"),
                        true
                    )
                ))
                .build()
            )
            .type(DialogType.confirmation(
                ActionButton.create(
                    Component.text("Confirm", TextColor.color(0x9EFFA3)),
                    Component.text("Click to apply your changes"),
                    100,
                    DialogAction.customClick(
                        Key.key("party_mayhem:user_input/edit_main_config"),
                        null
                    )
                ),
                ActionButton.create(
                    Component.text("Discard", TextColor.color(0xFFA0AB)),
                    Component.text("Click to discard your changes"),
                    100,
                    null // If we set the action to null, it doesn't do anything and closes the dialog
                )
            ))
        );
    }

    @SuppressWarnings("UnstableApiUsage")
    public static Dialog minigameListDialog() {
        return Dialog.create(builder -> builder.empty()
            .base(DialogBase.builder(Component.text("Pick a minigame to manage")).build())
            .type(DialogType.multiAction(GameManager.getMinigames().stream().map((minigame) ->
                ActionButton.create(
                    minigame.getDisplayName(),
                    Component.text("Click to edit this game"),
                    100,
                    DialogAction.staticAction(ClickEvent.showDialog(beforeEditMinigameDialog(minigame)))
                )).toList()
            ).exitAction(
                ActionButton.builder(Component.text("Exit", TextColor.color(0xFFA0AB)))
                    .width(200)
                    .build()
            ).build())
        );
    }

    @SuppressWarnings("UnstableApiUsage")
    private static Dialog beforeEditMinigameDialog(MinigameFactory minigame) {
        // Collect minigame actions
        List<ActionButton> actions = new ArrayList<>();

        actions.add(ActionButton.create(
            Component.text("Show Status", TextColor.color(0x9EFFA3)),
            Component.text("Displays the setup status of this minigame"),
            100,
            DialogAction.staticAction(ClickEvent.showDialog(showMinigameStatusDialog(minigame)))
        ));

        actions.add(ActionButton.create(
            Component.text("Edit Options"),
            null,
            100,
            DialogAction.staticAction(ClickEvent.showDialog(editMinigameOptionsDialog(minigame)))
        ));

        if (!minigame.getBlueprints().isEmpty()) actions.add(ActionButton.create(
            Component.text("Edit Blueprints"),
            null,
            100,
            DialogAction.staticAction(ClickEvent.showDialog(viewMinigameBlueprintsListDialog(minigame)))
        ));

        // Create the dialog
        return Dialog.create(builder -> builder.empty()
            .base(DialogBase.builder(Component.text("Navigate to where you want to manage")).build())
            .type(DialogType.multiAction(actions).exitAction(
                ActionButton.builder(Component.text("Exit", TextColor.color(0xFFA0AB)))
                    .width(200)
                    .build()
            ).build())
        );
    }

    @SuppressWarnings("UnstableApiUsage")
    private static Dialog showMinigameStatusDialog(MinigameFactory minigame) {
        List<PlainMessageDialogBody> body = new ArrayList<>();

        body.add(DialogBody.plainMessage(AwesomeText.beautifyMessage(
            "The game is <status>",
            Placeholder.parsed("status", minigame.isEnabled()
                ? minigame.isSetup()
                    ? minigame.isReady()
                        ? "<green>Ready"
                        : "<yellow>Setup"
                    : "<gold>Not Setup"
                : "<red>Disabled")
        )));

        body.add(DialogBody.plainMessage(AwesomeText.beautifyMessage(
            "The minimum players required is <gold><min_players></gold>",
            Placeholder.unparsed("min_players", String.valueOf(minigame.getMinPlayers()))
        )));

        // Collect the blueprints status
        if (!minigame.getBlueprints().isEmpty()) {
            body.add(DialogBody.plainMessage(AwesomeText.beautifyMessage("<b><u>Blueprints</u>:</b>")));
        }

        for (Blueprint blueprint : minigame.getBlueprints()) {
            body.add(DialogBody.plainMessage(AwesomeText.beautifyMessage(
                "<name> <gray>-</gray> <status>",
                Placeholder.component("name", blueprint.getDisplayName()),
                Placeholder.parsed("status", blueprint.status() ? "<green>Good" : "<red>Bad")
            )));
        }

        // Collect the options status
        if (!minigame.getOptions().isEmpty()) {
            body.add(DialogBody.plainMessage(AwesomeText.beautifyMessage("<b><u>Options</u>:</b>")));
        }

        for (GameOption<?> option : minigame.getOptions()) {
            body.add(DialogBody.plainMessage(AwesomeText.beautifyMessage(
                "<name> <gray>=</gray> <value>",
                Placeholder.component("name", option.getDisplayName()),
                Placeholder.unparsed("value", String.valueOf(option.getValue()))
            )));
        }

        // Create the dialog
        return Dialog.create(builder -> builder.empty()
            .base(DialogBase.builder(Component.text("Current minigame status"))
                .body(body)
                .build()
            )
            .type(DialogType.notice())
        );
    }

    @SuppressWarnings("UnstableApiUsage")
    private static Dialog editMinigameOptionsDialog(MinigameFactory minigame) {
        // Collect minigame inputs
        List<DialogInput> blueprintInputs = new ArrayList<>();

        blueprintInputs.add(DialogInput.bool("enabled", AwesomeText.beautifyMessage("Enable"))
            .initial(minigame.isEnabled())
            .build());

        for (GameOption<?> option : minigame.getOptions()) {
            switch (option) {
                case NumberRange numberRange -> blueprintInputs.add(DialogInput.numberRange(
                        numberRange.getKey(),
                        numberRange.getDisplayName(),
                        numberRange.getRangeStart(),
                        numberRange.getRangeEnd()
                    )
                    .initial(numberRange.getValue())
                    .step(numberRange.getRangeStep())
                    .build());
                case Selection selection -> blueprintInputs.add(DialogInput.singleOption(
                        selection.getKey(),
                        selection.getDisplayName(),
                        selection.getOptions().stream().map(selectionOption -> SingleOptionDialogInput.OptionEntry.create(
                            selectionOption,
                            Component.text(selectionOption),
                            selectionOption.equals(selection.getValue())
                        )).toList()
                    )
                    .build());
                case Text text -> blueprintInputs.add(DialogInput.text(
                        text.getKey(),
                        text.getDisplayName()
                    )
                    .initial(text.getValue())
                    .build());
                case Toggle toggle -> blueprintInputs.add(DialogInput.bool(
                        toggle.getKey(),
                        toggle.getDisplayName()
                    )
                    .initial(toggle.getValue())
                    .build());
                case null, default -> throw new UnsupportedOperationException("Unimplemented option type");
            }
        }

        // Create the dialog
        return Dialog.create(builder -> builder.empty()
            .base(DialogBase.builder(Component.text("Configure the game options"))
                .inputs(blueprintInputs)
                .build()
            )
            .type(DialogType.confirmation(
                ActionButton.create(
                    Component.text("Confirm", TextColor.color(0x9EFFA3)),
                    Component.text("Click to apply your changes"),
                    100,
                    DialogAction.customClick(
                        Key.key("party_mayhem:user_input/edit_minigame_options"),
                        BinaryTagHolder.binaryTagHolder("{minigame_id:\"" + minigame.getId() + "\"}") // This is probably harmless
                    )
                ),
                ActionButton.create(
                    Component.text("Discard", TextColor.color(0xFFA0AB)),
                    Component.text("Click to discard your changes"),
                    100,
                    null // If we set the action to null, it doesn't do anything and closes the dialog
                )
            ))
        );
    }

    @SuppressWarnings("UnstableApiUsage")
    private static Dialog viewMinigameBlueprintsListDialog(MinigameFactory minigame) {
        return Dialog.create(builder -> builder.empty()
            .base(DialogBase.builder(Component.text("Pick a blueprint to manage"))
                .body(List.of(
                    DialogBody.plainMessage(AwesomeText.beautifyMessage("This game is practically glowing"))
                ))
                .build()
            )
            .type(DialogType.multiAction(minigame.getBlueprints().stream().map((blueprint) ->
                ActionButton.create(
                    blueprint.getDisplayName(),
                    Component.text("Click to edit this blueprint"),
                    100,
                    DialogAction.staticAction(ClickEvent.showDialog(editMinigameBlueprintDialog(minigame, blueprint)))
                )).toList()
            ).exitAction(
                ActionButton.builder(Component.text("Exit", TextColor.color(0xFFA0AB)))
                    .width(200)
                    .build()
            ).build())
        );
    }

    @SuppressWarnings("UnstableApiUsage")
    private static Dialog editMinigameBlueprintDialog(MinigameFactory minigame, Blueprint blueprint) {
        // Collect minigame inputs
        List<ActionButton> actions = new ArrayList<>();

        if (blueprint instanceof Region) {
            actions.add(ActionButton.create(
                Component.text("Select", TextColor.color(0xFFFF9E)),
                null,
                100,
                DialogAction.staticAction(ClickEvent.runCommand(String.join(" ", List.of(
                    "blueprint",
                    minigame.getId(),
                    blueprint.getId(),
                    "select"
                ))))
            ));
            actions.add(ActionButton.create(
                Component.text("Set", TextColor.color(0x9EFFFF)),
                null,
                100,
                DialogAction.staticAction(ClickEvent.runCommand(String.join(" ", List.of(
                    "blueprint",
                    minigame.getId(),
                    blueprint.getId(),
                    "set"
                ))))
            ));
            actions.add(ActionButton.create(
                Component.text("Reset", TextColor.color(0xFFA0AB)),
                null,
                100,
                DialogAction.staticAction(ClickEvent.runCommand(String.join(" ", List.of(
                    "blueprint",
                    minigame.getId(),
                    blueprint.getId(),
                    "reset"
                ))))
            ));
        } else if (blueprint instanceof Anchor) {
            actions.add(ActionButton.create(
                Component.text("Teleport", TextColor.color(0xDB9EFF)),
                null,
                100,
                DialogAction.staticAction(ClickEvent.runCommand(String.join(" ", List.of(
                    "blueprint",
                    minigame.getId(),
                    blueprint.getId(),
                    "teleport"
                ))))
            ));
            actions.add(ActionButton.create(
                Component.text("Set", TextColor.color(0x9EFFFF)),
                null,
                100,
                DialogAction.staticAction(ClickEvent.runCommand(String.join(" ", List.of(
                    "blueprint",
                    minigame.getId(),
                    blueprint.getId(),
                    "set"
                ))))
            ));
            actions.add(ActionButton.create(
                Component.text("Reset", TextColor.color(0xFFA0AB)),
                null,
                100,
                DialogAction.staticAction(ClickEvent.runCommand(String.join(" ", List.of(
                    "blueprint",
                    minigame.getId(),
                    blueprint.getId(),
                    "reset"
                ))))
            ));
        } else {
            throw new UnsupportedOperationException("Unimplemented blueprint type");
        }

        // Create the minigame dialog
        return Dialog.create(builder -> builder.empty()
            .base(DialogBase.builder(Component.text("Configure the game blueprint"))
                .body(List.of(
                    DialogBody.plainMessage(AwesomeText.beautifyMessage("This game is practically glowing"))
                ))
                .build()
            )
            .type(DialogType.multiAction(actions)
            .exitAction(
                ActionButton.builder(Component.text("Exit", TextColor.color(0xFFA0AB)))
                    .width(200)
                    .build()
            ).build())
        );
    }

    @SuppressWarnings("UnstableApiUsage")
    @EventHandler
    void handleLevelsDialog(PlayerCustomClickEvent event) {
        if (event.getCommonConnection() instanceof PlayerGameConnection conn) {
            Player player = conn.getPlayer();
            player.sendMessage(AwesomeText.beautifyMessage("" + event.getIdentifier()));
        }

        DialogResponseView view = event.getDialogResponseView();
        if (view == null) return;

        if (event.getCommonConnection() instanceof PlayerGameConnection conn) {
            Player player = conn.getPlayer();

            player.sendMessage(AwesomeText.beautifyMessage("" + view.payload()));

            if (event.getIdentifier().equals(Key.key("party_mayhem:user_input/edit_minigame_options"))) {
                // Handle editing a minigames options
                String minigameId = view.getText("minigame_id");
                if (minigameId == null) {
                    player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT_CLOSED, 2f, 1f);
                    player.sendMessage(AwesomeText.beautifyMessage("<red>Error:</red> <gray>Something went wrong.</gray>"));
                    return;
                }

                MinigameFactory minigame = GameManager.getMinigameById(minigameId);
                if (minigame == null) {
                    player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT_CLOSED, 2f, 1f);
                    player.sendMessage(AwesomeText.beautifyMessage("<red>Error:</red> <gray>Could not find the minigame.</gray>"));
                    return;
                }

                // Update options
                for (GameOption<?> option : minigame.getOptions()) {
                    switch (option) {
                        case NumberRange numberRange -> {
                            Float value = view.getFloat(option.getKey());
                            if (value == null) continue;

                            numberRange.setValue(value);
                        }
                        case Selection selection -> {
                            String value = view.getText(option.getKey());
                            if (value == null) continue;

                            selection.setValue(value);
                        }
                        case Text text -> {
                            String value = view.getText(option.getKey());
                            if (value == null) continue;

                            text.setValue(value);
                        }
                        case Toggle toggle -> {
                            Boolean value = view.getBoolean(option.getKey());
                            if (value == null) continue;

                            toggle.setValue(value);
                        }
                        case null, default -> throw new UnsupportedOperationException("Unimplemented option type");
                    }
                }

                // Update built-in options
                {
                    Boolean enabled = view.getBoolean("enabled");

                    if (enabled != null) {
                        minigame.setEnabled(enabled);
                    }
                }

                // Send a successful message to the player
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2f, 1f);
                player.sendMessage(AwesomeText.beautifyMessage("<green><bold>»</bold></green> <gray>Options have been updated.</gray>"));
            } else if (event.getIdentifier().equals(Key.key("party_mayhem:user_input/edit_main_config"))) {
                // Handle editing the plugin config
                Transition transition = Transition.getByLabel(view.getText("transition"));
                if (transition == null) {
                    player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT_CLOSED, 2f, 1f);
                    player.sendMessage(AwesomeText.beautifyMessage("<red>Error:</red> <gray>Something went wrong.</gray>"));
                    return;
                }

                // Update plugin config
                GameManager.setTransition(transition);

                // Send a successful message to the player
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2f, 1f);
                player.sendMessage(AwesomeText.beautifyMessage("<green><bold>»</bold></green> <gray>Config has been updated.</gray>"));
            }
        }
    }

}
