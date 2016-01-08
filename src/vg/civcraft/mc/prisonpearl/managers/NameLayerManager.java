package vg.civcraft.mc.prisonpearl.managers;

import org.bukkit.Bukkit;

public class NameLayerManager {

	public static boolean isNameLayerEnabled() {
		return Bukkit.getPluginManager().isPluginEnabled("NameLayer");
	}
}
