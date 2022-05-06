package dev.metanoia.smartitemsort.worldguard;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Supplier;
import java.util.logging.Level;

import static org.bukkit.Bukkit.getPluginManager;


// This plugin acts as a bridge between the SmartItemSort plugin and the WorldGuard plugin.
//
// It listens for the events that SmartItemSort generates when it attempts to bind a particular item drop
// teleporter to a potential drop target destination. When this occurs, this plugin identifies the region
// that the source and destination are in. Unless they are in the same region, it cancels the bind attempt.
// This will prevent items from being transferred out of the region they are currently in.

public final class SmartItemSortWorldGuard extends JavaPlugin {

	private Config pluginConfig;
	private ColorLogger logger;
	private BindTargetListener listener;

	@Override
	public void onEnable() {
		super.onEnable();
		load();
	}


	@Override
	public void onDisable() {
		super.onDisable();
		unload();
	}


	private void load() {
		this.logger = new ColorLogger(this);
		this.pluginConfig = new Config(this);

		// the whole purpose of this plugin is this event listener.
		listener = new BindTargetListener(this);
		final PluginManager pluginManager = getPluginManager();
		pluginManager.registerEvents(listener, this);

		info(() -> String.format("Loaded %s by %s", getName(), getDescription().getAuthors()));
	}


	private void unload() {
		HandlerList.unregisterAll(listener);
		this.listener = null;

		this.pluginConfig = null;

		info(() -> "Unloaded SmartItemSort WorldGuard bridge by BornToCode");
		this.logger = null;
	}




	///
	/// Logging API
	///

	public void setLevel(Level level) { this.logger.setLevel(level); }

	public void config(Supplier<String> message) { this.logger.config(message); }
	public void debug(Supplier<String> message) { this.logger.debug(message); }
	public void error(Supplier<String> message) { this.logger.error(message); }
	public void info(Supplier<String> message) { this.logger.info(message); }
	public void trace(Supplier<String> message) { this.logger.trace(message); }

}

