package vg.civcraft.mc.prisonpearl.managers;

import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;

import vg.civcraft.mc.prisonpearl.PrisonPearlPlugin;
import vg.civcraft.mc.prisonpearl.managers.ban.CBanManager;
import vg.civcraft.mc.prisonpearl.managers.ban.FBanManager;

/**
 * Extracting common functions as abstract class to allow switching out ban managers
 * 
 * @author ProgrammerDan <programmerdan@gmail.com>
 *
 */
public abstract class BanManager{

	public abstract BanManager enable();
	public abstract void setBanMessage(String msg);
	public abstract String getBanMessage();
	public abstract boolean isBanned(UUID uuidName);
	public abstract Set<UUID> listBannedUUIDS();
	public abstract void ban(UUID UUIDName);
	public abstract void pardon(UUID UUIDName);
	public abstract void setBanState(UUID UUIDName, boolean isBanned);
	public abstract boolean hasBannedPlayers();
	
	public static BanManager initialize(PrisonPearlPlugin plugin) {
		if (plugin.getServer().getPluginManager().isPluginEnabled("CBanManagement")) {
			return new CBanManager(plugin);
		}
		else return new FBanManager(plugin);
	}
}
