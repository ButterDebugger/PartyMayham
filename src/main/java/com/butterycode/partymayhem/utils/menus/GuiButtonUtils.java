package com.butterycode.partymayhem.utils.menus;

import com.butterycode.partymayhem.utils.GameMakerUtils;
import dev.debutter.cuberry.paper.utils.AwesomeText;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GuiButtonUtils {

    public record Option(ItemStack item, String label, Runnable callback) {}

    public static void createOptionSelect(GuiMenu menu, int slot, int initialOptionIndex, Option[] options) {
        ItemStack[] itemCycles = new ItemStack[options.length];

        // Create clickable item cycles
        for (int i = 0; i < options.length; i++) {
            ItemStack item = options[i].item();
            ItemMeta itemMeta = item.getItemMeta();

            // Append options
            GameMakerUtils.appendLoreLine(itemMeta, AwesomeText.beautifyMessage(""));
            for (int j = 0; j < options.length; j++) {
                if (i == j) {
                    GameMakerUtils.appendLoreLine(itemMeta, AwesomeText.beautifyMessage("<!i><dark_aqua>» <aqua>" + options[j].label()));
                } else {
                    GameMakerUtils.appendLoreLine(itemMeta, AwesomeText.beautifyMessage("<!i><gray>" + options[j].label()));
                }
            }

            item.setItemMeta(itemMeta);

            itemCycles[i] = item;

            // Add click handler
            int index = i;
            menu.onClick(item, (event) -> {
                Player human = (Player) event.getWhoClicked();

                if (event.getClick().isRightClick()) {
                    // Cycle backwards
                    human.playSound(human.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.5f, 0.8f);

                    int newIndex = index - 1;
                    if (newIndex < 0) newIndex = options.length - 1;

                    options[newIndex].callback().run();
                    menu.getInventory().setItem(slot, itemCycles[newIndex]);
                } else {
                    // Cycle forwards
                    human.playSound(human.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.5f, 1.2f);

                    int newIndex = (index + 1) % options.length;

                    options[newIndex].callback().run();
                    menu.getInventory().setItem(slot, itemCycles[newIndex]);
                }

                event.setCancelled(true);
                return true;
            });
        }

        // Set initial item button
        menu.getInventory().setItem(slot, itemCycles[initialOptionIndex]);
    }

}
