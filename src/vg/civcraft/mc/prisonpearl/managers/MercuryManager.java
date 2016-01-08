package vg.civcraft.mc.prisonpearl.managers;

import org.bukkit.Bukkit;

public class MercuryManager {

	public MercuryManager() {
		
	}
	
	public static boolean isMercuryEnabled() {
		return Bukkit.getPluginManager().isPluginEnabled("Mercury");
	}
}
