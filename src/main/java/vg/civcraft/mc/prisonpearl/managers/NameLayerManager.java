package vg.civcraft.mc.prisonpearl.managers;

import java.util.UUID;

import org.bukkit.Bukkit;

import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.prisonpearl.PrisonPearlPlugin;

public class NameLayerManager {

	public static String getName(UUID uuid) {
		if (PrisonPearlPlugin.isNameLayerEnabled())
			return NameAPI.getCurrentName(uuid);
		return Bukkit.getOfflinePlayer(uuid).getName();
	}
	
	public static UUID getUUID(String name) {
		if (PrisonPearlPlugin.isNameLayerEnabled())
			return NameAPI.getUUID(name);
		return Bukkit.getOfflinePlayer(name).getUniqueId();
	}
}
