package com.butterycode.partymayhem;

import com.butterycode.partymayhem.games.presets.Spleef;
import com.butterycode.partymayhem.manager.EditorManager;
import com.butterycode.partymayhem.manager.GameCommand;
import com.butterycode.partymayhem.manager.GameManager;
import dev.debutter.cuberry.paper.utils.storage.DataManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class PartyMayhem extends JavaPlugin {

    private static PartyMayhem plugin;
    private static DataManager dataInstance;

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onEnable() {
        plugin = this;

        dataInstance = new DataManager(plugin);

        getServer().getPluginManager().registerEvents(new GameManager(), plugin);

        new Spleef();

        getCommand("game").setExecutor(new GameCommand());
        getCommand("game").setTabCompleter(new GameCommand());

        getServer().getPluginManager().registerEvents(new EditorManager(), plugin);

        EditorManager.init();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        GameManager.stopMinigames();

        dataInstance.saveAll();
    }

    public static PartyMayhem getPlugin() {
        return plugin;
    }
    public static DataManager getData() {
        return dataInstance;
    }
}
