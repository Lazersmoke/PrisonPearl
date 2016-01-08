package vg.civcraft.mc.prisonpearl;

import java.util.logging.Level;

import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.prisonpearl.database.DataBaseHandler;
import vg.civcraft.mc.prisonpearl.managers.BanManager;

public class PrisonPearlPlugin extends ACivMod {

	private static PrisonPearlPlugin plugin;
	private static BanManager banManager;
	private static DataBaseHandler dbHandle; // This class handles both the save/load and getting storage types.
	
	public void onEnable() {
		super.onEnable();
		plugin = this;
		// It would be best to load the storage first so all managers have it available;
		dbHandle = new DataBaseHandler();
		banManager = BanManager.initialize(this);
	}
	
	public void onDisable() {
		super.onDisable();
	}
	
	@Override
	protected String getPluginName() {
		return "PrisonPearl";
	}
	
	public static DataBaseHandler getDBHandler() {
		return dbHandle;
	}
	
	public static BanManager getBanManager() {
		return banManager;
	}
	
	public static PrisonPearlPlugin getInstance() {
		return plugin;
	}
	
	public static void log(String message) {
		plugin.getLogger().log(Level.INFO, message);
	}
	
}
